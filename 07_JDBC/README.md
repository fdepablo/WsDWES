# CRUD de productos con JDBC

Este módulo permite crear, consultar, editar y eliminar productos desde una aplicación web. Usa JDBC directamente, sin ORM ni framework.

## Qué vas a aprender

- Relacionar las cuatro operaciones CRUD con `INSERT`, `SELECT`, `UPDATE` y `DELETE`.
- Abrir una `Connection`, preparar parámetros con `PreparedStatement` y recorrer un `ResultSet`.
- Cerrar recursos JDBC con `try-with-resources`.
- Seguir cada operación en su propio servlet y separar los objetos, el acceso a datos y las JSP.
- Usar el número de filas afectadas para detectar registros inexistentes.
- Reconocer la diferencia entre las comprobaciones HTML y la validación que debe hacerse en el servidor.
- Usar una interfaz común y dos implementaciones para cambiar de motor sin modificar el DAO.
- Comparar una base H2 en archivo con un servidor MariaDB.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Solo para la alternativa MariaDB: XAMPP con el servicio **MySQL** iniciado. XAMPP denomina así al botón del servicio aunque incluya MariaDB.
- Por defecto se conecta a H2 en archivo, sin instalar un servidor. Para usar MariaDB de XAMPP, cambia la implementación de `ConexionBD` que se crea en el servlet, sin modificar el DAO.

## Conceptos clave

### Qué es JDBC y qué significa CRUD

**JDBC** es la API de Java que permite conectarse a una base de datos, ejecutar sentencias SQL y leer sus resultados. En este ejemplo escribimos el SQL y convertimos sus resultados en objetos Java nosotros mismos.

**CRUD** reúne las cuatro operaciones básicas sobre los datos. En nuestra tabla, cada fila representa un producto:

| Operación | Qué hace el usuario | Sentencia SQL | Método de ProductoDAO |
| --- | --- | --- | --- |
| Crear | Añadir un producto | `INSERT` | `insertar(...)` |
| Consultar | Ver el listado o abrir un producto | `SELECT` | `listar()` o `buscar(id)` |
| Actualizar | Guardar los cambios de un producto | `UPDATE` | `actualizar(...)` |
| Eliminar | Borrar un producto | `DELETE` | `eliminar(id)` |

El `id` identifica cada fila. Lo genera la base de datos al insertar y se utiliza para localizar el producto al consultar, editar o eliminar. Cambiar el nombre o el precio no cambia su ID.

### Qué responsabilidad tiene cada clase

**DAO** significa *Data Access Object*, u objeto de acceso a datos. `ProductoDAO` reúne las sentencias SQL para que no aparezcan mezcladas con el HTML o con el tratamiento de las peticiones HTTP.

| Componente | Responsabilidad en este ejemplo |
| --- | --- |
| `Producto` | Representa los datos de una fila: ID, nombre y precio. No ejecuta SQL. |
| `ConexionBD` y sus implementaciones | Proporcionan conexiones JDBC con H2 o MariaDB. |
| `ProductoDAO` | Ejecuta SQL y devuelve objetos `Producto`, listas o el resultado de una modificación. |
| Los cuatro servlets | Cada uno recibe los parámetros de una operación, llama al DAO y decide la respuesta HTTP. |
| Las JSP | Presentan los datos entregados por el servlet y muestran los formularios. No se conectan a la base de datos. |

### Cuatro servlets para seguir cada operación

En este primer ejemplo JDBC separamos el CRUD para que cada servlet tenga un recorrido corto y no haya que distinguir operaciones mediante rutas dentro de un único controlador.

| Servlet | GET | POST |
| --- | --- | --- |
| `ListadoProductosServlet` | `/productos`: consulta y muestra `listado.jsp`. | No se utiliza. |
| `AltaProductoServlet` | `/productos/nuevo`: muestra `alta_editar.jsp` vacía. | Inserta el producto y redirige al listado. |
| `EditarProductoServlet` | `/productos/editar?id=...`: busca el producto y muestra `alta_editar.jsp`. | Actualiza la fila indicada y redirige al listado. |
| `EliminarProductoServlet` | No se utiliza. | `/productos/eliminar`: elimina por ID y redirige al listado. |

