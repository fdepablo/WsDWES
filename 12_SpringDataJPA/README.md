# Spring Data JPA con Spring Boot

Este módulo continúa [`11_SpringBoot`](../11_SpringBoot/README.md): conserva el catálogo de películas, el servicio y la demostración por consola, pero sustituye la lista de `PeliculaRepositorio` por H2 y una interfaz Spring Data JPA. MariaDB es una alternativa opcional. Se inspira en `WorkspaceSpring/13_SpringJPAData`, simplificando su configuración para el entorno actual. Los conceptos de entidades y claves ya aparecen en [`09_JPA`](../09_JPA/README.md).

## Qué vas a aprender

- Distinguir JPA, Hibernate, Spring Data JPA y Spring Boot.
- Mapear `Pelicula` a una tabla con `@Entity`, `@Id` y `@GeneratedValue`.
- Declarar una interfaz que extiende `JpaRepository<Pelicula, Long>` y usar su CRUD sin escribir un DAO concreto.
- Crear consultas mediante el nombre del método y mediante `@Query` con JPQL.
- Tratar una búsqueda ausente con `Optional` y utilizar el ID que devuelve la base.
- Usar H2 por defecto y activar MariaDB mediante un perfil de Spring Boot.
- Observar las operaciones SQL durante la demostración.

## Requisitos y ejecución

Necesitas Java 25, Maven e IntelliJ IDEA; consulta el [README raíz](../README.md). Para la opción predeterminada no hacen falta Tomcat, XAMPP ni una instalación de H2. `spring-boot-starter-data-jpa` reúne Spring Data JPA, Hibernate y la integración JPA; `h2` aporta la base integrada y `mariadb-java-client` permite elegir MariaDB sin cambiar el POM.

Abre el proyecto Maven en IntelliJ y ejecuta `springdatajpa.Aplicacion.main` con JDK 25. Es una aplicación de consola, sin URL que abrir. Primero verás mensajes de arranque y sentencias SQL de Hibernate; después, las líneas encabezadas por `=== Spring Data JPA ===`.

## De `11_SpringBoot` a una base relacional

| Aspecto | `11_SpringBoot` | `12_SpringDataJPA` |
| --- | --- | --- |
| Almacenamiento | `ArrayList` en `PeliculaRepositorio` | Tabla `peliculas` en H2. |
| Repositorio | Clase `@Repository` con métodos escritos a mano | Interfaz `JpaRepository` implementada por Spring Data. |
| Identificador | Contador Java | Columna ID generada por la base. |
| Película | `record` inmutable para datos en memoria | Clase `@Entity` con constructor sin argumentos y propiedades modificables para JPA. |
| Configuración | Límite `catalogo.maximo-peliculas` | H2 por defecto y perfil MariaDB; SQL visible en `application.properties`. |

H2 está configurada **en memoria** (`jdbc:h2:mem:catalogo;DB_CLOSE_ON_EXIT=FALSE`): las películas se guardan en tablas reales mientras vive el proceso, pero desaparecen al cerrarlo. La opción `DB_CLOSE_ON_EXIT=FALSE` deja que Boot controle el cierre de la conexión. Esto permite estudiar JPA sin dejar archivos de base de datos ni necesitar un servidor. `spring.jpa.hibernate.ddl-auto=create-drop` crea el esquema al arrancar y lo elimina al terminar. `spring.jpa.show-sql=true` permite relacionar las operaciones Java con el SQL que ejecuta Hibernate. Este módulo no conserva el límite de dos películas del anterior: aquí interesa observar el CRUD de la base.

### Cambiar a MariaDB (opcional)

La ejecución normal usa H2. Para probar MariaDB con la instalación local habitual de XAMPP:

1. Inicia **MySQL** en XAMPP y crea la base `dwes_12` desde phpMyAdmin; también puedes ejecutar [sql/mariadb.sql](sql/mariadb.sql).
2. En la configuración de ejecución de `springdatajpa.Aplicacion` en IntelliJ, escribe `--spring.profiles.active=mariadb` en **Program arguments** y vuelve a ejecutar.

El perfil carga `application-mariadb.properties`: cambia la URL y el driver JDBC; además usa `ddl-auto=update` para conservar las filas entre ejecuciones. El ejemplo presupone el acceso local habitual de XAMPP (`root` sin contraseña). Si tu instalación usa otros datos de conexión, cambia esas propiedades **solo en tu equipo** y no subas credenciales al repositorio. Para volver a H2, quita el argumento del perfil y reinicia. Las dos bases son independientes.

