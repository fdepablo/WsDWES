# Relaciones unidireccionales con JPA

Este módulo adapta el antiguo `33_JPARelaciones` para aprender a relacionar entidades sin mantener referencias en ambos sentidos. Es una aplicación de consola: primero observa cómo se guardan los objetos y después comprueba qué tablas y claves crea JPA. H2 funciona por defecto; MariaDB de XAMPP se activa cambiando `persistence.xml`.

## Qué vas a aprender

- Incluir los campos de un objeto en la tabla de otro con `@Embeddable` y `@Embedded`.
- Relacionar un cliente con sus datos bancarios mediante `@OneToOne`.
- Representar uno-a-muchos desde el lado de cada pedido con `@ManyToOne`.
- Relacionar clientes y comerciales con `@ManyToMany` y una tabla intermedia.
- Distinguir una clave foránea de una tabla intermedia y evitar cascadas sobre entidades compartidas.
- Consultar los pedidos de un cliente con JPQL sin añadir una colección inversa.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Haber trabajado `09_JPA`, especialmente `persist`, `find`, transacciones, JPQL y el ciclo de vida de `EntityManager`.
- No hace falta Tomcat: este módulo se ejecuta por consola.
- Para la opción MariaDB: XAMPP con el servicio **MySQL** iniciado y acceso a phpMyAdmin.

## Relaciones del ejemplo

| Concepto | Dirección en Java | Anotación | Resultado en la base |
| --- | --- | --- | --- |
| Objeto embebido | `Cliente → Direccion` | `@Embedded` / `@Embeddable` | `calle` y `ciudad` son columnas de `clientes`; no existe tabla `direcciones`. |
| Uno a uno | `Cliente → DatosBancarios` | `@OneToOne` y `@JoinColumn` | `clientes.datos_bancarios_id` es una FK única hacia `datos_bancarios`. |
| Uno a muchos | `Pedido → Cliente` | `@ManyToOne` y `@JoinColumn` | Cada fila de `pedidos` lleva `cliente_id`. Varios pedidos pueden tener el mismo cliente. |
| Muchos a muchos | `Cliente → Comercial` | `@ManyToMany` y `@JoinTable` | `clientes_comerciales` guarda las dos FK. |

**Unidireccional** significa que solo una clase tiene una referencia Java a la otra. No hay `mappedBy` ni hay que sincronizar dos atributos de los objetos. `Cliente` no tiene `List<Pedido>`: para encontrar sus pedidos se consulta `Pedido` con JPQL. `DatosBancarios` y `Comercial` tampoco guardan una referencia de vuelta al cliente. La relación de uno a muchos existe en la base aunque en Java naveguemos únicamente de cada pedido a su cliente.

La dirección es una excepción a esa idea de relación: `Direccion` es un objeto Java, pero **no es una entidad ni tiene identidad propia en la base**. Se incluye en la tabla `clientes`.

### 1. Dirección embebida: un objeto sin tabla propia

En el original se explicaba que `@Embedded` permite agrupar datos de dirección sin crear otra tabla. Aquí `Direccion` lleva `@Embeddable` y `Cliente.direccion` lleva `@Embedded`. Al hacer `persist(cliente)`, JPA guarda `nombre`, `calle` y `ciudad` en una sola fila de `clientes`. No se llama a `persist(direccion)` porque una dirección embebida no tiene ID ni ciclo de vida independiente. Si necesitásemos compartir una misma dirección entre varios clientes o consultarla por separado, habría que modelarla como entidad; no es el objetivo de esta primera práctica.

El código antiguo también usaba `java.util.Date` con `@Temporal`: `DATE` conserva la fecha, `TIME` solo la hora y `TIMESTAMP` fecha y hora. Esa explicación se mantiene aquí como contexto, pero no como código: en el ejemplo nuevo `Pedido.fecha` es `LocalDateTime` y JPA lo mapea sin `@Temporal`. Si añadieses una fecha de nacimiento a `Cliente`, usarías `LocalDate` para representar solo día, mes y año.

### 2. Uno a uno: cliente y datos bancarios