Los servlets de alta y edición comparten `alta_editar.jsp`. Si no existe el atributo de petición `producto`, la JSP muestra los campos vacíos y envía el POST a `/productos/nuevo`. Si existe, muestra sus datos, incluye el ID oculto y envía a `/productos/editar`. No necesitamos el atributo `accion`: la presencia del producto indica el modo del formulario.

Es habitual agrupar las operaciones de un CRUD en **un solo controlador** y reutilizar el formulario. Lo veremos en [08_MVC](../08_MVC/README.md), junto con un servicio que concentra la validación. Aquí utilizamos cuatro servlets para aprender JDBC paso a paso.

### Inicialización en el recorrido guiado

La entrada del ejemplo es el listado. Su servlet prepara la base en `init()` una sola vez por instancia; los demás servlets se utilizan después y no repiten esa preparación. Crear un DAO no abre una conexión: esta se abre cuando se ejecuta una de sus operaciones.

La anotación del listado incluye `loadOnStartup = 1`: Tomcat ejecuta su `init()` al desplegar la aplicación, sin esperar a la primera petición. Así se prepara la base una sola vez y se imprime el directorio de trabajo en la consola. El recorrido de clase sigue empezando por el listado.

### Validación omitida para simplificar el ejemplo

**Este módulo no realiza validaciones de negocio en el servidor.** Los formularios utilizan `required`, `maxlength`, `min`, `max` y `step` para ayudar al usuario, pero esas comprobaciones del navegador pueden eludirse.

Los servlets leen los parámetros y convierten el ID con `Long.parseLong(...)` y el precio con `Double.parseDouble(...)`. Convertir un texto a número no sustituye la validación: por ejemplo, un precio negativo es un decimal válido y aquí no se rechaza en Java. Un valor que no puede convertirse puede provocar un error 500; una restricción de la base de datos también puede rechazar una escritura.

En una aplicación real hay que **validar todos los datos en el servidor**, incluidos los ID ocultos: presencia, formato, longitud, rango y permisos. El ejemplo 08 introduce la validación de nombre y precio en `ProductoServicio`.

### Abrir una conexión con Connection

Una `Connection` representa una conexión abierta con la base de datos. El DAO la obtiene así:

```java
Connection conexion = baseDatos.abrir();
```

`baseDatos` es un objeto que cumple la interfaz `ConexionBD` de nuestro proyecto. Su método `abrir()` devuelve una `Connection` de la API JDBC. Son dos tipos distintos: uno es nuestro contrato para obtener conexiones y el otro permite trabajar con la base de datos.

Cada método del DAO abre su propia conexión y la cierra al terminar mediante `try-with-resources`, que se explica más abajo.

### Una interfaz y dos implementaciones de conexión

`bbdd.interfaces.ConexionBD` define el contrato común: `abrir()` devuelve una `Connection` JDBC nueva y `prepararBaseDatos()` prepara o comprueba la base al iniciar el servlet. El paquete se llama `interfaces` porque `interface` es una palabra reservada de Java.

Las clases `ConexionBDMariaDB` y `ConexionBDH2`, dentro de `bbdd`, cumplen ese contrato mediante `implements ConexionBD`. Cada una conoce su URL, usuario, contraseña de demostración y controlador. MariaDB comprueba la conexión; H2 crea la tabla si no existe.

En `ListadoProductosServlet.init()` elegimos la implementación, preparamos la base y creamos su DAO:

```java
ConexionBD baseDatos = new ConexionBDH2();
baseDatos.prepararBaseDatos();
productos = new ProductoDAO(baseDatos);
```

La variable está declarada con el tipo de la **interfaz**, pero contiene un objeto de una **clase concreta**. Para elegir MariaDB, cambia la primera línea por `new ConexionBDMariaDB()` y su import por `bbdd.ConexionBDMariaDB` **en los cuatro servlets**. Deben usar el mismo motor. Compila y vuelve a desplegar.

La elección del motor se repite de forma explícita en los cuatro servlets para evitar introducir una clase base o una configuración adicional. Solo `ListadoProductosServlet` tiene `init()` y llama a `prepararBaseDatos()`. Los otros tres crean la implementación y el DAO como campos, sin repetir la preparación. Cada operación abre y cierra su propia conexión. **No se utilizan las variables de entorno `JDBC_URL`, `JDBC_USER` ni `JDBC_PASSWORD` en este módulo.** En una aplicación real, las credenciales se configurarían fuera del código; aquí solo se muestran valores locales de demostración.