En MariaDB, la primera ejecución crea las películas de partida y las posteriores las reutilizan por título. Si `Coco` ya cambió a `Familiar`, conserva ese género. La película `Temporal` se crea y se borra para mostrar `deleteById` sin eliminar los datos iniciales. Los IDs proceden de la base y pueden ser distintos en cada instalación; no hay que esperar siempre `1` y `2`.

## Qué aporta cada tecnología

**JPA** define las anotaciones y reglas para mapear entidades Java a tablas; sus imports actuales son `jakarta.persistence.*`. **Hibernate** implementa JPA y genera el SQL. **Spring Data JPA** crea una implementación del repositorio a partir de nuestra interfaz y ofrece métodos de acceso habituales. **Spring Boot** reúne dependencias, configura la conexión, detecta entidades y repositorios bajo el paquete `springdatajpa` y arranca la aplicación. Spring Data JPA no sustituye a JPA: trabaja sobre ella.

En `Pelicula`, `@Entity` marca la clase persistente; `@Table(name = "peliculas")` nombra su tabla; `@Id` señala la clave primaria; `@GeneratedValue(strategy = IDENTITY)` deja que la base asigne el identificador. Antes de `save`, `getId()` es `null`; después se usa el valor devuelto, nunca un ID supuesto. `@Column(nullable = false)` exige valores en las columnas y `unique = true` en `titulo` evita títulos duplicados; el servicio valida entradas con mensajes claros antes de llegar a la base. El constructor sin argumentos protegido permite que JPA cree la entidad; el constructor público facilita crearla desde el servicio. `Pelicula` no lleva `@Component`: cada fila representa un dato, no un bean singleton de Spring.

## La interfaz del repositorio

`PeliculaRepositorio extends JpaRepository<Pelicula, Long>` indica la entidad y el tipo de su ID. No existe una clase `PeliculaRepositorioImpl` escrita por nosotros: Spring Data registra una implementación al arrancar y la inyecta en `PeliculaServicio`. En esta disposición convencional bajo el paquete raíz de Boot no hace falta escribir `@EnableJpaRepositories`. Tampoco se necesita un `persistence.xml` ni declarar manualmente `DataSource` y `EntityManagerFactory`.

| Método | Origen | Qué muestra |
| --- | --- | --- |
| `save`, `findById`, `deleteById` | Heredados de `JpaRepository` | Alta o actualización, búsqueda por clave y borrado. |
| `findAllByOrderByTituloAsc` | Nombre interpretado por Spring Data | Listado ordenado por la propiedad `titulo`. |
| `findByTituloContainingIgnoreCase` | Nombre interpretado por Spring Data | Búsqueda parcial sin distinguir mayúsculas. |
| `findByTituloIgnoreCase` | Nombre interpretado por Spring Data | Evita duplicar las películas iniciales al repetir la demostración en MariaDB. |
| `buscarPorGenero` | `@Query` con JPQL | Consulta escrita por nosotros cuando queremos indicar la condición y el orden expresamente. |

En los nombres derivados, `Titulo` debe coincidir con una propiedad de `Pelicula`; `Containing`, `IgnoreCase` y `OrderByTituloAsc` expresan partes de la consulta. En `@Query("select p from Pelicula p where p.genero = :genero order by p.titulo")`, `Pelicula` y `genero` son la **clase y la propiedad Java**, no la tabla y la columna SQL. `@Param("genero")` enlaza el parámetro del método con `:genero`. Esta es una consulta JPQL; Hibernate la traduce a SQL para H2.

### `Optional`: una búsqueda que puede no encontrar nada

`findById` devuelve `Optional<Pelicula>`, no una `Pelicula` directamente. Es un objeto que representa **uno de dos estados**: contiene una película (`isPresent() == true`) o está vacío (`isEmpty() == true`). Un `Optional` vacío no es una película con campos vacíos, ni un `Optional` que «apunta a `null`». La referencia devuelta por `findById` tampoco debería ser `null`.

En `DemoConsola` se ven los dos casos sin usar API Stream ni expresiones lambda:

```java
Optional<Pelicula> encontrada = servicio.buscarPorId(coco.getId());
if (encontrada.isPresent()) {
    System.out.println(encontrada.get());
}

Optional<Pelicula> inexistente = servicio.buscarPorId(-1L);
if (inexistente.isEmpty()) {
    System.out.println("ID -1: no existe ninguna película");
}
```

`get()` extrae la película, pero **solo debe llamarse después de comprobar que está presente**; sobre un `Optional` vacío lanza `NoSuchElementException`. Si se necesita un valor alternativo, `orElse(...)` permite indicarlo, aunque `orElse(null)` devolvería de nuevo un posible `null` y ocultaría la ventaja pedagógica de `Optional`. Aquí se comprueban los dos estados expresamente.

