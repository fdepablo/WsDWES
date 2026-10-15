# Modelo-Vista-Controlador con servlets y JSP

Este módulo retoma el CRUD de productos de `07_JDBC` para explicar el patrón **Modelo-Vista-Controlador (MVC)**. El 07 separa las operaciones en cuatro servlets y omite la validación de negocio en Java para centrarse en JDBC. Aquí agrupamos el CRUD en un controlador, reutilizamos el formulario y validamos los datos en `ProductoServicio`. Las mismas acciones —listar, crear, editar y eliminar— permiten seguir el recorrido de una petición y reconocer qué responsabilidad tiene cada parte. H2 en archivo está seleccionado por defecto. Como en el ejemplo 07, se puede elegir MariaDB de XAMPP cambiando la implementación de la interfaz de conexión en el servlet.

## Qué vas a aprender

- Identificar modelo, vista y controlador en una aplicación web pequeña.
- Distinguir la lógica de negocio de `ProductoServicio` del acceso SQL de `ProductoDAO`.
- Seguir el recorrido desde un enlace o formulario hasta la respuesta HTML.
- Distinguir datos de la petición (`request.setAttribute`) y datos persistidos en la base.
- Comprender por qué el controlador decide la vista y la JSP no ejecuta SQL.
- Comparar `forward` para mostrar una vista con `sendRedirect` después de modificar datos.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- H2 no necesita un servidor instalado. Solo para la alternativa MariaDB se requiere XAMPP con el servicio **MySQL** iniciado.

## La idea de MVC

| Parte | Archivos del módulo | Responsabilidad |
|---|---|---|
| Modelo | `modelo/entidad/Producto.java`, `modelo/servicio/ProductoServicio.java` y `modelo/persistencia/`, con `ProductoDAO`, las dos conexiones y su interfaz | Representar productos, aplicar reglas y leer o modificar los datos. No genera HTML ni decide qué página mostrar. |
| Vista | `WEB-INF/vistas/listado.jsp` y `alta_editar.jsp` | Presentar los atributos que le entrega el controlador. No abre conexiones JDBC. |
| Controlador | `controlador/ProductoServlet.java` | Interpretar la ruta y los parámetros HTTP, llamar al servicio y elegir la vista, la redirección o el estado HTTP. No conoce el DAO. |

`index.html` es la página pública de entrada. No forma parte del recorrido del CRUD una vez que se abre `/productos`.

Dentro del modelo hay tres funciones: `Producto` representa una entidad; `ProductoServicio` aplica la regla de nombre obligatorio y precio válido y coordina las operaciones; `ProductoDAO` ejecuta SQL mediante `ConexionBD`. El servicio es el único que llama al DAO. Al inicializarse, el servlet elige la implementación de conexión y se la entrega al servicio; no llama al DAO ni ejecuta SQL.

Por ejemplo, al solicitar `GET /productos`, `ProductoServlet` llama a `ProductoServicio.listar()`. El servicio consulta `ProductoDAO`, y el servlet coloca el resultado en el atributo de petición `productos` y hace `forward` a `listado.jsp`. La JSP la recorre con `<c:forEach>` y genera la tabla. Al enviar `POST /productos/nuevo`, el controlador pasa los textos recibidos a `ProductoServicio.crear()`. El servicio valida, llama a `ProductoDAO.insertar()` y el controlador redirige a `GET /productos`. La redirección evita repetir el `INSERT` al recargar.

```text
Navegador ── petición ──► ProductoServlet ──► ProductoServicio ──► ProductoDAO ──► Base de datos
                              │
                              ├── GET: atributo de petición + forward ──► JSP ──► HTML
                              └── POST correcto: redirect ──► nueva petición GET
```

El controlador no construye la tabla HTML ni comprueba las reglas de nombre y precio; esas reglas pertenecen al servicio. Sí convierte el parámetro `id` y decide si responde con `400`, `404` o una vista, porque son decisiones HTTP. La vista no ejecuta SQL. MVC describe esas responsabilidades, no exige un framework concreto.

## Conceptos clave del código

`PreparedStatement` asigna los valores del formulario a los parámetros `?` de las sentencias SQL. `try-with-resources` cierra conexión, sentencia y resultado. El precio usa `Double` para simplificar el ejemplo; el DAO utiliza `setDouble()` y `getDouble()`, mientras la columna sigue siendo `DECIMAL(10,2)`. En una aplicación real con importes monetarios se recomienda `BigDecimal` por la precisión decimal. El servicio comprueba el rango, rechaza valores no finitos y limita los decimales del texto recibido. Las JSP están bajo `WEB-INF`, por lo que solo se muestran a través del servlet. `<c:out>` escapa los valores mostrados; `<c:choose>`, `<c:when>`, `<c:otherwise>`, `<c:if>` y `<c:forEach>` expresan decisiones y recorridos sin scriptlets. Estas etiquetas se explican con más detalle en el [README de 07_JDBC](../07_JDBC/README.md).

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
    │   │   └── persistencia/
    │   │       ├── interfaces/ConexionBD.java
    │   │       └── {ConexionBDMariaDB,ConexionBDH2,ProductoDAO}.java
    │   └── controlador/ProductoServlet.java
    └── webapp/
        ├── index.html
        └── WEB-INF/vistas/{listado,alta_editar}.jsp
