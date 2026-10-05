# Formulario procesado con Servlet y JSP

Este módulo muestra el procesamiento completo de un formulario HTML. Un servlet muestra el formulario y otro recibe los datos y solicita su registro al gestor, mientras que varias páginas JSP se ocupan exclusivamente de generar las vistas.

**URL de entrada:** [http://localhost:8080/05_Formulario/formulario](http://localhost:8080/05_Formulario/formulario), al ejecutar con el contexto `/05_Formulario` indicado más abajo.

## Qué vas a aprender

- Enviar un formulario mediante `POST`.
- Leer valores con `getParameter` y `getParameterValues`.
- Tratar correctamente un grupo de casillas opcional.
- Validar en el servidor campos obligatorios y opciones permitidas.
- Usar JSTL para tomar decisiones, recorrer una colección y escapar salidas.
- Introducción al modelo de capas: separar el controlador, el modelo, la lógica de negocio y las vistas.
- Comprender cómo `synchronized` protege datos compartidos entre peticiones.

Los requisitos comunes están en el [README raíz](../README.md).

## Conceptos clave

### El servlet controla y la JSP presenta

`FormularioServlet` muestra el formulario al recibir un `GET /formulario`. `RegistroServlet` recibe el `POST /registro`, solicita el registro a `GestorUsuarios` y decide si mostrar la confirmación o un error. Las JSP contienen el HTML y muestran los atributos preparados por los servlets. De este modo no se construye la página mediante `PrintWriter`.

`@WebServlet("/formulario")` asigna `FormularioServlet` a la ruta `/formulario`, relativa al contexto de la aplicación. Con el contexto `/05_Formulario`, la URL de entrada es `/05_Formulario/formulario`. El formulario envía los datos a `${pageContext.request.contextPath}/registro`, y los enlaces para volver apuntan a `${pageContext.request.contextPath}/formulario`. `RegistroServlet` solo implementa `doPost`: abrir `/05_Formulario/registro` desde la barra del navegador envía un `GET` y devuelve `405 Method Not Allowed`.

Las vistas se guardan en `WEB-INF/vistas`, por lo que el navegador no puede abrirlas directamente. Solo el servlet puede llegar a ellas mediante un `forward`.

### `POST` y seguridad de la contraseña

El formulario usa `POST` porque el registro modifica el estado de la aplicación. Esto evita mostrar los parámetros en la URL, pero no los cifra: una aplicación real también necesita HTTPS.

Por simplificación didáctica, este ejemplo incluye la contraseña en `Usuario` y la conserva en texto plano en la lista del gestor, únicamente en memoria: no utiliza una base de datos. No se muestra en las JSP ni se escribe en logs. Usa contraseñas ficticias para probarlo.

En una aplicación real, la base de datos guarda un **hash de la contraseña**, generado mediante un algoritmo específico para contraseñas y una **sal aleatoria** por contraseña. La sal evita que dos contraseñas iguales produzcan el mismo hash almacenado. El hash no se descifra para recuperar el texto original: al iniciar sesión se verifica la contraseña introducida mediante el algoritmo y la sal guardados. HTTPS protege el envío; el hash protege el almacenamiento.

### Parámetros con varios valores

Las casillas comparten `name="intereses"`. El servlet usa `getParameterValues`, que devuelve un array cuando hay selecciones y `null` cuando no se marca ninguna. El código convierte ambos casos en una lista que puede manejar de forma uniforme.

### JSTL y salida segura

**JSTL** (*JavaServer Pages Standard Tag Library*, actualmente Jakarta Standard Tag Library) es una biblioteca de etiquetas que permite recorrer datos, tomar decisiones y mostrar valores en una JSP sin escribir bloques de código Java. En este módulo, el `pom.xml` incluye su API y su implementación.

Las JSP que la utilizan declaran la biblioteca de etiquetas básicas (*core*):

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```

`prefix="c"` permite escribir etiquetas como `<c:out>`. La URI identifica la biblioteca; no es una dirección que el navegador deba visitar. Las etiquetas se ejecutan en el servidor y el navegador recibe el HTML resultante.

| Etiqueta utilizada | Función en este ejemplo |
| --- | --- |
| `<c:out value="${usuario.nombre}"/>` | Muestra el valor y escapa por defecto caracteres especiales como `<`, `>` y `&`, para que el nombre se presente como texto. También se usa para los intereses, el contador y los mensajes de error. |
| `<c:choose>` | Agrupa las alternativas de presentación: mostrar «Ninguno» o la lista de intereses. |
| `<c:when test="${empty usuario.intereses}">` | Ejecuta su contenido si la condición es verdadera. `empty` comprueba si la lista es nula o está vacía. |
| `<c:otherwise>` | Ejecuta su contenido si ninguna condición de los `<c:when>` anteriores se cumple. |
| `<c:forEach items="${usuario.intereses}" var="interes">` | Recorre la lista; en cada vuelta, `interes` contiene el elemento actual y se genera un `<li>`. |

Las expresiones `${...}` pertenecen a **Expression Language (EL)** y permiten consultar los datos; JSTL utiliza esos valores para decidir qué HTML generar. Por ejemplo, `${usuario.nombre}` obtiene el nombre, mientras que `<c:out>` se ocupa de mostrarlo escapado. Escribir únicamente `${usuario.nombre}` en el HTML no lo escapa automáticamente.

### Estado temporal

`GestorUsuarios` conserva los registros en memoria y evita correos duplicados. Sus métodos están sincronizados porque Tomcat puede atender varias peticiones al mismo tiempo. Los datos desaparecen al reiniciar o redesplegar la aplicación.

### Validación y códigos de resultado

`GestorUsuarios.registrar(Usuario)` comprueba el nombre, el formato y longitud del correo, la contraseña, los intereses y el tipo de cuenta. Después comprueba si el correo ya existe. Devuelve el código del primer error encontrado y solo modifica la lista cuando todas las comprobaciones pasan.

| Constante del gestor | Código | Respuesta del servlet |
| --- | --- | --- |
| `REGISTRO_CORRECTO` | 0 | Confirmación (`200 OK`) |
| `ERROR_EMAIL` | 1 | `400 Bad Request`: correo inválido |
| `ERROR_INTERESES` | 2 | `400 Bad Request`: interés no permitido |
| `ERROR_TIPO_CUENTA` | 3 | `400 Bad Request`: tipo de cuenta inválido |
| `ERROR_NOMBRE` | 4 | `400 Bad Request`: nombre vacío o demasiado largo |
| `EMAIL_DUPLICADO` | 5 | `409 Conflict`: correo ya registrado |
| `ERROR_PASSWORD` | 6 | `400 Bad Request`: contraseña nula o fuera de 8–72 caracteres |

Son códigos internos del ejemplo, no estados HTTP. Las constantes expresan su significado sin repetir números sueltos. El `switch` del servlet convierte el resultado en una respuesta HTTP y un mensaje para la vista; un código desconocido se trata como error interno (`500`). El gestor no depende de la API de servlets.

El gestor valida en este orden: nombre, correo, contraseña, intereses, tipo de cuenta y correo duplicado. Una contraseña inválida devuelve `ERROR_PASSWORD`, que el servlet traduce a `400`. La expresión regular del correo es una comprobación básica para este ejercicio; no demuestra que la dirección exista.

### Un gestor compartido y un usuario por registro

Tomcat reutiliza la instancia de `RegistroServlet` para atender peticiones. El atributo `private final GestorUsuarios gestorUsuarios = new GestorUsuarios()` se inicializa al crear esa instancia del servlet, por lo que sus peticiones usan el mismo gestor. Esto permite conservar la lista entre peticiones, detectar correos ya registrados y contar todos los registros almacenados.

Si se hiciera `new GestorUsuarios()` dentro de `doPost`, cada petición empezaría con una lista vacía: aceptaría correos registrados en peticiones anteriores y el contador devolvería `1` después de registrar. Ese gestor dejaría de estar disponible para las siguientes peticiones. En cambio, `new Usuario(...)` sí se ejecuta para cada envío, porque representa los datos recibidos. El gestor valida ese objeto y solo lo añade a su lista si acepta el registro.

`final` impide sustituir la referencia `gestorUsuarios` por otro objeto, pero no impide modificar su lista ni protege el acceso concurrente. La necesidad de sincronizar aparece porque varios hilos acceden a datos compartidos que pueden cambiar; tener un objeto compartido, por sí solo, no obliga a usar `synchronized`.

### Peticiones concurrentes y `synchronized`

Tomcat puede ejecutar varias peticiones simultáneamente mediante distintos **hilos de ejecución**. Estas peticiones pueden usar la misma instancia de `RegistroServlet` y, por tanto, el mismo objeto `gestorUsuarios` y su lista de usuarios. Si varios hilos acceden a datos compartidos sin coordinación, pueden producirse resultados incorrectos.

Por ejemplo, sin sincronización podrían intercalarse dos registros del mismo correo:

1. La petición A comprueba que `ana@ejemplo.com` no existe.
2. La petición B comprueba ese correo antes de que A lo añada y tampoco lo encuentra.
3. A añade su usuario y B añade el suyo: el correo queda duplicado.

Este problema se llama **condición de carrera**: el resultado depende del orden en que se intercalan las operaciones de los hilos.

En `GestorUsuarios`, la declaración `public synchronized int registrar(Usuario usuario)` hace que el hilo adquiera el **monitor**, o bloqueo, del objeto antes de ejecutar el método. Mientras lo mantiene, ningún otro hilo puede ejecutar un método `synchronized` sobre **esa misma instancia**. Los otros hilos esperan; el bloqueo se libera al salir del método, tanto con `return` como por una excepción.

Así, comprobar el correo y añadir el usuario forman una operación indivisible respecto a los otros métodos sincronizados del gestor. La segunda petición espera a que termine la primera, encuentra el correo y obtiene `EMAIL_DUPLICADO`. El servlet responde entonces con `409 Conflict`.

`contarUsuarios()` también es `synchronized`: utiliza el mismo bloqueo para leer el tamaño de la lista sin que otro hilo la modifique durante esa lectura. Además, la sincronización garantiza que un hilo que adquiere el mismo monitor vea los cambios realizados por el anterior antes de liberarlo.

Hay que tener en cuenta sus límites:

- El bloqueo pertenece al objeto. Dos instancias distintas de `GestorUsuarios` tienen bloqueos independientes.
- No bloquea automáticamente los métodos sin `synchronized`; todos los accesos a la lista deben seguir la misma coordinación. Aquí la lista es privada y solo se accede a ella desde los dos métodos sincronizados.
- Cada llamada se protege por separado. Entre `registrar()` y `contarUsuarios()` puede registrarse otro usuario, por lo que el total mostrado incluye los registros existentes al realizar la consulta.
- No conserva los datos ni coordina servidores distintos. En una aplicación con base de datos, la unicidad del correo se garantizaría también mediante una restricción de unicidad y el manejo adecuado de las operaciones en la base de datos.

## Estructura relevante

```text
05_Formulario/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/Usuario.java
    │   ├── servicios/GestorUsuarios.java
    │   └── servlets/
    │       ├── FormularioServlet.java
    │       └── RegistroServlet.java
    └── webapp/WEB-INF/vistas/
        ├── formulario.jsp
        ├── registro.jsp
        └── error.jsp
```

## Recorrido del ejemplo

1. `GET /05_Formulario/formulario` ejecuta `FormularioServlet` y hace un `forward` a `formulario.jsp`.
2. El navegador envía los campos mediante `POST /05_Formulario/registro` a `RegistroServlet`.
3. El servlet establece UTF-8 antes de leer los parámetros.
4. El servlet crea un `Usuario` con los datos recibidos, incluida la contraseña.
5. Si hay un error, el servlet establece el estado HTTP adecuado y muestra `error.jsp`.
6. El gestor valida el usuario y comprueba duplicados; solo lo añade si todo es correcto. El servlet traduce los errores del gestor a estados HTTP y mensajes.
7. El servlet añade el usuario a la petición y hace `forward` a `registro.jsp`.
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
6. Utiliza `/05_Formulario` como **Context Path**.
7. Abre la URL de entrada: `http://localhost:8080/05_Formulario/formulario`.

> [!WARNING]
> **Deployment Directory** y **Use classpath of module** deben apuntar al mismo módulo.

## Cómo comprobar que funciona

- Abre `/05_Formulario/formulario` y comprueba que aparece el formulario.
- Tras un registro o un error, pulsa **Volver al formulario** y comprueba que regresas a `/05_Formulario/formulario`.
- Abre `/05_Formulario/registro` directamente y comprueba el estado `405 Method Not Allowed`: esa ruta procesa envíos mediante `POST`.
- Registra usuarios con ninguno, uno y varios intereses.
- Comprueba que la confirmación nunca muestra la contraseña.
- Repite un correo y comprueba que la respuesta tiene estado `409 Conflict`.
- Manipula una petición para enviar una opción desconocida y comprueba el estado `400 Bad Request`.
- Introduce `<strong>Ana</strong>` como nombre y comprueba que se muestra como texto.
- Intenta abrir `/WEB-INF/vistas/registro.jsp` directamente y comprueba que no es accesible.

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
- Creer que `synchronized` bloquea todos los objetos de una clase o convierte varias llamadas consecutivas en una única operación indivisible.

## Propuestas para practicar

1. Añade una lista desplegable con el turno y valida sus opciones.
2. Exige al menos un interés y muestra un mensaje específico.
3. Añade una casilla obligatoria para aceptar las condiciones.
4. Conserva en el formulario los valores no sensibles cuando falle la validación.
5. Traduce los códigos de intereses a textos más descriptivos antes de mostrarlos.
