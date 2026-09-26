# Modelo-Vista-Controlador con servlets y JSP

Este módulo retoma el CRUD de productos de `07_JDBC` para explicar el patrón **Modelo-Vista-Controlador (MVC)**. Las mismas acciones —listar, crear, editar y eliminar— permiten seguir el recorrido de una petición y reconocer qué responsabilidad tiene cada parte. La base H2 en archivo funciona por defecto; MariaDB de XAMPP es opcional.

## Qué vas a aprender

- Identificar modelo, vista y controlador en una aplicación web pequeña.
- Distinguir la lógica de negocio de `ProductoServicio` del acceso SQL de `ProductoDAO`.
- Seguir el recorrido desde un enlace o formulario hasta la respuesta HTML.
- Distinguir datos de la petición (`request.setAttribute`) y datos persistidos en la base.
- Comprender por qué el controlador decide la vista y la JSP no ejecuta SQL.
- Comparar `forward` para mostrar una vista con `sendRedirect` después de modificar datos.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Haber trabajado con servlets, JSP, formularios y JDBC en los módulos anteriores.
- XAMPP solo si se elige la variante MariaDB.

## La idea de MVC

| Parte | Archivos del módulo | Responsabilidad |
|---|---|---|
| Modelo | `modelo/entidad/Producto.java`, `modelo/servicio/ProductoServicio.java` y `modelo/persistencia/{ConexionBD,ProductoDAO}.java` | Representar productos, aplicar reglas y leer o modificar los datos. No genera HTML ni decide qué página mostrar. |
| Vista | `WEB-INF/vistas/listado.jsp` y `formulario.jsp` | Presentar los atributos que le entrega el controlador. No abre conexiones JDBC. |
| Controlador | `controlador/ProductoServlet.java` | Interpretar la ruta y los parámetros HTTP, llamar al servicio y elegir la vista, la redirección o el estado HTTP. No conoce el DAO. |

`index.html` es la página pública de entrada. No forma parte del recorrido del CRUD una vez que se abre `/productos`.

Dentro del modelo hay tres funciones: `Producto` representa una entidad; `ProductoServicio` aplica la regla de nombre obligatorio y precio válido y coordina las operaciones; `ProductoDAO` ejecuta SQL mediante `ConexionBD`. El servicio es el único que llama al DAO. El servlet no importa clases de `modelo.persistencia`.

Por ejemplo, al solicitar `GET /productos`, `ProductoServlet` llama a `ProductoServicio.listar()`. El servicio consulta `ProductoDAO`, y el servlet coloca el resultado en el atributo de petición `productos` y hace `forward` a `listado.jsp`. La JSP la recorre con `<c:forEach>` y genera la tabla. Al enviar `POST /productos/nuevo`, el controlador pasa los textos recibidos a `ProductoServicio.crear()`. El servicio valida, llama a `ProductoDAO.insertar()` y el controlador redirige a `GET /productos`. La redirección evita repetir el `INSERT` al recargar.

```text
Navegador ── petición ──► ProductoServlet ──► ProductoServicio ──► ProductoDAO ──► Base de datos
                              │
                              ├── GET: atributo de petición + forward ──► JSP ──► HTML
                              └── POST correcto: redirect ──► nueva petición GET
```

El controlador no construye la tabla HTML ni comprueba las reglas de nombre y precio; esas reglas pertenecen al servicio. Sí convierte el parámetro `id` y decide si responde con `400`, `404` o una vista, porque son decisiones HTTP. La vista no ejecuta SQL. MVC describe esas responsabilidades, no exige un framework concreto.

## Conceptos clave del código

`PreparedStatement` asigna los valores del formulario a los parámetros `?` de las sentencias SQL. `try-with-resources` cierra conexión, sentencia y resultado. El precio usa `BigDecimal`. Las JSP están bajo `WEB-INF`, por lo que solo se muestran a través del servlet. `<c:out>` escapa los valores mostrados; `<c:choose>`, `<c:when>`, `<c:otherwise>`, `<c:if>` y `<c:forEach>` expresan decisiones y recorridos sin scriptlets. Estas etiquetas se explican con más detalle en el [README de 07_JDBC](../07_JDBC/README.md).

