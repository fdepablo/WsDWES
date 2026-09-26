# Primeras operaciones con Jakarta Persistence

Este módulo adapta `32_JPA` del antiguo `WorkspaceJava` para introducir JPA de dos formas: una demostración de consola y un CRUD web. `DemoJpa` permite observar `persist`, `find`, `merge` y `remove` en secuencia; la interfaz web permite probar las mismas ideas desde el navegador. H2 funciona por defecto en un archivo; también se puede usar MariaDB de XAMPP.

**Orden de trabajo:** primero ejecuta y comprende `main.DemoJpa`, sin Tomcat. Cuando hayas observado las operaciones JPA y el ciclo de vida del `EntityManager`, pasa a la versión web. Esta segunda parte reutiliza lo aprendido y añade peticiones HTTP, un servicio y vistas JSP; no es necesario abordarlas a la vez.

## Qué vas a aprender

- Relacionar una clase Java con una tabla mediante `@Entity`, `@Table`, `@Id` y `@GeneratedValue`.
- Entender para qué sirven la unidad de persistencia, `EntityManagerFactory` y `EntityManager`.
- Usar `persist`, `find` y `remove` con entidades gestionadas y observar qué devuelve `merge`.
- Modificar una entidad dentro de una transacción sin escribir `UPDATE` a mano.
- Consultar varias entidades con JPQL y una consulta tipada.
- Confirmar o revertir transacciones y cerrar los recursos.
- Cambiar entre H2 y MariaDB editando `persistence.xml`, sin modificar código Java.
- Reutilizar la unidad de persistencia desde consola y desde un servlet, separando controlador, servicio y vistas.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Tomcat 11 solo para la parte web; la demostración `main` no lo necesita.
- Haber trabajado el CRUD con JDBC en `07_JDBC` y la separación de responsabilidades en `08_MVC`.
- Para MariaDB: XAMPP con el servicio **MySQL** iniciado y acceso a phpMyAdmin.

## JPA en este ejemplo

JPA es la especificación de persistencia; Hibernate es la implementación incluida como dependencia Maven. La clase `Persona` describe los datos y sus anotaciones indican cómo relacionarlos con la tabla `personas`. `persistence.xml` define la unidad `PersonasPU`, el proveedor y la conexión. Tanto `DemoJpa` como `PersonaServicio` crean una `EntityManagerFactory` al iniciarse y un `EntityManager` para cada operación. La elección de base de datos queda fuera del código Java.

### De objetos Java a filas: ORM

En JDBC escribíamos SQL y convertíamos manualmente cada fila de `ResultSet` en un objeto. JPA propone un **mapeo objeto-relacional** (ORM): describimos qué clase representa una entidad, cómo se identifica y qué propiedades se guardan; el proveedor genera el SQL necesario. Así podemos trabajar con `Persona` y sus atributos en vez de construir a mano cada `INSERT`, `SELECT`, `UPDATE` y `DELETE`. Esto no elimina la base relacional ni sustituye el conocimiento de SQL: conviene comprobar qué tablas y consultas produce Hibernate, especialmente cuando el modelo crezca.

JPA define la API y las reglas; **Hibernate** es el proveedor que las ejecuta en este módulo. Existen otros proveedores de JPA, pero cambiar de proveedor requeriría revisar dependencias y configuración y probar el resultado. **MyBatis no es una implementación de JPA**: es otra herramienta de persistencia con un enfoque diferente. La configuración por anotaciones y valores predeterminados reduce el código, aunque aquí declaramos expresamente los nombres y restricciones que ayudan a aprender el mapeo.

### Anotaciones de `Persona`

| Anotación | Qué indica | En este módulo |
| --- | --- | --- |
| `@Entity` | La clase tiene identidad persistente y JPA la gestiona. | `Persona` se convierte en entidad. |
| `@Table(name = "personas")` | Nombre de la tabla; no dependemos del nombre elegido por defecto. | La tabla se llama `personas`. |
| `@Id` | Propiedad que identifica de forma única una fila. | `id` es la clave primaria. |
| `@GeneratedValue(strategy = IDENTITY)` | La base genera el identificador al insertar. | No asignamos el ID al construir `Persona`. |
| `@Column(nullable = false, length = 80)` | Ajusta el mapeo y las restricciones de una columna. | `nombre` no admite `NULL` y tiene longitud máxima de 80. |