`Cliente` tiene un atributo `DatosBancarios` marcado con `@OneToOne`; `DatosBancarios` no tiene un atributo `Cliente`. Podemos recorrer `cliente.getDatosBancarios()`, pero no hacer el recorrido inverso directamente. `@JoinColumn(name = "datos_bancarios_id", unique = true)` sitúa la clave foránea en `clientes`: apunta al ID de `datos_bancarios`, y la restricción de unicidad impide que dos clientes apunten a la misma fila. En una relación uno a uno la FK podría situarse en cualquiera de las dos tablas; esta es la dirección que hemos elegido. En el proyecto antiguo estaba en `datos_bancarios`, por eso no debes buscar allí `fk_id_cliente` al inspeccionar este módulo.

El ID de ambas entidades lo genera la base mediante `GenerationType.IDENTITY`. Antes de persistir, `cliente.getId()` es `null`; tras guardar y confirmar ya podemos usar el valor generado, sin suponer que sea 1. Se crea primero el objeto `DatosBancarios`, se asigna al cliente y se llama únicamente a `persist(cliente)`. Funciona por `cascade = CascadeType.PERSIST`, que propaga **el alta** desde el cliente a sus datos bancarios.

La cascada mostrada cubre el alta inicial. Si los datos bancarios llegan más tarde, cuando el cliente ya existe, no basta con asignar un objeto nuevo y esperar que `PERSIST` se repita: habría que abrir una transacción, buscar el cliente gestionado, persistir explícitamente los datos bancarios nuevos, asignarlos al cliente y confirmar. Este caso aparecía en el ejemplo antiguo, pero aquí no se mezcla con la primera demostración.

### 3. Uno a muchos: varios pedidos de un cliente

Cada `Pedido` tiene un atributo `Cliente` con `@ManyToOne(optional = false)` y `@JoinColumn(name = "cliente_id", nullable = false)`. La columna `cliente_id` aparece en `pedidos`: varias filas pueden contener el mismo ID de cliente. El lado «muchos» conserva la FK; por eso basta con esta anotación para representar la relación en este diseño. No hemos puesto `@OneToMany` en `Cliente`. Esa anotación sí puede utilizarse en otros diseños, pero añadirla aquí crearía una segunda forma de navegar que no necesitamos.

No hay cascada desde `Pedido` hacia `Cliente`: eliminar un pedido no debe eliminar al cliente ni a los demás pedidos. `crearPedidos` busca primero un cliente ya guardado y después persiste cada pedido asociado. Si intentas guardar un pedido sin cliente, la relación obligatoria y la columna no nula provocarán un error. Para ir en sentido contrario —obtener todos los pedidos de un cliente— usamos JPQL:

```java
select p from Pedido p where p.cliente.id = :id order by p.id
```

`Pedido` y `cliente` son nombres de clase y propiedad Java, no nombres de tabla ni columna SQL. `:id` es un parámetro: se le asigna el ID generado en esta ejecución con `setParameter`. Así evitamos escribir una SQL distinta para H2 y MariaDB y no dependemos de IDs fijos.

### 4. Muchos a muchos: clientes y comerciales

Un cliente puede tener varios comerciales y un comercial puede atender a varios clientes. `Cliente.comerciales` lleva `@ManyToMany`; `Comercial` no tiene una lista de clientes. `@JoinTable` indica el nombre `clientes_comerciales` para la tabla intermedia. Dentro de esa anotación, `joinColumns = @JoinColumn(name = "cliente_id")` identifica la FK del lado donde está el atributo (`Cliente`), mientras que `inverseJoinColumns = @JoinColumn(name = "comercial_id")` identifica la FK hacia `Comercial`. «Inverse» aquí nombra la otra columna de la tabla; **no implica que exista una propiedad Java inversa**.

En `asignarComerciales`, primero se persisten Lucía y Mario, luego se añaden a `cliente.getComerciales()` mientras el cliente está gestionado. Al confirmar, JPA inserta las asociaciones en la tabla intermedia. Añadir un comercial nuevo a esa lista **no lo guarda automáticamente**, porque no hay `CascadeType.PERSIST` en `@ManyToMany`. Esta decisión permite asociar después un comercial existente a otro cliente sin duplicar su fila. El orden exacto de los `INSERT` lo decide el proveedor JPA; no se debe deducir el ID de un objeto por el orden en que aparece en el código.

La tabla intermedia generada por `@ManyToMany` solo representa la asociación. Si la relación necesitase datos propios —por ejemplo, fecha de asignación o comisión— sería mejor crear una entidad `AsignacionComercial` para esa tabla y relacionarla con `Cliente` y `Comercial`. Eso se deja fuera de este módulo para mantener clara la idea principal.

