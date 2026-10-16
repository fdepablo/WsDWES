# Primeras operaciones con Jakarta Persistence

Este ejemplo permite crear, consultar, modificar y eliminar personas usando **JPA**, sin escribir las sentencias SQL del CRUD. Tiene una demostración de consola (`DemoJpa`) y una versión web con servlet, servicio y JSP. H2 funciona por defecto en archivo; MariaDB de XAMPP es una alternativa.

**Recorrido recomendado:** empieza por `DemoJpa` sin Tomcat y sigue las operaciones básicas. Después prueba la aplicación web.

## Qué vas a aprender

- Relacionar la clase `Persona` con la tabla `personas` mediante anotaciones JPA.
- Configurar la conexión en `persistence.xml` y reconocer la unidad de persistencia `PersonasJPA`.
- Utilizar `EntityManager` para crear, buscar, listar, modificar y eliminar personas.
- Confirmar o deshacer las escrituras mediante transacciones.
- Cerrar los recursos al terminar.
- Reutilizar las operaciones JPA desde un servlet y mostrar los datos en JSP.

## Conceptos clave para empezar

### De JDBC a JPA

En JDBC escribíamos SQL y convertíamos cada fila de `ResultSet` en un objeto. Con **JPA**, indicamos cómo se relacionan las clases Java y las tablas y trabajamos con objetos. Esta relación se llama **mapeo objeto-relacional**, u ORM. Un ORM es capaz de crear consultas, crear tablas y convertir filas a objetos de manera transparente.

**JPA es la especificación; Hibernate es la implementación** que utilizamos. Hibernate genera y ejecuta el SQL: la base de datos relacional sigue existiendo, aunque no escribamos cada `INSERT`, `UPDATE` o `DELETE` a mano.

En este ejemplo, `Persona` tiene **id, nombre, edad y peso**. La clase representa los datos y sus anotaciones indican cómo guardarlos.

### Anotaciones de Persona

Una **anotación** en Java es una marca que empieza por `@` y añade información sobre una clase, un campo o un método. El compilador o una librería pueden interpretar esa información. Por ejemplo, ya hemos utilizado `@WebServlet` para indicar la URL de un servlet; ahora Hibernate interpreta las anotaciones de JPA para saber cómo guardar nuestros objetos en la base de datos.

Algunas anotaciones admiten parámetros: en `@Table(name = "personas")`, el parámetro `name` indica el nombre de la tabla.

| Anotación | Qué indica | En este módulo |
| --- | --- | --- |
| `@Entity` | La clase tiene identidad persistente y JPA la gestiona. | `Persona` se convierte en entidad. |
| `@Table(name = "personas")` | Nombre de la tabla; no dependemos del nombre elegido por defecto. | La tabla se llama `personas`. |
| `@Id` | Propiedad que identifica de forma única una fila. | `id` es la clave primaria. |
| `@GeneratedValue(strategy = IDENTITY)` | La base genera el identificador al insertar. | No asignamos el ID al construir `Persona`. |
| `@Column(nullable = false, length = 80)` | Ajusta el mapeo y las restricciones de una columna. | `nombre` no admite `NULL` y tiene longitud máxima de 80. |


`edad` y `peso` se guardan aunque no tengan `@Column`. La entidad necesita un constructor sin argumentos para que JPA pueda crear sus objetos al leer de la base de datos.

### Configuración, factoría y gestor

| Elemento | Para qué sirve en este ejemplo |
| --- | --- |
| `persistence.xml` | Define la entidad, el proveedor y la conexión a la base de datos. Está en `src/main/resources/META-INF`. |
| `PersonasJPA` | Es el nombre de la unidad de persistencia: identifica la configuración que queremos utilizar. |
| `EntityManagerFactory` | Crea los `EntityManager`. Se crea una vez al iniciar el programa o servicio y se cierra al finalizar. |
| `EntityManager` | Es el gestor de entidades: permite realizar las operaciones de persistencia con ellas. En este ejemplo, se abre uno por operación y se cierra al terminar. |

La factoría a partir de la unidad de persistencia, y se crea con el nombre que figura en `persistence.xml`:

```java
EntityManagerFactory factoria = Persistence.createEntityManagerFactory("PersonasJPA");
```

Para realizar una operación, obtenemos un EntityManager:

