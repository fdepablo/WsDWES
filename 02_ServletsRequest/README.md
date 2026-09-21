# Servlets y parámetros de la petición

Este módulo muestra cómo recibe un servlet los datos enviados por el navegador y cómo genera una respuesta HTML dinámica. También permite observar que dos envíos consecutivos producen dos peticiones HTTP independientes.

## Qué vas a aprender

- Enviar datos desde un formulario HTML mediante el método `GET`.
- Leer parámetros con `HttpServletRequest#getParameter`.
- Convertir un parámetro de tipo `String` a un número.
- Validar los datos aunque el formulario ya tenga validaciones HTML.
- Generar una respuesta con `HttpServletResponse` y `PrintWriter`.
- Indicar el tipo de contenido y la codificación de la respuesta.
- Propagar un dato a otra petición mediante un campo oculto.
- Evitar que un valor introducido por el usuario se interprete como HTML.

## Requisitos

- Java 25.
- Maven 3.9 o una versión compatible.
- Apache Tomcat 11.
- IntelliJ IDEA. En la edición Community puede utilizarse el plugin Smart Tomcat.

## Recorrido del ejemplo

1. `index.html` solicita el nombre y la edad del usuario.
2. El navegador envía ambos parámetros a `PrimerServlet`.
3. El servlet valida los parámetros y construye una página con datos dinámicos.
4. Esa página contiene otro formulario con la edad en un campo oculto.
5. Al pulsar su botón, el navegador crea una petición nueva para `SegundoServlet`.
6. El segundo servlet recibe la edad, pero no el nombre, porque el segundo formulario no lo ha enviado.

La URL generada tras el primer formulario permite ver también los parámetros de la petición:

```text
primer-servlet?nombrePersona=Ana&edadPersona=24
```

## Archivos principales

```text
02_ServletsRequest/
├── pom.xml
└── src/main/
    ├── java/servlets/
    │   ├── PrimerServlet.java
    │   └── SegundoServlet.java
    ├── java/utils/
    │   └── UtilsHTML.java
    └── webapp/
        └── index.html
```

- `PrimerServlet` lee y valida los parámetros del formulario inicial.
- `SegundoServlet` ayuda a comprobar qué información contiene una petición nueva.
- `UtilsHTML` escapa los valores externos antes de insertarlos en una página.

## Cómo compilar

Desde la raíz del workspace:

```bash
mvn -pl 02_ServletsRequest -am clean package
```

El resultado será `02_ServletsRequest/target/02_ServletsRequest-1.0-SNAPSHOT.war`.

## Cómo ejecutar en IntelliJ con Smart Tomcat

1. Recarga los proyectos desde la ventana **Maven** de IntelliJ.
2. Crea una configuración de ejecución de tipo **Smart Tomcat**.
3. Selecciona tu instalación de Tomcat 11.
4. Usa `02_ServletsRequest/src/main/webapp` como **Deployment Directory**.
5. Selecciona `02_ServletsRequest` en **Module**.
6. Utiliza `/servlets-request` como **Context Path**.
7. Ejecuta la configuración y abre:

> [!WARNING]
> **Deployment Directory** y **Use classpath of module** deben apuntar siempre al mismo módulo. Si el directorio pertenece a `02_ServletsRequest`, el módulo seleccionado también debe ser `02_ServletsRequest`. De lo contrario, puede mostrarse el contenido estático pero devolver un error `404` al acceder a los servlets, o incluso no encontrarse el `index.html`.
>
> Smart Tomcat ejecuta un solo módulo web por configuración. Lo más cómodo es duplicar la configuración y conservar una por ejemplo, como `Tomcat - 01_HolaMundoWeb` y `Tomcat - 02_ServletsRequest`, en lugar de cambiar ambos campos cada vez.

```text
http://localhost:8080/servlets-request/
```

## Qué debes comprobar

- El formulario no permite enviar campos vacíos desde la interfaz normal.
- Tras introducir, por ejemplo, `Ana` y `24`, el primer servlet muestra ambos valores y la hora del servidor.
- El segundo servlet conserva la edad porque viaja en un campo oculto.
- El segundo servlet no conoce el nombre porque no se incluyó en la segunda petición.
- Una edad no numérica o fuera del intervalo permitido produce una respuesta HTTP `400 Bad Request` si se manipula manualmente la URL.

## Conceptos importantes

### Los parámetros siempre llegan como texto

`request.getParameter("edadPersona")` devuelve un `String`. Para realizar operaciones numéricas hay que convertirlo, por ejemplo con `Integer.parseInt`, y controlar el posible error.

### La validación HTML no es suficiente

Los atributos `required`, `min` y `max` ayudan al usuario, pero un cliente puede modificar o construir la petición manualmente. El servidor siempre debe volver a validar los datos.

### Cada petición es independiente

`PrimerServlet` y `SegundoServlet` reciben objetos `HttpServletRequest` diferentes. Un parámetro de la primera petición no aparece automáticamente en la segunda: debe enviarse otra vez o conservarse mediante otro mecanismo, como una sesión, que se estudiará en ejemplos posteriores.

### Los valores externos deben escaparse

El nombre procede del navegador. Antes de incluirlo en el HTML se procesa con `UtilsHTML.escapar` para que caracteres como `<` o `>` se muestren como texto y no se interpreten como etiquetas.

## Errores frecuentes

- Abrir directamente `/primer-servlet` sin parámetros provoca correctamente un error `400`.
- Seleccionar el módulo raíz `WsDWES` en Smart Tomcat impide que encuentre las clases de este módulo.
- Usar Tomcat 9 no funciona con los imports `jakarta.servlet`; este ejemplo necesita Tomcat 11.
- El campo oculto viaja al cliente y puede modificarse. No debe utilizarse para guardar información sensible o fiable.

## Propuestas para practicar

1. Propaga también `nombrePersona` al segundo servlet mediante otro campo oculto.
2. Cambia el formulario inicial a `method="post"` y observa cómo desaparecen los parámetros de la URL.
3. Añade un nuevo parámetro y valida su contenido en el servidor.
4. Prueba como nombre `<strong>Ana</strong>` y explica el resultado.