### Cascadas: qué se propaga y qué no

Una cascada no es una dirección de navegación: indica qué operación JPA se propaga a los objetos referenciados. En este ejemplo:

| Asociación | Cascada usada | Consecuencia |
| --- | --- | --- |
| `Cliente → DatosBancarios` | `PERSIST` | Dar de alta un cliente nuevo también guarda sus datos bancarios nuevos. No se propaga el borrado. |
| `Pedido → Cliente` | Ninguna | Cada pedido se guarda por separado y nunca borra a su cliente por cascada. |
| `Cliente → Comercial` | Ninguna | Los comerciales se guardan antes y no se borran al actuar sobre un cliente. |

El código antiguo comentaba `CascadeType.ALL` —propaga entre otras operaciones `PERSIST` y `REMOVE`— y `CascadeType.REMOVE` —propaga el borrado—. Son útiles solo si el ciclo de vida de ambos objetos debe ir unido. Aplicar `ALL` a `Pedido → Cliente` podría borrar al cliente al borrar un pedido; aplicarlo a `Cliente → Comercial` podría borrar un comercial que atiende a otros clientes. Por eso este ejemplo no lo hace. Tampoco añade un borrado en cascada de cliente a pedidos: al existir pedidos asociados, la FK puede impedir borrar el cliente hasta resolver esas relaciones explícitamente.

### Carga de relaciones y `EntityManager`

El original distinguía carga **inmediata** (`EAGER`) y **diferida** (`LAZY`). Con `EAGER`, la relación debe quedar disponible al obtener la entidad; esto no significa necesariamente que se use una sola consulta SQL. Con `LAZY`, el proveedor puede esperar hasta que accedamos a la relación para traerla. Por defecto, `@OneToOne` y `@ManyToOne` son `EAGER`, mientras que `@ManyToMany` es `LAZY`. En este módulo no cambiamos esos valores para no añadir configuración extra.

`consultar` accede a `cliente.getComerciales()` **antes de cerrar** el `EntityManager`; así puede cargarse la colección diferida. Si se cerrase primero y la colección todavía no estuviera cargada, Hibernate lanzaría un error al intentar recorrerla. La consulta de pedidos también se ejecuta con el gestor abierto. Evita la idea de que `find(cliente)` trae siempre, y de una vez, todo el grafo de objetos: el momento y número de consultas depende de la estrategia de carga y del proveedor.

La cuenta `CUENTA-DEMO` es ficticia; no utilices datos bancarios reales en este ejercicio.

## Estructura relevante

```text
10_JPARelaciones/
├── pom.xml
├── sql/mariadb.sql
└── src/main/
    ├── java/
    │   ├── main/DemoRelaciones.java
    │   └── modelo/entidad/{Cliente,Direccion,DatosBancarios,Pedido,Comercial}.java
    └── resources/META-INF/persistence.xml
```

## Cómo ejecutar con H2

Desde la raíz del workspace, compila con `mvn -pl 10_JPARelaciones -am clean package`. En IntelliJ, recarga Maven y ejecuta `main.DemoRelaciones.main()`. La configuración incluida usa `jdbc:h2:file:~/wsdwes_10_relaciones`; el archivo `wsdwes_10_relaciones.mv.db` se crea en la carpeta personal del usuario. La unidad `RelacionesPU` usa Hibernate y `hibernate.hbm2ddl.auto=update`: crea o adapta las tablas para la práctica y conserva los datos de ejecuciones anteriores. No es una estrategia de migración de esquemas para producción.

`DemoRelaciones` avanza en cuatro pasos: crea un cliente con dirección y datos bancarios, añade dos pedidos, asocia dos comerciales y vuelve a consultar. Usa el ID generado en el primer paso, sin suponer que sea 1. Cada método abre su propio `EntityManager`; las escrituras comienzan una transacción, confirman y revierten si sigue activa al salir por un error. Las colecciones de `@ManyToMany` se recorren antes de cerrar el gestor, porque pueden cargarse de forma diferida.

La salida debe mostrar el ID del cliente, dos pedidos (`PED-A` y `PED-B`), los nombres de los dos comerciales, la ciudad y el banco. Al repetir la ejecución se añaden registros nuevos y se conservan los anteriores. En H2 puedes inspeccionar las tablas con la consola de H2 si la tienes configurada, o cambiar temporalmente a MariaDB y verlas en phpMyAdmin.