El servicio también devuelve `Optional<Pelicula>` desde `cambiarGenero`: primero consulta `findById`; si no existe la fila, devuelve `Optional.empty()`. Si existe, modifica el género, llama a `save` y envuelve la película resultante con `Optional.of(...)`. Así quien llama puede distinguir actualización realizada de ID inexistente. `save` sirve para crear y actualizar, pero no significa que sea seguro inventar un ID y asumir que existe una fila: por eso el servicio busca antes de modificar.

## Estructura relevante

```text
12_SpringDataJPA/
├── pom.xml
├── src/main/
    ├── java/springdatajpa/
    │   ├── Aplicacion.java
    │   ├── presentacion/DemoConsola.java
    │   └── modelo/
    │       ├── entidad/Pelicula.java
    │       ├── persistencia/PeliculaRepositorio.java
    │       └── servicio/PeliculaServicio.java
    └── resources/
        ├── application.properties
        └── application-mariadb.properties
└── sql/mariadb.sql
```

## Cómo comprobarlo

1. Al arrancar H2, se crean dos películas y se imprimen sus IDs generados. En MariaDB se crean si faltan y, si ya existen, se reutilizan.
2. La búsqueda del ID de `Coco` imprime la película tras comprobar `isPresent()`; para el ID `-1`, `isEmpty()` muestra «no existe ninguna película».
3. La búsqueda de `CO` encuentra `Coco` aunque el texto esté en mayúsculas.
4. La consulta JPQL por `Ciencia ficción` encuentra `La llegada`.
5. La modificación devuelve un `Optional` presente y se imprime `Coco` con género `Familiar`. Se crea y borra `Temporal`; el listado final contiene `Coco` y `La llegada`, pero no `Temporal`.
6. Ejecuta de nuevo: H2 en memoria parte vacía; MariaDB conserva `Coco` y `La llegada` y la demostración no los duplica.

Busca en la salida SQL las sentencias `insert`, `select`, `update` y `delete`. El orden y formato exactos de las consultas dependen de Hibernate. Si falla H2, comprueba `spring.datasource.url` y la dependencia descargada. Si falla MariaDB, verifica que XAMPP esté iniciado, exista `dwes_12` y el perfil esté activo.

## Qué conservamos y qué corregimos del proyecto antiguo

El original enseña `JpaRepository`, `save`, `findById`, búsquedas derivadas y `@Query`; esos conceptos siguen aquí con menos métodos para poder observar cada uno. Su lista de tecnologías compatibles con Spring Data era una foto de aquella época, no una lista vigente que convenga copiar. La dependencia directa `spring-data-jpa` 2.1.21, Java 8, Hibernate 5, `javax.persistence.*`, la configuración manual y la conexión MySQL con credenciales se sustituyen por Boot 4, Java 25, `jakarta.persistence.*` y H2.

La explicación antigua de `Optional` decía que podía «apuntar a null»: en realidad un `Optional` devuelto por `findById` está **presente** o **vacío**. También se modificaba una película suponiendo el ID `1` y se borraba el ID `2`; aquí se usan los IDs de las entidades guardadas. Los ejemplos no necesitan registrar cada entidad como bean `@Component` ni pedirla al contexto con `getBean`.

## Errores frecuentes y propuestas

- Confundir `@Entity` con `@Service` o `@Repository`: la entidad representa datos; el servicio y el repositorio son beans de la aplicación.
- Buscar una clase de implementación escrita a mano para `PeliculaRepositorio`.
- Escribir nombres de tabla SQL en una consulta JPQL o un nombre de propiedad inexistente en un método derivado.
- Pensar que H2 en memoria conserva datos entre ejecuciones.
- Llamar a `get()` sin comprobar el `Optional`: un ID inexistente no contiene ninguna película.

Para practicar, añade `findByGeneroIgnoreCase` y compáralo con la consulta `@Query`. Después crea una tercera película y comprueba cómo cambia el listado ordenado. Si ya conoces paginación, investiga `Pageable` como ampliación, sin añadirlo al recorrido básico.

## Documentación para ampliar

- [JPA y Spring Data JPA en Spring Boot](https://docs.spring.io/spring-boot/reference/data/sql.html).
- [Conceptos de los repositorios Spring Data](https://docs.spring.io/spring-data/jpa/reference/repositories/core-concepts.html).
- [Consultas derivadas y `@Query`](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html).