`edad` y `peso` también se guardan aunque no lleven `@Column`: JPA mapea por defecto los atributos persistentes de la entidad. Como las anotaciones están en los **campos** (`id`, `nombre`), este ejemplo usa acceso por campos. JPA necesita un constructor sin argumentos accesible al proveedor para reconstruir una entidad desde la base; por eso `Persona` tiene uno `protected`. El constructor público facilita crear instancias nuevas en el programa.

La restricción de `@Column` describe el esquema; **no sustituye la validación de los datos recibidos**. En la parte web, `PersonaServicio` comprueba el nombre, la edad y el peso antes de guardar. En la demostración `main` se usan valores fijos conocidos para centrar la atención en JPA.

El README antiguo citaba otras anotaciones que **no se usan aquí**, pero conviene reconocerlas: `@Transient` excluye una propiedad del mapeo; `@Enumerated` indica cómo guardar un `enum` (por ejemplo, como texto con `EnumType.STRING`); `@Temporal` se empleaba para precisar fecha, hora o ambas al mapear `java.util.Date` o `Calendar`. Si usamos tipos modernos como `LocalDate` o `LocalDateTime`, no necesitamos `@Temporal`. Las anotaciones de relaciones (`@OneToOne`, `@ManyToOne`, etc.) se estudian en `10_JPARelaciones`.

### Unidad de persistencia, factoría y gestor

Maven descarga las bibliotecas declaradas en `pom.xml`. El archivo `src/main/resources/META-INF/persistence.xml` define **dónde** persistir: la unidad `PersonasPU`, la clase `Persona`, Hibernate como proveedor y las propiedades JDBC. El nombre de la unidad debe coincidir exactamente con el usado en `Persistence.createEntityManagerFactory("PersonasPU")`. Un `persistence.xml` puede definir varias unidades, cada una con su propia configuración; esta práctica solo necesita una. La base de datos MariaDB debe existir antes de ejecutar el ejemplo; Hibernate puede crear la tabla con la configuración de práctica, no el servidor ni necesariamente la base.

`EntityManagerFactory` es la factoría de gestores. Crear una factoría tiene un coste, así que en la consola se crea una vez para todo `main` y se cierra al finalizar; en la web, `PersonaServicio` la conserva durante la vida del servlet. De una misma factoría pueden salir varios `EntityManager`. Cada gestor representa una **unidad de trabajo** con su propio contexto de persistencia y se cierra al terminar. No es simplemente una conexión JDBC mantenida abierta: el proveedor administra las conexiones y el contexto según las operaciones.

Un `EntityManager` no está pensado para compartirse entre hilos. Por eso `PersonaServicio` abre uno en cada llamada, en lugar de guardarlo como atributo reutilizado por todas las peticiones HTTP. La factoría sí puede compartirse. Tampoco guardamos un `EntityManager` en `HttpSession`: prolongar su vida entre peticiones complicaría las transacciones, el uso concurrente y la liberación de recursos.

### Contexto de persistencia: gestionada o separada

Cada gestor de entidades mantiene su propio **contexto de persistencia**: las entidades obtenidas con `find` quedan gestionadas mientras ese gestor está abierto. En `actualizar`, se busca una `Persona` gestionada y se cambian sus propiedades; al confirmar la transacción, Hibernate sincroniza esos cambios con la base. No hace falta llamar a `merge` en ese caso.

El contexto actúa como una caché de primer nivel **propia de ese gestor**: si buscamos dos veces la misma clave dentro del mismo contexto, recibimos la misma instancia gestionada. Eso no significa que todos los gestores compartan los mismos objetos ni que cualquier cambio ya esté confirmado en la base. Mientras la entidad está gestionada, el proveedor detecta los cambios de sus atributos; al sincronizar el contexto puede emitir SQL. Al cerrar el gestor, los objetos que conservamos dejan de estar gestionados. `contains(objeto)` permite comprobarlo mientras el gestor está abierto.