```

## Elegir la conexión mediante una interfaz

`modelo.persistencia.interfaces.ConexionBD` define `abrir()` y `prepararBaseDatos()`. `ConexionBDMariaDB` y `ConexionBDH2`, en `modelo.persistencia`, implementan ese contrato. Se conserva esta ubicación dentro del modelo para mantener la estructura MVC del módulo.

En `ProductoServlet.init()` se selecciona el motor de forma manual:

```java
ConexionBD baseDatos = new ConexionBDH2();
servicio = new ProductoServicio(baseDatos);
```

`ProductoServicio` recibe la interfaz, prepara la base y construye `ProductoDAO` con esa misma implementación. El DAO obtiene una conexión nueva mediante `abrir()` para cada operación y la cierra con `try-with-resources`. No cambia el SQL al cambiar de motor.

La elección de la conexión se realiza una vez, al iniciar el servlet. Esto no altera el recorrido MVC: durante las peticiones, el controlador llama al servicio, y el servicio al DAO. El controlador no utiliza la conexión para consultar ni modificar productos.

Cada implementación carga explícitamente su controlador JDBC con `Class.forName(...)`, para que pueda registrarse en `DriverManager` al desplegar en Tomcat. Maven incluye los JAR en `WEB-INF/lib` del WAR; no hay que copiarlos a mano.

## Arrancar con H2 y localizar sus datos

H2 ya está seleccionado en `ProductoServlet`, con el import `modelo.persistencia.ConexionBDH2` y estas líneas:

```java
ConexionBD baseDatos = new ConexionBDH2();
servicio = new ProductoServicio(baseDatos);
```

Compila y despliega de nuevo. No necesitas cambiar el servicio, el DAO ni las JSP. H2 crea la tabla si no existe y utiliza la URL `jdbc:h2:file:./datos/wsdwes_08_mvc`. El archivo será `datos/wsdwes_08_mvc.mv.db`, relativo al **directorio de trabajo de Tomcat**, no necesariamente a la ubicación del WAR. H2 crea la carpeta `datos` si no existe; necesita permisos de escritura. En IntelliJ, la ubicación depende de la configuración de ejecución.

Al desplegar, `ProductoServlet.init()` imprime en la consola de Tomcat `08_MVC - Directorio de trabajo:`, seguido del resultado de `System.getProperty("user.dir")`. La carpeta `datos` se crea respecto a ese directorio. La anotación `loadOnStartup = 1` hace que el servlet se inicialice al desplegar la aplicación, sin esperar a la primera petición.

El archivo queda fuera del WAR. Si ya tenías una base en tu carpeta personal, cambiar la URL no la mueve ni copia sus datos. Para elegir MariaDB, sigue el apartado de la alternativa y vuelve a compilar y desplegar. Cada motor mantiene sus propios datos.

## Alternativa con MariaDB de XAMPP

Para elegir MariaDB, cambia en `ProductoServlet` el import a `modelo.persistencia.ConexionBDMariaDB` y la creación del objeto a `new ConexionBDMariaDB()`.

1. Inicia **MySQL** en el panel de XAMPP.
2. Ejecuta [sql/mariadb.sql](sql/mariadb.sql) en phpMyAdmin. Crea la base `dwes_08` y la tabla `productos`.
3. Revisa los valores locales de demostración de `ConexionBDMariaDB`: URL `jdbc:mariadb://localhost:3306/dwes_08`, usuario `root` y contraseña vacía. Adáptalos a tu instalación sin publicar credenciales reales.
4. Compila, despliega y abre `http://localhost:8080/mvc/productos`.

Al seleccionar MariaDB, el servicio comprueba su conexión al inicializarse, pero no crea la base ni las tablas. Este módulo ya no utiliza las variables `JDBC_URL`, `JDBC_USER` ni `JDBC_PASSWORD`; los valores se encuentran en las clases de conexión como simplificación didáctica. En una aplicación real se configurarían fuera del código.

## Cómo comprobar que funciona

Compila desde la raíz con `mvn -f 08_MVC/pom.xml clean package`. En IntelliJ, configura Tomcat 11 con `08_MVC/src/main/webapp` como **Deployment Directory**, `08_MVC` en **Use classpath of module** y `/mvc` como **Context Path**. Por defecto se usa H2 y la tabla se crea automáticamente, sin iniciar XAMPP. Abre `http://localhost:8080/mvc/productos`.

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
- Si eliges MariaDB, olvidar arrancar su servicio o ejecutar el script antes de iniciar la aplicación.
- Cambiar el motor sin actualizar su import o sin recompilar y desplegar.
- Intentar elegir la conexión mediante variables `JDBC_*`: ahora se selecciona en `ProductoServlet`.
- Suponer que este ejemplo tiene control de acceso: antes de publicarlo harían falta autorización y protección CSRF para las operaciones de escritura.

## Propuestas para practicar

1. Dibuja el recorrido de `POST /productos/editar` e indica dónde interviene cada parte de MVC.
2. Añade el campo `stock` y señala qué archivos de cada parte tienes que modificar, incluida su validación en `ProductoServicio`.
3. Crea una vista de detalle: el controlador debe llamar a `ProductoServicio.buscar(id)`, no al DAO ni a SQL.