```java
EntityManager gestor = factoria.createEntityManager();
```

Como norma práctica, **compartimos la factoría, pero no un mismo gestor entre peticiones**. En `PersonaServicio` se puede ver cómo cada método abre y cierra el suyo.


### Fichero `persistence.xml`

Este fichero reúne la configuración de JPA y se encuentra en `src/main/resources/META-INF/persistence.xml`. Sus elementos más importantes en este ejemplo son:

| Elemento | Qué configura |
| --- | --- |
| `<persistence-unit name="PersonasJPA">` | Define una unidad de persistencia: una configuración identificada por el nombre que usamos al crear la factoría. |
| `transaction-type="RESOURCE_LOCAL"` | Indica que controlamos las transacciones desde nuestro código mediante `EntityTransaction`. |
| `<provider>` | Selecciona Hibernate como implementación de JPA. |
| `<class>` | Declara la entidad `modelo.entidad.Persona` que vamos a guardar y consultar. |
| Propiedades `jakarta.persistence.jdbc.*` | Indican el driver, la URL, el usuario y la contraseña para conectarse a la base de datos. Aquí utilizamos H2 en archivo. |
| `hibernate.hbm2ddl.auto` | Decide cómo se crean o actualizan las tablas a partir de las entidades. |

#### Creación y actualización de las tablas

La propiedad `hibernate.hbm2ddl.auto` de `persistence.xml` indica qué hace Hibernate con el **esquema** (tablas, columnas y restricciones) al crear la factoría:

| Valor | Comportamiento | Cuándo utilizarlo |
| --- | --- | --- |
| `none` | No crea, modifica ni valida el esquema. | Cuando las tablas se gestionan por otro medio. |
| `validate` | Comprueba que el esquema sea compatible con las entidades; si encuentra diferencias incompatibles, falla el arranque. No modifica las tablas. | Para detectar desajustes en un esquema ya creado. |
| `update` | Crea las tablas que faltan e intenta adaptar las existentes, conservando los datos. No resuelve todos los cambios posibles. | Durante estas prácticas, para mantener los datos entre ejecuciones. |
| `create` | Elimina y vuelve a crear las tablas mapeadas al arrancar. **Se pierden sus datos anteriores.** | Para pruebas que deban empezar con tablas vacías. |
| `create-drop` | Hace lo mismo que `create` y elimina las tablas al cerrar la factoría. | Para pruebas con datos temporales. |

Para comenzar con `DemoJpa`, usamos:

```xml
<property name="hibernate.hbm2ddl.auto" value="create"/>
```

**`create` resulta útil para repetir la demostración desde cero**: al ejecutar `DemoJpa` se recrean las tablas y después el programa inserta a Ana y Luis. Con `create-drop`, las tablas también desaparecen al finalizar el programa y cerrarse la factoría.