El programa vuelve a buscar a Ana y cierra ese gestor. La instancia devuelta queda **separada** del contexto. Tras cambiar su peso, `fusionar` llama a `merge`: JPA copia sus valores a una instancia gestionada y devuelve esta última. Las dos llamadas a `contains` muestran `false` para el objeto enviado y `true` para el devuelto antes de cerrar el nuevo gestor. Para evitar inserciones inesperadas, se hace `merge` con un ID que acabamos de consultar y sabemos que existe.

Las operaciones que escriben usan `begin` y `commit`. Si se produce una excepción antes de confirmar, el bloque `finally` revierte la transacción que siga activa y cierra el gestor. Las lecturas de este ejemplo no modifican datos y se realizan sin una transacción explícita. `remove` recibe una entidad gestionada, por eso primero se obtiene mediante `find`.

La consulta `select p from Persona p order by p.id` es **JPQL**: menciona la clase `Persona` y sus propiedades, no la tabla SQL `personas`. Hibernate genera el SQL correspondiente para H2 o MariaDB. El identificador se asigna al guardar porque `id` usa `GenerationType.IDENTITY`.

### Operaciones del `EntityManager`, paso a paso

1. **`persist` para crear.** `crear` recibe una `Persona` nueva, inicia una transacción y llama a `gestor.persist(persona)`. La instancia pasa a estar gestionada. `commit` confirma la operación; el ID generado se devuelve después. No presupongas que el `INSERT` sucede siempre exactamente en `commit`: con `IDENTITY`, el proveedor puede necesitar insertarla antes para obtener el ID. Por eso distinguimos *hacer gestionada* una entidad de *confirmar* la transacción.
2. **`find` para buscar.** `buscar` llama a `find(Persona.class, id)`. Devuelve la entidad gestionada dentro de ese gestor o `null` si la clave no existe. Como el método cierra el gestor antes de devolverla, quien recibe el objeto obtiene una entidad **separada**.
3. **Modificar una entidad gestionada.** `actualizar` abre transacción, busca a Ana y cambia `nombre` y `edad`. No hay una llamada explícita a `UPDATE` ni a `merge`: Hibernate detecta los cambios y los sincroniza al confirmar. La clave primaria identifica a la entidad y no debe modificarse como si fuera un dato editable.
4. **`merge` para copiar cambios de una entidad separada.** `fusionar` recibe la instancia devuelta por otra búsqueda ya cerrada. `merge` copia su estado a una **instancia gestionada y devuelve esa instancia**. El objeto pasado sigue separado: `contains(separada)` es `false` y `contains(gestionada)` es `true`. Si quisiéramos seguir cambiando valores dentro de la transacción, usaríamos la referencia devuelta, no la enviada. No es un «UPDATE garantizado»: usar `merge` indiscriminadamente con IDs inexistentes o entidades nuevas puede provocar inserciones o errores según el caso y el proveedor. Aquí conocemos el ID porque acabamos de crear y buscar a Ana.
5. **JPQL para listar.** `listar` usa `createQuery(..., Persona.class)` para obtener una `List<Persona>` con una consulta tipada. `Persona` en JPQL es el nombre de la entidad; `p.id` es una propiedad Java. No escribimos el nombre físico de la tabla ni concatenamos entradas del usuario en la consulta.
6. **`remove` para borrar.** `borrar` busca primero a Luis **con el mismo gestor** que ejecutará `remove`. Así entrega una entidad gestionada. Pasar a `remove` un objeto separado o recién construido no equivale a borrar la fila con ese ID y puede producir una excepción. Si `find` devuelve `null`, el método indica que no había nada que borrar.

El README antiguo mencionaba además operaciones que aquí no hacen falta: `contains` comprueba si un objeto está gestionado; `detach(objeto)` separa solo esa entidad y `clear()` vacía el contexto completo; `refresh(objeto)` vuelve a cargar su estado desde la base, descartando cambios locales pendientes. `flush()` sincroniza los cambios con la base **sin confirmar la transacción**: todavía podrían revertirse con `rollback`. Una consulta con nombre (`createNamedQuery`) reutiliza una consulta declarada previamente; para esta primera práctica basta con `createQuery`.

