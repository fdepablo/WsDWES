# Formulario procesado con Servlet y JSP

Este módulo muestra el procesamiento completo de un formulario HTML. Un servlet recibe y valida los datos, mientras que varias páginas JSP se ocupan exclusivamente de generar las vistas.

## Qué vas a aprender

- Mostrar una JSP desde un servlet mediante `forward`.
- Enviar un formulario mediante `POST`.
- Leer valores con `getParameter` y `getParameterValues`.
- Tratar correctamente un grupo de casillas opcional.
- Validar en el servidor campos obligatorios y opciones permitidas.
- Convertir los datos válidos en un objeto `Usuario`.
- Enviar objetos y mensajes a las JSP mediante atributos de la petición.
- Usar JSTL para tomar decisiones, recorrer una colección y escapar salidas.
- Separar el controlador, el modelo, la lógica de negocio y las vistas.
- Evitar registrar, mostrar o guardar contraseñas en texto plano.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Haber realizado `04_JSP`.

## Conceptos clave

### El servlet controla y la JSP presenta

`RegistroServlet` decide qué debe ocurrir con cada petición. Las JSP contienen el HTML y muestran los atributos preparados por el servlet. De este modo no se construye la página mediante `PrintWriter`.

Las vistas se guardan en `WEB-INF/vistas`, por lo que el navegador no puede abrirlas directamente. Solo el servlet puede llegar a ellas mediante un `forward`.

### `POST` y seguridad de la contraseña

El formulario usa `POST` porque el registro modifica el estado de la aplicación. Esto evita mostrar los parámetros en la URL, pero no los cifra: una aplicación real también necesita HTTPS.

La contraseña se valida para estudiar su recepción, pero no se incluye en `Usuario`, no se muestra, no se escribe en logs y no se almacena. Un sistema real tendría que transformarla con un algoritmo específico para contraseñas.

### Parámetros con varios valores

Las casillas comparten `name="intereses"`. El servlet usa `getParameterValues`, que devuelve un array cuando hay selecciones y `null` cuando no se marca ninguna. El código convierte ambos casos en una lista que puede manejar de forma uniforme.

### JSTL y salida segura

La JSP de confirmación usa `<c:forEach>` para recorrer los intereses, `<c:choose>` para elegir qué mostrar y `<c:out>` para escapar los valores antes de incorporarlos al HTML.

### Estado temporal

`GestorUsuarios` conserva los registros en memoria y evita correos duplicados. Sus métodos están sincronizados porque Tomcat puede atender varias peticiones al mismo tiempo. Los datos desaparecen al reiniciar o redesplegar la aplicación.

## Estructura relevante

```text
05_Formulario/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/Usuario.java
    │   ├── servicios/GestorUsuarios.java
    │   └── servlets/RegistroServlet.java
    └── webapp/WEB-INF/vistas/
        ├── formulario.jsp
        ├── confirmacion.jsp
        └── error.jsp
```

## Recorrido del ejemplo

1. `GET /registro` ejecuta el servlet y hace un `forward` a `formulario.jsp`.
2. El navegador envía los campos mediante `POST /registro`.
3. El servlet establece UTF-8 antes de leer los parámetros.
4. Los datos se validan y las opciones se comparan con los valores permitidos.
5. Si hay un error, el servlet establece el estado HTTP adecuado y muestra `error.jsp`.
6. Si los datos son válidos, se crea y registra un `Usuario`.
7. El servlet añade el usuario a la petición y hace `forward` a `confirmacion.jsp`.
8. JSTL genera la presentación sin introducir código Java en la JSP.

## Cómo compilar

Desde la raíz del workspace:

```bash
mvn -pl 05_Formulario -am clean package
```

El resultado será `05_Formulario/target/05_Formulario-1.0-SNAPSHOT.war`.

## Cómo ejecutar en IntelliJ con Smart Tomcat

1. Recarga los proyectos desde la ventana **Maven**.
2. Crea o duplica una configuración de tipo **Smart Tomcat**.
3. Selecciona Tomcat 11.
4. Usa `05_Formulario/src/main/webapp` como **Deployment Directory**.
5. Selecciona `05_Formulario` en **Use classpath of module**.
6. Utiliza `/formulario` como **Context Path**.
7. Abre `http://localhost:8080/formulario/registro`.

> [!WARNING]
> **Deployment Directory** y **Use classpath of module** deben apuntar al mismo módulo.

## Cómo comprobar que funciona

- Registra usuarios con ninguno, uno y varios intereses.
- Comprueba que la confirmación nunca muestra la contraseña.
- Repite un correo y comprueba que la respuesta tiene estado `409 Conflict`.
- Manipula una petición para enviar una opción desconocida y comprueba el estado `400 Bad Request`.
- Introduce `<strong>Ana</strong>` como nombre y comprueba que se muestra como texto.
- Intenta abrir `/WEB-INF/vistas/confirmacion.jsp` directamente y comprueba que no es accesible.

## Explicación guiada

`request.setAttribute` permite que el controlador entregue datos a la vista sin añadirlos a la URL. Después, `RequestDispatcher#forward` continúa la misma petición dentro del servidor. Por eso la JSP puede consultar `${usuario.nombre}` y `${totalUsuarios}`.

Los nombres utilizados por Expression Language corresponden a propiedades Java. La expresión `${usuario.nombre}` llama de forma indirecta a `usuario.getNombre()`.

## Errores frecuentes

- Acceder directamente a una JSP guardada dentro de `WEB-INF`.
- Confundir parámetros del formulario con atributos creados por el servlet.
- Leer parámetros antes de establecer la codificación UTF-8.
- Usar `getParameter` cuando un control puede enviar varios valores.
- Confiar únicamente en la validación HTML del navegador.
- Mostrar valores externos con `${...}` sin escaparlos cuando podrían contener HTML.
- Creer que `POST` sustituye a HTTPS.

## Propuestas para practicar

1. Añade una lista desplegable con el turno y valida sus opciones.
2. Exige al menos un interés y muestra un mensaje específico.
3. Añade una casilla obligatoria para aceptar las condiciones.
4. Conserva en el formulario los valores no sensibles cuando falle la validación.
5. Traduce los códigos de intereses a textos más descriptivos antes de mostrarlos.