`ProductoDAO` recibe `ConexionBD` y solo utiliza `abrir()`. No necesita saber qué implementación recibe: las sentencias del CRUD son las mismas para ambos motores. Esta es la utilidad de la interfaz en el ejemplo.

### Cargar el controlador JDBC

JDBC proporciona interfaces como `Connection`, pero necesita un **driver de conexión** para comunicarse con cada motor de base de datos, por ello los damos de alta en el pom.xml de Maven. Maven incluye los JAR de H2 y MariaDB en `WEB-INF/lib` al construir el WAR para que los pueda usar el servidor.

El método `abrir()` de cada implementación carga su controlador con `Class.forName(...)`: `ConexionBDH2` carga `org.h2.Driver` y `ConexionBDMariaDB` carga `org.mariadb.jdbc.Driver`. Al inicializarse, el controlador se registra en `DriverManager`, que ya puede abrir la conexión.

### Preparar una sentencia y pasarle parámetros

`PreparedStatement` representa una sentencia SQL que podemos ejecutar asignando sus parámetros por separado. Por ejemplo, `buscar(id)` utiliza:

```java
String sql = "SELECT id, nombre, precio FROM productos WHERE id = ?";
```

Después de abrir la conexión, prepara esa sentencia y asigna el ID:

```java
PreparedStatement sentencia = conexion.prepareStatement(sql);
sentencia.setLong(1, id);
```

El `?` es un parámetro SQL. `setLong(1, id)` asigna el valor al **primer parámetro**; la numeración empieza en 1. Para otros tipos usamos métodos como `setString(...)` y `setDouble(...)`.

Los valores se envían como datos, sin concatenarlos con el SQL. Así, un nombre con un apóstrofo no rompe la sentencia y el texto introducido por el usuario no se interpreta como instrucciones SQL. Con esto ayudamos a prevenir inyecciones SQL.

### Leer una consulta con ResultSet

Un `SELECT` se ejecuta con `executeQuery()` y devuelve un `ResultSet`, que permite recorrer las filas obtenidas:

```java
ResultSet filas = sentencia.executeQuery();
while (filas.next()) {
    productos.add(leerProducto(filas));
}
```

Inicialmente estamos antes de la primera fila. Cada llamada a `next()` avanza una posición y devuelve `true` si hay una fila que leer. Si no quedan filas, devuelve `false` y termina el bucle.

`leerProducto(filas)` crea un producto vacío y asigna cada columna de la fila actual mediante su setter:

```java
Producto producto = new Producto();
producto.setId(filas.getLong("id"));
producto.setNombre(filas.getString("nombre"));
producto.setPrecio(filas.getDouble("precio"));
return producto;
```

`listar()` devuelve una lista vacía cuando no hay productos. `buscar(id)` solo necesita comprobar una fila y devuelve `null` si no encuentra ese ID. La JSP recibirá objetos Java.

### Ejecutar INSERT, UPDATE y DELETE

Estas operaciones usan `executeUpdate()`, que devuelve el **número de filas afectadas**, no un `ResultSet`.

```java
return sentencia.executeUpdate() == 1;
```

En `actualizar()` y `eliminar()`, el `WHERE id = ?` identifica una única fila. Si se modifica o elimina, el método devuelve `true`; si ya no existe, se afectan cero filas y devuelve `false`. El servlet utiliza ese resultado para responder con un error 404.

### Cerrar recursos con try-with-resources

Al trabajar con JDBC abrimos recursos que deben cerrarse cuando dejamos de utilizarlos: la conexión (`Connection`), la sentencia (`PreparedStatement`) y, en las consultas, el resultado (`ResultSet`). Dejarlos abiertos puede agotar las conexiones y otros recursos de la base de datos. No basta con esperar a que Java libere la memoria de los objetos.

**`try-with-resources`** es una forma de `try` que cierra automáticamente los recursos declarados entre sus paréntesis. En el método `listar()` de `ProductoDAO` aparece así:

```java
try (Connection conexion = baseDatos.abrir();
     PreparedStatement sentencia = conexion.prepareStatement(sql);
     ResultSet filas = sentencia.executeQuery()) {
    while (filas.next()) {
        productos.add(leerProducto(filas));
    }
}
```

El recorrido es el siguiente:

1. Se abre la conexión, se prepara la sentencia y se ejecuta la consulta, en ese orden. Las declaraciones se separan con `;`.
2. Dentro de las llaves se recorren las filas y se construyen los objetos `Producto`.
3. Al salir del bloque, Java llama automáticamente a `close()` en orden inverso: primero cierra `filas`, después `sentencia` y finalmente `conexion`.