## Estructura relevante

```text
08_MVC/
├── pom.xml
├── sql/mariadb.sql
└── src/main/
    ├── java/
    │   ├── modelo/
    │   │   ├── servicio/ProductoServicio.java
    │   │   ├── entidad/Producto.java
    │   │   └── persistencia/{ConexionBD,ProductoDAO}.java
    │   └── controlador/ProductoServlet.java
    └── webapp/
        ├── index.html
        └── WEB-INF/vistas/{listado,formulario}.jsp
```

## Cómo usar MariaDB de XAMPP

1. Inicia **MySQL** en el panel de XAMPP.
2. Ejecuta [sql/mariadb.sql](sql/mariadb.sql) en la pestaña **SQL** de phpMyAdmin. Crea la base `dwes_08` y la tabla `productos`.
3. Define en la configuración de Tomcat estas variables de entorno y reinicia:

   ```text
   JDBC_URL=jdbc:mariadb://localhost:3306/dwes_08
   JDBC_USER=usuario_local_de_mariadb
   JDBC_PASSWORD=contraseña_local_de_mariadb
   ```

Usa un usuario local con permisos sobre `dwes_08` y no guardes credenciales en el repositorio. Si el puerto de XAMPP es distinto, cambia la URL. Para volver a H2, elimina las variables y reinicia Tomcat.

## Cómo comprobar que funciona

Compila desde la raíz con `mvn -pl 08_MVC -am clean package`. En IntelliJ, configura Tomcat 11 con `08_MVC/src/main/webapp` como **Deployment Directory**, `08_MVC` en **Use classpath of module** y `/mvc` como **Context Path**. Sin variables JDBC se usa la base H2 predeterminada. Abre `http://localhost:8080/mvc/productos`.

1. Abre `/mvc/productos`: aparece el listado vacío o los productos guardados previamente.
2. Crea `Cuaderno` con precio `4.50`; comprueba que el navegador termina en `/mvc/productos` y aparece una fila.
3. Edita el nombre y comprueba la actualización.
4. Elimina el producto y comprueba que desaparece.
5. Prueba un precio negativo o un nombre vacío: el servicio rechaza los datos y el controlador vuelve al formulario con un mensaje y estado `400`.
6. Pide la edición de un ID inexistente: recibes `404`.

## Explicación guiada

Empieza por el enlace del listado en `index.html`. Sigue `doGet`: la ruta determina si se presenta el listado o el formulario. Busca la llamada a `servicio.listar()`, después `ProductoDAO.listar()`, y vuelve al `setAttribute("productos", ...)` del servlet. Localiza `${requestScope.productos}` en `listado.jsp`: es el mismo dato durante el `forward`. Después sigue un alta: `doPost` entrega los parámetros a `ProductoServicio.crear()`, el servicio valida, el DAO ejecuta `INSERT` y el servlet hace `sendRedirect` para provocar un nuevo `GET`. Si el servicio rechaza los datos, el servlet muestra el formulario con `forward` para conservar lo escrito.

## Errores frecuentes

- Llamar «modelo» solo a `Producto`: el servicio y la persistencia también forman parte del modelo.
- Hacer que el servlet llame directamente al DAO y repetir allí las reglas de nombre y precio.
- Poner SQL o validación de negocio en una JSP.
- Creer que `forward` y `sendRedirect` conservan los mismos atributos de petición.
- Seleccionar `07_JDBC` en **Use classpath of module** al arrancar este módulo.
- Olvidar arrancar MariaDB o ejecutar el script al cambiar de H2 a XAMPP.
- Suponer que este ejemplo tiene control de acceso: antes de publicarlo harían falta autorización y protección CSRF para las operaciones de escritura.

## Propuestas para practicar

1. Dibuja el recorrido de `POST /productos/editar` e indica dónde interviene cada parte de MVC.
2. Añade el campo `stock` y señala qué archivos de cada parte tienes que modificar, incluida su validación en `ProductoServicio`.
3. Crea una vista de detalle: el controlador debe llamar a `ProductoServicio.buscar(id)`, no al DAO ni a SQL.