## Cómo usar MariaDB de XAMPP

1. Inicia **MySQL** en XAMPP y ejecuta [sql/mariadb.sql](sql/mariadb.sql) desde phpMyAdmin para crear la base `dwes_10`.
2. En `src/main/resources/META-INF/persistence.xml`, sustituye las cuatro propiedades JDBC de H2 por estas, ajustando puerto, usuario y contraseña a tu instalación:

   ```xml
   <property name="jakarta.persistence.jdbc.driver" value="org.mariadb.jdbc.Driver"/>
   <property name="jakarta.persistence.jdbc.url" value="jdbc:mariadb://localhost:3306/dwes_10"/>
   <property name="jakarta.persistence.jdbc.user" value="usuario_local"/>
   <property name="jakarta.persistence.jdbc.password" value="contraseña_local"/>
   ```

3. Ejecuta `DemoRelaciones` y comprueba en phpMyAdmin las tablas `clientes`, `datos_bancarios`, `pedidos`, `comerciales` y `clientes_comerciales`. Verifica las FK: los dos pedidos y las dos filas de la tabla intermedia deben apuntar al cliente creado en esa ejecución.

No subas a Git tu `persistence.xml` si has escrito en él una contraseña real. Para volver a H2 restaura driver `org.h2.Driver`, URL `jdbc:h2:file:~/wsdwes_10_relaciones`, usuario `sa` y contraseña vacía. Cada motor mantiene sus propios datos.

## Explicación guiada y diferencias con el original

El proyecto antiguo utilizaba `javax.persistence`, JPA 2.1, Hibernate 5, MySQL y ejemplos con referencias bidireccionales. Aquí usamos Jakarta Persistence 3.2, Hibernate 7, Java 25 y Maven. Se han eliminado las referencias de vuelta, `mappedBy`, los ID fijos y los grandes bloques de comentarios. También se usan `LocalDateTime` en lugar de `Date` y una cuenta ficticia como texto, en vez de guardar un número de cuenta en un `Integer`.

En una relación bidireccional como la antigua `Cliente ↔ Pedido`, las dos clases tenían atributos para navegar en ambos sentidos. El lado que no llevaba la FK usaba `mappedBy` para señalar el atributo del otro lado, y el programa tenía que mantener coherentes las referencias Java (`pedido.setCliente(cliente)` y añadir el pedido a la lista del cliente). Omitir una de ellas podía dar una imagen inconsistente en memoria. Es un tema importante para más adelante, pero no hace falta para comprender cómo se almacenan las FK: por eso aquí se conserva solo `Pedido → Cliente`. Del mismo modo, ya no hay que asignar dos referencias para `Cliente → DatosBancarios` ni mantener dos colecciones para clientes y comerciales.

El ejemplo antiguo proponía borrar y recrear las tablas en cada ejecución. Aquí se usa una base distinta de `09_JPA` y `update` para poder repetir las pruebas sin borrar datos. Al no tener colecciones inversas, consultar los pedidos requiere `select p from Pedido p where p.cliente.id = :id`: JPQL recorre la propiedad `cliente` de cada `Pedido`. La tabla intermedia de muchos a muchos la genera JPA a partir de `@JoinTable`; no hace falta una entidad Java para este caso sencillo.

## Errores frecuentes

- Esperar una tabla `direcciones`: `Direccion` es embebida, no entidad.
- Añadir un pedido sin cliente: `cliente_id` no admite `null`.
- Persistir un comercial nuevo solo por añadirlo a `cliente.getComerciales()`: esta relación no tiene cascada; hay que persistirlo antes.
- Acceder a `cliente.getComerciales()` después de cerrar el `EntityManager` sin haber cargado la colección.
- Ejecutar contra MariaDB antes de crear la base o con un usuario sin permisos.
- Confundir `@ManyToOne` con una relación bidireccional: el lado de `Cliente` no necesita una lista.

## Propuestas para practicar

1. Crea un segundo cliente y asígnale uno de los comerciales ya guardados. Comprueba que no se duplica el comercial.
2. Añade un tercer pedido al primer cliente y muestra el número de pedidos obtenido mediante JPQL.
3. Consulta los clientes que atiende un comercial mediante JPQL sin añadir `List<Cliente>` a `Comercial`.