El cierre también se realiza si salimos con un `return` o se lanza una excepción. Si falla la apertura de un recurso, se cierran los anteriores que hayan llegado a abrirse. Por eso no necesitamos escribir los `close()` manualmente en un bloque `finally`.

Para usar esta sintaxis, los recursos deben implementar `AutoCloseable`, una interfaz que define el método `close()`. Los tres tipos JDBC del ejemplo la implementan. Los objetos `Producto` y la lista que devuelve el DAO siguen disponibles después del cierre: sus datos ya se han copiado desde el `ResultSet`.

#### Cómo se hacía antes con finally

Sin `try-with-resources`, el cierre se escribía manualmente en un bloque `finally`. Este ejemplo muestra solo la conexión para centrarnos en la diferencia:

```java
Connection conexion = null;
try {
    conexion = baseDatos.abrir();
    // Aquí ejecutaríamos las operaciones con la base de datos.
} finally {
    if (conexion != null) {
        conexion.close();
    }
}
```

La variable se declara fuera del `try` para poder utilizarla en `finally`. Se comprueba que no sea `null` porque la apertura podría haber fallado. El bloque `finally` se ejecuta al salir del `try`, también si hay un `return` o una excepción.

Si además abrimos una sentencia y un resultado, también debemos cerrarlos manualmente, en orden inverso, y garantizar que se intente cerrar cada recurso aunque falle otro cierre. `close()` también puede lanzar una excepción. `try-with-resources` resuelve esa gestión con menos código; por eso es la opción utilizada en el DAO.

#### Por qué buscar tiene un try dentro de otro

En `buscar(long id)` primero hay que asignar el parámetro de la consulta y después ejecutarla:

```java
try (Connection conexion = baseDatos.abrir();
     PreparedStatement sentencia = conexion.prepareStatement(sql)) {
    sentencia.setLong(1, id);
    try (ResultSet filas = sentencia.executeQuery()) {
        return filas.next() ? leerProducto(filas) : null;
    }
}
```

El `try` interior cierra el resultado; el exterior cierra la sentencia y la conexión. Aunque haya un `return` dentro, los recursos se cierran antes de devolver el producto.

`try-with-resources` **gestiona el cierre, pero no resuelve el error**. Los métodos del DAO declaran `throws SQLException` para que el código que los llama pueda tratar los fallos de JDBC. Tampoco realiza un `commit` o un `rollback` automáticamente por usar esta sintaxis: la gestión de transacciones es otro concepto.

### Pasar de la petición HTTP a la JSP

Al consultar el listado, el recorrido es:

```text
GET /productos → ListadoProductosServlet → DAO → SELECT → List<Producto>
                     │
                     └─ atributo de petición + forward → listado.jsp
```

El servlet guarda la lista con `request.setAttribute("productos", ...)` y hace `forward` a la JSP. La vista recibe esos datos dentro de la misma petición. Las JSP están en `WEB-INF/vistas`, por lo que el navegador accede a través del servlet.

Al añadir, editar o eliminar, se envía un **POST**. Si la operación es correcta, el servlet hace `redirect` al listado: el navegador ejecuta un GET nuevo y actualizar la página no repite la escritura. En este módulo no se implementa la vuelta al formulario con errores de validación del servidor; ese tratamiento se verá en el ejemplo 08.

En el listado, **Editar** es un formulario GET que envía el ID para consultar el producto. **Eliminar** es un formulario POST que envía el ID para borrarlo. El ID oculto puede manipularse: en una aplicación real también habría que validarlo en el servidor.

Este ejemplo no incluye cuentas de usuario. En una aplicación real habría que añadir autorización y protección CSRF antes de permitir modificaciones.

### Etiquetas JSTL de las JSP

La línea `<%@ taglib prefix="c" uri="jakarta.tags.core" %>` permite utilizar las etiquetas de la biblioteca principal de JSTL con el prefijo `c`.

Las condiciones y los valores dentro de `${...}` usan Expression Language (EL).

En `listado.jsp` aparecen estas etiquetas:

- `<c:choose>` agrupa varias posibilidades, como un `if` con alternativa.
- `<c:when test="${empty requestScope.productos}">` muestra el aviso si la lista que entregó el servlet está vacía. `requestScope` indica que `productos` es un atributo de la petición.
- `<c:otherwise>` muestra la tabla cuando la condición anterior no se cumple.
- `<c:forEach var="producto" items="${requestScope.productos}">` repite una fila por cada producto. Dentro del bucle, `${producto.nombre}` consulta su getter `getNombre()`.
- `<c:out value="${producto.nombre}" />` escribe el valor escapando caracteres HTML. Se usa para los datos que aparecen en la página.

En `alta_editar.jsp`, `empty requestScope.producto` comprueba si falta el producto. La expresión `${condicion ? valorSiCierto : valorSiFalso}` elige el título y la ruta de envío: alta si no hay producto y edición si lo hay. `<c:if test="${not empty requestScope.producto}">` incluye el ID oculto solo al editar. Los campos usan `c:out` para mostrar los valores; en el alta quedan vacíos. `${pageContext.request.contextPath}` antepone el contexto de despliegue a los enlaces y formularios, de modo que funcionen con `/jdbc` o con otro contexto.

## Estructura relevante

```text
07_JDBC/
├── pom.xml
├── sql/mariadb.sql
└── src/main/
    ├── java/
    │   ├── bbdd/
    │   │   ├── interfaces/ConexionBD.java
    │   │   └── {ConexionBDMariaDB,ConexionBDH2,ProductoDAO}.java
    │   ├── modelo/Producto.java
    │   └── servlets/{ListadoProductos,AltaProducto,EditarProducto,EliminarProducto}Servlet.java
    └── webapp/
        ├── index.html
        └── WEB-INF/vistas/{listado,alta_editar}.jsp
```

## Compilar y desplegar

Desde la raíz del workspace, compila el módulo:

```bash
mvn -f 07_JDBC/pom.xml clean package
```

El WAR queda en `07_JDBC/target/07_JDBC-1.0-SNAPSHOT.war`. Recarga Maven en IntelliJ. Con Smart Tomcat, selecciona Tomcat 11, `07_JDBC/src/main/webapp` como **Deployment Directory**, `07_JDBC` en **Use classpath of module** y `/jdbc` como **Context Path**. En Tomcat Server de IntelliJ puedes desplegar el WAR y configurar ese contexto. Si eliges otro contexto, sustituye `/jdbc` en las URL.

## Arrancar con H2 y localizar sus datos

H2 ya está seleccionado en los cuatro servlets mediante el import `bbdd.ConexionBDH2` y esta línea:

```java
ConexionBD baseDatos = new ConexionBDH2();
```

H2 crea la tabla si no existe y conserva los datos en un archivo. La URL está en `ConexionBDH2`: `jdbc:h2:file:./datos/wsdwes_07_jdbc`. El archivo resultante es `datos/wsdwes_07_jdbc.mv.db`, relativo al **directorio de trabajo de Tomcat**, que puede variar según la configuración de ejecución en IntelliJ. No es necesariamente la carpeta que contiene el WAR. H2 crea la carpeta `datos` si no existe; debe tener permisos de escritura.

Para localizarla cuando ejecutas desde IntelliJ, ten en cuenta que Tomcat puede arrancar con su carpeta `bin` como directorio de trabajo. En ese caso encontrarás el archivo en:

```text
<carpeta de instalación de Tomcat>/bin/datos/wsdwes_07_jdbc.mv.db
```

Si tu configuración utiliza otro directorio de trabajo, busca la carpeta `datos` dentro de él. No tiene por qué aparecer en el árbol del proyecto de IntelliJ. Al desplegar, `ListadoProductosServlet.init()` muestra en la consola de Tomcat el resultado de `System.getProperty("user.dir")`, precedido de `07_JDBC - Directorio de trabajo:`. Esa es la carpeta desde la que se resuelve `./datos`.

La base queda fuera del WAR y debe conservarse al redesplegar. Si antes utilizabas la ruta de la carpeta personal, aquel archivo permanece allí: cambiar la URL crea o abre otra base, no traslada los datos anteriores.

Si cambias a MariaDB siguiendo el apartado de la alternativa, compila y despliega de nuevo. Cada motor conserva sus propios datos: cambiar la implementación no los copia de una base a otra.

## Alternativa con MariaDB de XAMPP

H2 es la implementación predeterminada. Para usar MariaDB, cambia en los cuatro servlets el import a `bbdd.ConexionBDMariaDB` y la creación del objeto a `new ConexionBDMariaDB()`.