### Transacciones y cierre de recursos

En este módulo la unidad usa `RESOURCE_LOCAL`: el código controla las transacciones con `getTransaction().begin()`, `commit()` y, si queda una transacción activa al salir por un error, `rollback()`. `commit` confirma los cambios y normalmente sincroniza antes el contexto; `flush` por sí solo no equivale a confirmar. Las lecturas mostradas no abren una transacción explícita. El `finally` cierra el gestor incluso cuando una operación falla; dejarlo abierto retendría recursos. Esta misma regla se aplica al servicio web. `hibernate.hbm2ddl.auto=update` facilita crear o adaptar la tabla durante la práctica, pero no debe confundirse con la gestión de transacciones ni con una solución de migraciones para producción.

## Estructura relevante

```text
09_JPA/
├── pom.xml
├── sql/mariadb.sql
└── src/main/
    ├── java/
    │   ├── modelo/entidad/Persona.java
    │   ├── main/DemoJpa.java
    │   ├── modelo/servicio/PersonaServicio.java
    │   └── controlador/PersonaServlet.java
    ├── resources/META-INF/persistence.xml
    └── webapp/WEB-INF/vistas/{listado,formulario}.jsp
```

## Paso 1. Ejecutar la demostración `main` con H2

Desde la raíz del workspace:

```bash
mvn -pl 09_JPA -am clean package
```

En IntelliJ, recarga Maven y ejecuta `main.DemoJpa.main()` usando el módulo `09_JPA`. Para este recorrido no configures Tomcat. La configuración incluida en `src/main/resources/META-INF/persistence.xml` usa H2 y guarda la base en `~/wsdwes_09_jpa.mv.db`, dentro de la carpeta personal del usuario. Hibernate crea o adapta la tabla al iniciar; los registros se conservan entre ejecuciones.

El programa crea a Ana y Luis, busca a Ana, la modifica como entidad gestionada, cambia su peso mediante `merge`, lista las personas, borra a Luis y vuelve a listar. Los ID se imprimen porque son generados por la base. Al ejecutar otra vez, aparecen nuevas personas y permanecen las Ana de ejecuciones anteriores; el ejemplo no vacía la base automáticamente.

## Paso 2. Ejecutar la versión web

Una vez comprendida la demostración de consola, construye el módulo con `mvn -pl 09_JPA -am clean package` y despliega `09_JPA/target/09_JPA-1.0-SNAPSHOT.war` en Tomcat 11, como en los módulos web anteriores. Abre `http://localhost:8080/09_JPA-1.0-SNAPSHOT/personas` (ajusta el contexto si cambias el nombre del WAR). La raíz del contexto incluye un enlace al listado. Puedes crear, editar y eliminar personas; al recargar verás los datos conservados.

`PersonaServlet` interpreta rutas, parámetros y respuestas HTTP. `PersonaServicio` valida los datos y realiza las operaciones JPA; abre un `EntityManager` por operación y lo cierra después, mientras la factoría se mantiene durante la vida del servlet y se cierra en `destroy`. Las JSP de `WEB-INF/vistas` muestran los datos y escapan el nombre con `<c:out>`. Las escrituras se hacen por POST y terminan en una redirección. La versión web no reproduce `merge`: para editar usa una entidad gestionada, como `actualizar` en la demostración de consola.

Para comparar ambas formas, crea una persona en el navegador, detén Tomcat y ejecuta `DemoJpa`, o viceversa. Ambas usan la misma URL definida en `persistence.xml`, aunque la demostración de consola siempre añade a Ana y Luis. No mantengas Tomcat y la consola abiertos a la vez con la misma base H2 en archivo, porque H2 puede bloquear el acceso concurrente. Tras editar `persistence.xml`, reconstruye y vuelve a desplegar el WAR para que Tomcat lea la nueva configuración.

## Cómo usar MariaDB de XAMPP

