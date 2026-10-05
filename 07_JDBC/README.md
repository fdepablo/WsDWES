# CRUD de productos con JDBC

Este módulo permite crear, consultar, editar y eliminar productos desde una aplicación web. Usa JDBC directamente, sin ORM ni framework. Por defecto, H2 guarda los datos en un archivo sin instalar un servidor de bases de datos. El mismo código Java puede trabajar con MariaDB de XAMPP cambiando la conexión.

## Qué vas a aprender

- Relacionar las cuatro operaciones CRUD con `INSERT`, `SELECT`, `UPDATE` y `DELETE`.
- Abrir una `Connection`, preparar parámetros con `PreparedStatement` y recorrer un `ResultSet`.
- Cerrar recursos JDBC con `try-with-resources`.
- Separar el servlet, el objeto `Producto`, el acceso a datos y las JSP.
- Validar datos antes de escribir y usar el número de filas afectadas para detectar registros inexistentes.
- Comparar una base embebida con un servidor MariaDB sin cambiar el CRUD.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Para la opción MariaDB: XAMPP con el servicio **MySQL** iniciado. XAMPP denomina así al botón del servicio aunque incluya MariaDB.

## Conceptos clave

`ProductoDAO` contiene las sentencias SQL y recibe una conexión nueva para cada operación. `PreparedStatement` coloca los valores en los signos `?`: los datos del formulario no se concatenan en el SQL. El `SELECT` transforma cada fila de `ResultSet` en un `Producto`. El servlet valida nombre, precio e ID y decide qué JSP mostrar. Tras una escritura correcta redirige al listado para evitar repetir la operación al recargar.

El precio usa `BigDecimal`, adecuado para valores decimales exactos. Las vistas están en `WEB-INF/vistas`, se abren mediante `forward` y muestran valores con `<c:out>` para escapar HTML. El borrado se envía por `POST` para que un simple enlace no elimine registros. Es una práctica sin cuentas de usuario: en una aplicación real habría que añadir autorización y protección CSRF antes de permitir modificaciones.

### Etiquetas JSTL de las JSP

La línea `<%@ taglib prefix="c" uri="jakarta.tags.core" %>` permite utilizar las etiquetas de la biblioteca principal de JSTL con el prefijo `c`. Son instrucciones de la vista; evitan escribir código Java entre `<% ... %>`. Las condiciones y los valores dentro de `${...}` usan Expression Language (EL).

En `listado.jsp` aparecen estas etiquetas:

- `<c:choose>` agrupa varias posibilidades, como un `if` con alternativa.
- `<c:when test="${empty requestScope.productos}">` muestra el aviso si la lista que entregó el servlet está vacía. `requestScope` indica que `productos` es un atributo de la petición.
- `<c:otherwise>` muestra la tabla cuando la condición anterior no se cumple.
- `<c:forEach var="producto" items="${requestScope.productos}">` repite una fila por cada producto. Dentro del bucle, `${producto.nombre}` consulta su getter `getNombre()`.
- `<c:out value="${producto.nombre}" />` escribe el valor escapando caracteres HTML. Se usa para los datos que aparecen en la página.

En `formulario.jsp`, `<c:if test="${not empty requestScope.error}">` muestra el mensaje solo si hubo un error de validación. Otro `<c:if>` añade el campo oculto `id` solo al editar. Allí `<c:choose>` también decide si el título será «Nuevo producto» o «Editar producto». Por último, `${pageContext.request.contextPath}` antepone el contexto de despliegue a los enlaces y formularios, para que funcionen tanto en `/jdbc` como en otro contexto.

## Estructura relevante

```text
07_JDBC/
├── pom.xml
├── sql/mariadb.sql
└── src/main/
    ├── java/
    │   ├── bbdd/{ConexionBD,ProductoDAO}.java
    │   ├── modelo/Producto.java
    │   └── servlets/ProductoServlet.java
    └── webapp/
        ├── index.html
        └── WEB-INF/vistas/{listado,formulario}.jsp
```

## Arranque rápido con H2

Desde la raíz del workspace, compila:

```bash
mvn -pl 07_JDBC -am clean package
```