1. Inicia **MySQL** en el panel de XAMPP.
2. Abre phpMyAdmin y ejecuta [sql/mariadb.sql](sql/mariadb.sql) en la pestaña **SQL**. El script crea `dwes_07` y la tabla `productos` si no existen.
3. Comprueba los valores de demostración de `ConexionBDMariaDB`: URL `jdbc:mariadb://localhost:3306/dwes_07`, usuario `root` y contraseña vacía. Si tu instalación tiene otro puerto, usuario o contraseña, adapta los valores localmente. No publiques credenciales reales en Git.
4. Compila, despliega y abre `http://localhost:8080/jdbc/productos` o la entrada `http://localhost:8080/jdbc/`.

`ListadoProductosServlet` comprueba o prepara la base durante `init()`. MariaDB no crea automáticamente la base ni la tabla: hay que ejecutar el script antes. Si el servidor está apagado o la conexión es incorrecta, el servlet no podrá inicializarse.

## Cómo comprobar que funciona

1. Abre `/jdbc/productos`: inicialmente aparece «Todavía no hay productos».
2. Añade `Cuaderno` con precio `4.50`: aparece una fila con ID asignado por la base de datos.
3. Edita el producto, cambia el nombre y comprueba el listado.
4. Elimínalo y comprueba que vuelve a aparecer el mensaje de lista vacía.
5. Crea otro producto y reinicia Tomcat: con H2 en archivo sigue en el listado.
6. Comprueba que el navegador impide enviar los campos obligatorios vacíos o un precio negativo. Son comprobaciones HTML: no hay validación de negocio en Java. Un ID numérico que no existe al editar o borrar devuelve `404`.

## Explicación guiada

Empieza siempre por `ListadoProductosServlet`: su `init()` prepara la base una vez al crear el servlet. Después obtiene la lista del DAO, la guarda en request y hace `forward` a `listado.jsp`. El enlace Añadir producto solicita el GET de `AltaProductoServlet`, que muestra `alta_editar.jsp`. Al guardar, su POST lee nombre y precio, llama a `insertar()` y redirige al listado.

El botón Editar envía un GET con el ID a `EditarProductoServlet`: busca el producto y pasa sus datos a `alta_editar.jsp`. El POST del mismo servlet recibe el ID oculto y los nuevos valores y llama a `actualizar()`. El botón Eliminar envía un POST al servlet de borrado. `UPDATE` y `DELETE` comprueban las filas afectadas para distinguir un registro que ya no existe.

Los cuatro servlets reciben una implementación de `ConexionBD` al construir su DAO. Ninguna JSP abre conexiones ni ejecuta SQL. El recorrido de escritura es formulario → servlet → DAO → base de datos → redirección al listado. Aquí se omite la validación de negocio en Java; el ejemplo 08 la incorpora a la capa de servicio y reúne las operaciones en un controlador.

## Errores frecuentes

- Olvidar recargar Maven: Tomcat no encuentra el controlador H2 o MariaDB.
- Recibir `No suitable driver found`: comprobar que el JAR del motor está en `WEB-INF/lib` y que la implementación de `ConexionBD` carga su controlador antes de llamar a `DriverManager.getConnection()`.
- Seleccionar otro módulo en **Use classpath of module**: falta el código o alguna dependencia.
- Usar MariaDB sin ejecutar `sql/mariadb.sql` o sin arrancar su servicio.
- Cambiar de implementación sin modificar su import, o sin recompilar y desplegar de nuevo.
- Intentar elegir el motor con variables `JDBC_*`: este ejemplo lo selecciona directamente en los cuatro servlets.
- Confiar en que `required` o un campo oculto garantizan datos válidos: se pueden eludir o manipular y en este módulo se ha omitido la validación del servidor.
- Escribir `4,50` en el formulario: el campo numérico HTML y `Double.parseDouble()` esperan `4.50` como valor enviado.
- Abrir el archivo H2 simultáneamente desde otra herramienta: una base H2 embebida suele quedar bloqueada para otro proceso mientras Tomcat la usa.

## Propuestas para practicar

1. Añade una columna `stock` y adapta modelo, formulario, DAO y tabla.
2. Muestra una página de detalle para un solo producto mediante `buscar(id)`.
3. Añade una confirmación visible antes de enviar el formulario de borrado.