1. Inicia **MySQL** en XAMPP y ejecuta [sql/mariadb.sql](sql/mariadb.sql) en phpMyAdmin. Este script crea la base `dwes_09`; Hibernate creará la tabla `personas` al ejecutar el programa.
2. En `src/main/resources/META-INF/persistence.xml`, sustituye las cuatro propiedades JDBC de H2 por estas, cambiando usuario, contraseña y puerto según tu instalación:

   ```xml
   <property name="jakarta.persistence.jdbc.driver" value="org.mariadb.jdbc.Driver"/>
   <property name="jakarta.persistence.jdbc.url" value="jdbc:mariadb://localhost:3306/dwes_09"/>
   <property name="jakarta.persistence.jdbc.user" value="usuario_local"/>
   <property name="jakarta.persistence.jdbc.password" value="contraseña_local"/>
   ```

3. Ejecuta de nuevo `DemoJpa` y comprueba en phpMyAdmin la tabla y los registros. Usa un usuario local con permisos sobre `dwes_09`. La contraseña se escribe solo en tu copia local de `persistence.xml`: no subas esa modificación a Git.

Para volver a H2, restaura en `persistence.xml` las propiedades originales: driver `org.h2.Driver`, URL `jdbc:h2:file:~/wsdwes_09_jpa`, usuario `sa` y contraseña vacía. Cada motor conserva su propia base. Si prefieres otro archivo H2, cambia únicamente el valor de la URL, por ejemplo a `jdbc:h2:file:C:/datos/dwes_09`.

## Cómo comprobar que funciona

- La primera ejecución con una base vacía imprime dos ID distintos, muestra a Ana tras `find`, indica que la actualización y el borrado se realizaron y termina con Ana María y peso `63.0` en el listado. Antes de `commit`, `contains` muestra `false` para la instancia enviada a `merge` y `true` para la devuelta.
- La segunda ejecución genera nuevos ID y conserva la Ana María anterior, porque la base H2 está en archivo.
- En MariaDB, phpMyAdmin muestra la tabla `personas` con columnas `id`, `nombre`, `edad` y `peso`.
- Si se cambia la URL por una base MariaDB inexistente o se detiene el servicio, el arranque falla: JPA necesita una conexión disponible.

## Explicación guiada y diferencias con el original

El ejemplo antiguo usaba `javax.persistence`, JPA 2.1, Hibernate 5, MySQL y metadatos de Eclipse. Aquí se utilizan `jakarta.persistence`, el esquema XML 3.2, Hibernate 7, Maven e IntelliJ. La configuración antigua `drop-and-create` borraba las tablas y los datos en cada arranque; este módulo usa `hibernate.hbm2ddl.auto=update` para conservarlos durante la práctica. Ese ajuste facilita el aprendizaje, pero no sustituye a las migraciones controladas de esquema en una aplicación real.

El original tenía una clase `main` por operación y extensos comentarios junto a cada llamada. `DemoJpa` reúne esas operaciones en un recorrido que usa los ID recién generados, sin suponer que exista el ID 1. También compara una actualización de entidad gestionada con `merge` sobre una entidad separada. El README explica el contexto de persistencia, las transacciones, JPQL y el motivo por el que `remove` requiere una entidad gestionada.

## Errores frecuentes

- Colocar `persistence.xml` fuera de `src/main/resources/META-INF` o escribir otro nombre de unidad de persistencia.
- Importar `javax.persistence` en lugar de `jakarta.persistence`.
- Creer que Tomcat proporciona JPA: en este módulo la dependencia Hibernate se añade explícitamente y se incluye en el WAR.
- Olvidar `commit` o cerrar el `EntityManager` y la factoría.
- Usar `remove` con una entidad recién construida que no está gestionada.
- Escribir `personas` en JPQL como si fuera SQL; la consulta usa el nombre de la clase `Persona`.
- Esperar una base MariaDB creada automáticamente: el script prepara la base antes de que Hibernate cree la tabla.

## Propuestas para practicar

1. Añade una búsqueda por nombre con un parámetro JPQL y prueba un nombre existente e inexistente.
2. Añade un campo `correo` a `Persona` y comprueba cómo se refleja en H2 tras ejecutar el ejemplo.
3. Cambia el peso de Ana dentro de `actualizar` y compara ese recorrido con `fusionar`. Indica qué instancias están gestionadas en cada momento.