Esta configuración es propia de Hibernate y es distinta de las transacciones del CRUD. En una aplicación real se utilizarían migraciones controladas para evolucionar el esquema. Consulta la [referencia oficial de las acciones de esquema de Hibernate](https://docs.hibernate.org/stable/orm/javadocs/org/hibernate/tool/schema/Action.html).

### Las operaciones básicas

| Operación | Cómo se hace con JPA |
| --- | --- |
| Crear | Construir una `Persona` y llamar a `gestor.persist(persona)`. La base genera su ID. |
| Buscar por ID | `gestor.find(Persona.class, id)`. Devuelve la persona o `null` si no existe. |
| Listar | Ejecutar una consulta JPQL y obtener una `List<Persona>`. |
| Modificar | Buscar con `find`, cambiar los campos mediante setters y confirmar la transacción con `commit()`. Como la entidad sigue gestionada por el mismo `EntityManager`, JPA guarda automáticamente los cambios. Si modificamos una entidad cuyo gestor ya se ha cerrado, tendremos que usar `merge()` con un nuevo gestor y confirmar su transacción para guardar esos cambios. |
| Eliminar | Buscar con `find` y llamar a `gestor.remove(persona)` con ese mismo gestor. |

### Consultar con JPQL

**JPQL (Jakarta Persistence Query Language)** es el lenguaje de consultas de JPA. Su sintaxis se parece a SQL, pero trabaja con entidades y propiedades Java. En este ejemplo Hibernate traduce la consulta a SQL para ejecutarla en la base de datos.

Para listar todas las personas ordenadas por su identificador, utilizamos:

```java
List<Persona> personas = gestor.createQuery(
        "select p from Persona p order by p.id", Persona.class).getResultList();
```

Podemos leer la consulta por partes:

| Parte | Qué significa |
| --- | --- |
| `select p` | Devuelve los objetos `Persona` completos, no solo uno de sus campos. |
| `from Persona p` | Consulta la entidad `Persona` y le asigna el alias `p`, un nombre corto que usamos en el resto de la consulta. |
| `order by p.id` | Ordena los resultados por la propiedad `id`, de menor a mayor por defecto. |

Usamos **`Persona`, el nombre de la entidad**, en lugar de `personas`, el nombre de la tabla. Del mismo modo, `p.id` se refiere a una propiedad Java.

En la llamada Java, `createQuery(..., Persona.class)` crea una consulta cuyo resultado contiene objetos `Persona`. **`getResultList()` ejecuta la consulta** y devuelve una `List<Persona>`: estará vacía si no hay personas. Podemos recorrerla con un bucle o mostrarla en una JSP.

Para consultar más ejemplos, puedes acudir al [tutorial oficial de JPQL de Jakarta EE](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-querylanguage/persistence-querylanguage.html). La [especificación de Jakarta Persistence 3.2](https://jakarta.ee/specifications/persistence/3.2/jakarta-persistence-spec-3.2.html) sirve como referencia más completa.

### Transacciones y cierre de recursos

En este ejemplo utilizamos transacciones en todas las operaciones, tanto en las consultas como en las escrituras. Así delimitamos el inicio y el final de cada operación con la base de datos. Una transacción puede agrupar varias operaciones de escritura de forma **atómica**: o se confirman todos sus cambios o no se conserva ninguno.

Para controlar la transacción, obtenemos un objeto `EntityTransaction` a partir del `EntityManager`:

```java
EntityTransaction transaccion = gestor.getTransaction();
```

Este objeto permite controlar sus pasos:

1. **`begin()`** inicia la transacción.
2. Realizamos la operación: consultar, insertar, modificar o eliminar.
3. **`commit()`** finaliza la transacción y confirma los cambios, si los hay.
4. Si la operación falla y la transacción sigue activa, **`rollback()`** la deshace.

`try-with-resources` cierra automáticamente el gestor, incluso si ocurre un error; `finally` ejecuta `rollback()` si la transacción sigue activa.

La factoría se mantiene mientras se utiliza el programa o servicio; en `DemoJpa` se cierra con `try-with-resources` y en la aplicación web desde `destroy()` del servlet.

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

## Paso 1. Ejecutar la demostración de consola

En IntelliJ, recarga Maven y ejecuta `main.DemoJpa.main()` usando el módulo `09_JPA`. No necesitas Tomcat. H2 guarda la base en `~/wsdwes_09_jpa.mv.db`, dentro de la carpeta personal del usuario. Aunque el archivo permanece, con `create` las tablas se recrean y sus datos anteriores se pierden en cada ejecución.

Sigue este recorrido:

1. `crear` guarda a Ana y Luis y muestra los ID generados.
2. `buscar` obtiene a Ana por su ID.
3. `actualizarConFind` cambia su nombre a Ana María y su edad a 26 mediante setters, con el mismo gestor que la busca. El programa vuelve a consultar para mostrar el resultado.
4. El programa vuelve a buscar a Ana y cierra ese gestor. Cambia el nombre del objeto a Ana López y la edad a 27; `actualizarConMerge` guarda esos cambios con un nuevo gestor. Otra consulta muestra el resultado. Comparamos así la modificación de los mismos campos con ambos enfoques; sus detalles internos se explican en la ampliación final.
5. `listar` consulta las personas mediante JPQL.
6. `borrar` elimina a Luis y el último listado permite comprobar el resultado.

**Nota:** con `create`, cada ejecución empieza desde cero y no acumula personas de ejecuciones anteriores.

## Paso 2. Ejecutar la versión web

Antes de pasar a la web, cambia manualmente `create` por `update` en `src/main/resources/META-INF/persistence.xml`:

```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

De esta manera no perderemos los datos cada vez que arranquemos el Tomcat.

Despliega `09_JPA/target/09_JPA-1.0-SNAPSHOT.war` en Tomcat 11 y vamos a `\index.html`.

1. `PersonaServlet` recibe la petición y llama a `PersonaServicio`.
2. El servicio valida los datos y utiliza JPA para consultar o modificar las personas.
3. El servlet entrega los datos a las JSP de `WEB-INF/vistas` mediante `forward`.
4. Tras guardar o eliminar correctamente, redirige al listado para no repetir la escritura al actualizar.

Las JSP muestran los datos con `c:out`. Para editar, el servicio utiliza `find`, setters y una transacción; la web no necesita la demostración adicional de `merge`. La factoría se cierra desde `destroy()` del servlet.

Consola y web utilizan la misma base definida en `persistence.xml`. Para compararlas, detén una antes de ejecutar la otra: H2 en archivo puede bloquear el acceso desde otro proceso. Después de modificar `persistence.xml`, reconstruye y vuelve a desplegar el WAR.

## Cómo comprobar que funciona

1. Ejecuta `DemoJpa`: aparecen dos ID distintos, se consulta y modifica a Ana con ambos enfoques y se elimina a Luis. Al final aparece Ana López, con edad 27 y peso 62.5.
2. Vuelve a ejecutarlo: aparecen nuevos ID y se conservan los datos anteriores.
3. En la web, crea una persona, edítala y elimínala comprobando los cambios en el listado.
4. Actualiza el listado después de guardar: no debe repetirse la inserción.
5. Reinicia Tomcat y comprueba que las personas que no has eliminado permanecen.

## Errores frecuentes

- Colocar `persistence.xml` fuera de `src/main/resources/META-INF` o usar un nombre distinto de `PersonasJPA` al crear la factoría.
- Importar `javax.persistence` en lugar de `jakarta.persistence`.
- Suponer que Tomcat proporciona JPA: Hibernate es una dependencia del módulo y se incluye en el WAR.
- Olvidar `commit()` o dejar abiertos el gestor y la factoría.
- Escribir el nombre de la tabla en JPQL: se utiliza la entidad `Persona`.
- Abrir a la vez la consola y Tomcat con el mismo archivo H2.

### `NoClassDefFoundError: org/jboss/logging/BasicLogger` al ejecutar DemoJpa

Este error indica que falta una librería al ejecutar el programa. Hibernate necesita otras librerías, como JBoss Logging, que Maven incorpora automáticamente: son sus **dependencias transitivas**. Si IntelliJ no ha sincronizado correctamente Maven, puede ejecutar la clase sin incluirlas.

1. Abre la ventana **Maven** de IntelliJ y pulsa **Reload All Maven Projects**.
2. Espera a que termine la sincronización y comprueba que no haya errores de descarga.
3. Ejecuta de nuevo `DemoJpa`.

No es necesario añadir JBoss Logging manualmente al `pom.xml`: ya está declarada como dependencia de Hibernate.

## Alternativa con MariaDB de XAMPP

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

## Propuestas para practicar

1. Añade una búsqueda por nombre con un parámetro JPQL y prueba un nombre existente e inexistente.
2. Añade un campo `correo` a `Persona` y comprueba cómo se refleja en la tabla tras ejecutar el ejemplo.

## Ampliación opcional sobre el funcionamiento interno

**Esta parte no es necesaria para realizar el CRUD ni para seguir la primera explicación de clase.** Puede utilizarse más adelante para estudiar la demostración de `merge` y entender con más detalle cómo trabaja JPA.

### Contexto de persistencia y caché de primer nivel


Cada gestor de entidades mantiene su propio **contexto de persistencia**: las entidades obtenidas con `find` quedan gestionadas mientras ese gestor está abierto. En `actualizarConFind`, se busca una `Persona` gestionada y se cambian sus propiedades; al confirmar la transacción, Hibernate sincroniza esos cambios con la base. No hace falta llamar a `merge` en ese caso.

El contexto actúa como una caché de primer nivel **propia de ese gestor**: si buscamos dos veces la misma clave dentro del mismo contexto, recibimos la misma instancia gestionada. Eso no significa que todos los gestores compartan los mismos objetos ni que cualquier cambio ya esté confirmado en la base. Mientras la entidad está gestionada, el proveedor detecta los cambios de sus atributos; al sincronizar el contexto puede emitir SQL. Al cerrar el gestor, los objetos que conservamos dejan de estar gestionados. `contains(objeto)` permite comprobarlo mientras el gestor está abierto.

El programa vuelve a buscar a Ana y cierra ese gestor. La instancia devuelta queda **separada** del contexto. Tras cambiar su nombre y edad, `actualizarConMerge` llama a `merge`: JPA copia sus valores a una instancia gestionada y devuelve esta última. Las dos llamadas a `contains` muestran `false` para el objeto enviado y `true` para el devuelto antes de cerrar el nuevo gestor. Para evitar inserciones inesperadas, se hace `merge` con un ID que acabamos de consultar y sabemos que existe.

Todas las operaciones usan `begin` y `commit`, incluidas las lecturas. `remove` recibe una entidad gestionada, por eso primero se obtiene mediante `find`.

La consulta `select p from Persona p order by p.id` es **JPQL**: menciona la clase `Persona` y sus propiedades, no la tabla SQL `personas`. Hibernate genera el SQL correspondiente para H2 o MariaDB. El identificador se asigna al guardar porque `id` usa `GenerationType.IDENTITY`.

### Detalles de las operaciones y merge


1. **`persist` para crear.** `crear` recibe una `Persona` nueva, inicia una transacción y llama a `gestor.persist(persona)`. La instancia pasa a estar gestionada. `commit` confirma la operación; el ID generado se devuelve después. No presupongas que el `INSERT` sucede siempre exactamente en `commit`: con `IDENTITY`, el proveedor puede necesitar insertarla antes para obtener el ID. Por eso distinguimos *hacer gestionada* una entidad de *confirmar* la transacción.
2. **`find` para buscar.** `buscar` llama a `find(Persona.class, id)`. Devuelve la entidad gestionada dentro de ese gestor o `null` si la clave no existe. Como el método cierra el gestor antes de devolverla, quien recibe el objeto obtiene una entidad **separada**.
3. **Modificar una entidad gestionada.** `actualizarConFind` abre transacción, busca a Ana y cambia `nombre` y `edad`. No hay una llamada explícita a `UPDATE` ni a `merge`: Hibernate detecta los cambios y los sincroniza al confirmar. La clave primaria identifica a la entidad y no debe modificarse como si fuera un dato editable.
4. **`merge` para copiar cambios de una entidad separada.** `actualizarConMerge` recibe la instancia devuelta por otra búsqueda ya cerrada. `merge` copia su estado a una **instancia gestionada y devuelve esa instancia**. El objeto pasado sigue separado: `contains(separada)` es `false` y `contains(gestionada)` es `true`. Si quisiéramos seguir cambiando valores dentro de la transacción, usaríamos la referencia devuelta, no la enviada. No es un «UPDATE garantizado»: usar `merge` indiscriminadamente con IDs inexistentes o entidades nuevas puede provocar inserciones o errores según el caso y el proveedor. Aquí conocemos el ID porque acabamos de crear y buscar a Ana.
5. **JPQL para listar.** `listar` usa `createQuery(..., Persona.class)` para obtener una `List<Persona>` con una consulta tipada. `Persona` en JPQL es el nombre de la entidad; `p.id` es una propiedad Java. No escribimos el nombre físico de la tabla ni concatenamos entradas del usuario en la consulta.
6. **`remove` para borrar.** `borrar` busca primero a Luis **con el mismo gestor** que ejecutará `remove`. Así entrega una entidad gestionada. Pasar a `remove` un objeto separado o recién construido no equivale a borrar la fila con ese ID y puede producir una excepción. Si `find` devuelve `null`, el método indica que no había nada que borrar.

### Otras anotaciones para consultar más adelante

Otras anotaciones que no se utilizan en este módulo: 

- `@Transient` excluye una propiedad del mapeo.
- `@Enumerated` indica cómo guardar un `enum` (por ejemplo, como texto con `EnumType.STRING`).
- `@Temporal` se empleaba para precisar fecha, hora o ambas al mapear `java.util.Date` o `Calendar`. Si usamos tipos modernos como `LocalDate` o `LocalDateTime`, no necesitamos `@Temporal`. 
- Las anotaciones de relaciones (`@OneToOne`, `@ManyToOne`, etc.) se estudian en `10_JPARelaciones`.