El WAR queda en `07_JDBC/target/07_JDBC-1.0-SNAPSHOT.war`. Recarga Maven en IntelliJ. Con Smart Tomcat, selecciona Tomcat 11, `07_JDBC/src/main/webapp` como **Deployment Directory**, `07_JDBC` en **Use classpath of module** y `/jdbc` como **Context Path**. Abre `http://localhost:8080/jdbc/` o directamente `http://localhost:8080/jdbc/productos`. Si despliegas el WAR con Tomcat Server, usa el contexto configurado para ese despliegue.

No hace falta configurar variables ni crear la tabla. Al recibir la primera petición, H2 crea la tabla si no existe. La URL predeterminada es `jdbc:h2:file:~/wsdwes_07_jdbc`: `~` representa la carpeta personal del usuario que ejecuta Tomcat. Allí se guarda el archivo de la base de datos, fuera del repositorio. Los productos permanecen al reiniciar Tomcat. Si deseas otro lugar, define `JDBC_URL` antes de arrancar Tomcat, por ejemplo `jdbc:h2:file:C:/datos/dwes_07`.

## Opción con XAMPP y MariaDB

1. Inicia **MySQL** en el panel de XAMPP.
2. Abre phpMyAdmin desde XAMPP y ejecuta [sql/mariadb.sql](sql/mariadb.sql) en la pestaña **SQL**. El script crea `dwes_07` y la tabla `productos` si no existen.
3. En la configuración de ejecución de Tomcat en IntelliJ, define estas variables de entorno:

   ```text
   JDBC_URL=jdbc:mariadb://localhost:3306/dwes_07
   JDBC_USER=usuario_local_de_mariadb
   JDBC_PASSWORD=contraseña_local_de_mariadb
   ```

4. Reinicia Tomcat y visita `/jdbc/productos`. Usa un usuario local con permisos sobre `dwes_07`; no escribas su contraseña en el `pom.xml`, el código o Git. Si el puerto de XAMPP no es 3306, ajusta la URL.

La aplicación no crea la tabla en MariaDB: ese paso se hace con el script para que puedas inspeccionar el esquema en phpMyAdmin. Al volver a H2, quita las tres variables de la configuración y reinicia Tomcat. Las dos bases guardan datos distintos.

## Cómo comprobar que funciona

1. Abre `/jdbc/productos`: inicialmente aparece «Todavía no hay productos».
2. Añade `Cuaderno` con precio `4.50`: aparece una fila con ID asignado por la base de datos.
3. Edita el producto, cambia el nombre y comprueba el listado.
4. Elimínalo y comprueba que vuelve a aparecer el mensaje de lista vacía.
5. Crea otro producto y reinicia Tomcat: con H2 en archivo sigue en el listado.
6. Prueba un nombre vacío, un precio negativo o más de dos decimales: el servidor rechaza la entrada. Un ID inexistente al editar o borrar devuelve `404`.

## Explicación guiada

`ConexionBD` elige la URL y las credenciales. `ProductoDAO` ejecuta SQL y devuelve objetos o el resultado de una operación. `ProductoServlet` recibe `GET /productos`, `GET /productos/nuevo`, `GET /productos/editar?id=...` y los `POST` de alta, edición y borrado. Los formularios envían el ID y los campos necesarios; el servlet nunca construye sentencias SQL.

El flujo de guardado es: formulario → validación → `ProductoDAO` → base de datos → redirección al listado. Si falla la validación, se conserva la petición para volver al formulario con un mensaje y los datos introducidos. `UPDATE` y `DELETE` comprueban las filas afectadas para distinguir un registro ya inexistente.

## Errores frecuentes

- Olvidar recargar Maven: Tomcat no encuentra el controlador H2 o MariaDB.
- Seleccionar otro módulo en **Use classpath of module**: falta el código o alguna dependencia.
- Usar MariaDB sin ejecutar `sql/mariadb.sql` o sin arrancar su servicio.
- Dejar configurado `JDBC_URL` de MariaDB al querer volver a H2.
- Escribir `4,50` en el formulario: el campo numérico HTML y `BigDecimal` esperan `4.50` como valor enviado.
- Abrir el archivo H2 simultáneamente desde otra herramienta: una base H2 embebida suele quedar bloqueada para otro proceso mientras Tomcat la usa.

## Propuestas para practicar

1. Añade una columna `stock` y adapta modelo, formulario, DAO y tabla.
2. Muestra una página de detalle para un solo producto mediante `buscar(id)`.
3. Añade una confirmación visible antes de enviar el formulario de borrado.
