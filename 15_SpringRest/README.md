# Spring REST con Spring Data JPA

Este módulo adapta `WorkspaceJava/27_SpringBootRest` a Java 25, Spring Boot 4 e IntelliJ. Conserva los dos ejemplos del original: respuestas sencillas de texto y HTML, y un CRUD de personas en JSON con filtro opcional por nombre. Todas las rutas comienzan por `/rest`. La lista y el DAO manuales se sustituyen por Spring Data JPA y H2, como en [`12_SpringDataJPA`](../12_SpringDataJPA/README.md). La carpeta `img` del proyecto antiguo no se traslada; el PDF [*REST by the Simpsons*](pdf/REST%20by%20the%20Simpsons.pdf) queda como lectura complementaria.

## Qué vas a aprender

- Distinguir las partes de una petición y una respuesta HTTP y elegir el método y estado adecuados.
- Relacionar recursos y rutas REST con `GET`, `POST`, `PUT` y `DELETE`.
- Explicar la filosofía REST y los cinco puntos prácticos del PDF complementario.
- Usar `@RestController`, `@RequestBody`, `@PathVariable`, `@RequestParam` y `ResponseEntity`.
- Observar cómo Spring convierte JSON en `Persona` y viceversa.
- Reutilizar `JpaRepository` para que el CRUD actúe sobre H2 en vez de sobre posiciones de una lista.

## Requisitos y ejecución

Necesitas Java 25, Maven e IntelliJ IDEA; los requisitos comunes están en el [README raíz](../README.md). **Para probar el CRUD completo necesitas un cliente HTTP**, por ejemplo [Postman](https://www.postman.com/downloads/) o `curl`. No necesitas instalar Tomcat ni H2: Boot arranca el servidor integrado y H2 se crea en memoria.

En IntelliJ, recarga Maven y ejecuta `serviciorest.Aplicacion.main`. La API escucha en `http://localhost:8080/rest`, pero **no tiene interfaz de usuario ni formularios**. Pegar una URL en la barra del navegador solo envía una petición GET; para elegir POST, PUT o DELETE, añadir cabeceras y escribir el cuerpo JSON usa Postman o `curl`. Si el puerto 8080 está ocupado, cambia `server.port` en `application.properties` y ajusta las URL de las pruebas.

## Recordatorio de HTTP

HTTP es el protocolo de comunicación entre cliente y servidor. Cada petición es independiente: el servidor no recuerda automáticamente las anteriores. En [`03_HTTP`](../03_HTTP/README.md) se estudian con más detalle sus mensajes, cabeceras, métodos y códigos de estado.

Una petición HTTP/1.1 tiene línea inicial (método, ruta y versión), cabeceras, una línea vacía y, si procede, cuerpo. Por ejemplo:

```http
POST /rest/personas HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json

{"nombre":"ANA","apellidos":"GARCÍA","edad":25}
```

La respuesta tiene línea de estado (versión y código), cabeceras, línea vacía y, cuando corresponde, cuerpo:

```http
HTTP/1.1 201 Created
Content-Type: application/json

{"id":6,"nombre":"ANA","apellidos":"GARCÍA","edad":25}
```

El ID del ejemplo es orientativo: lo genera H2. `Content-Type` describe el cuerpo **enviado**; `Accept` indica qué tipo de respuesta acepta el cliente. `application/json` identifica JSON; `text/plain` es texto sin formato y `text/html` es HTML. No basta con declarar JSON en la cabecera: el cuerpo también debe ser JSON válido. El servidor o el cliente pueden añadir otras cabeceras, por lo que no esperes una respuesta byte por byte idéntica a este esquema.

| Método | Uso en la API | ¿Seguro? | ¿Idempotente? |
| --- | --- | --- | --- |
| `GET` | Consultar una persona o la colección. | Sí | Sí |
| `POST` | Crear una persona nueva. | No | No necesariamente |
| `PUT` | Sustituir los campos de una persona existente. | No | Sí |
| `DELETE` | Eliminar una persona. | No | Sí |

«Seguro» significa que no debería modificar el estado. «Idempotente» significa que repetir la petición deja el mismo **efecto final**, aunque el código de respuesta pueda cambiar; por ejemplo, borrar dos veces el mismo ID devuelve primero `200` y después `404`. Entre las respuestas habituales están `200 OK` (consulta, cambio o borrado correcto), `201 Created` (alta), `400 Bad Request` (datos no válidos), `404 Not Found` (ID inexistente), `405 Method Not Allowed` (método no admitido), `415 Unsupported Media Type` (formato de entrada incorrecto) y `500 Internal Server Error` (fallo inesperado). Las familias son `1xx` informativas, `2xx` éxito, `3xx` redirecciones, `4xx` errores del cliente y `5xx` errores del servidor.

## Filosofía REST y cinco puntos prácticos

REST (*Representational State Transfer*) es un **estilo arquitectónico**, no un protocolo nuevo ni una biblioteca de Spring. Un cliente identifica un **recurso** mediante una URL, envía una petición HTTP y recibe una **representación** de su estado, por ejemplo JSON. Las operaciones se expresan con los métodos y la semántica de HTTP, en vez de inventar una URL distinta con un verbo en el nombre para cada acción. Cliente y servidor tienen responsabilidades separadas, y cada petición debe aportar la información necesaria para atenderla: el servidor no depende de una conversación previa almacenada en una sesión. Que la API use JSON o que una clase tenga `@RestController` no basta por sí solo para afirmar que sigue bien el estilo REST.

El PDF complementario resume su enfoque didáctico en **cinco puntos** (especialmente en sus páginas 9, 73 y 90):

1. **Asignar URL a los recursos.** `/rest/personas` identifica la colección y `/rest/personas/{id}` una persona. El nombre expresa *qué* recurso se consulta, no una acción como `/buscarPersona`.
2. **Interactuar con métodos HTTP.** `GET` consulta, `POST` crea, `PUT` actualiza y `DELETE` elimina. La misma URL puede aceptar métodos distintos con significados distintos.
3. **Permitir representaciones.** Un recurso puede presentarse en JSON, XML, HTML u otro formato negociado con el cliente. En este ejercicio el CRUD solo ofrece **JSON**; los endpoints `/rest/mensaje` y `/rest/mensajeHTML` comparan tipos de respuesta, pero no son dos representaciones negociadas del mismo recurso.
4. **Responder apropiadamente.** El estado y el cuerpo deben contar lo que ocurrió: `201` al crear, `200` al consultar o borrar y `404` cuando falta el ID. Devolver siempre `200` ocultaría errores.
5. **Aprovechar HTTP, incluidas sus cabeceras.** `Content-Type` y `Accept` describen los formatos; también existen cabeceras para caché, redirección, autenticación y peticiones condicionales. No implementamos todas esas posibilidades aquí: el objetivo es reconocer que la API se apoya en HTTP y no solo en sus URL.

Estos son los **cinco puntos prácticos del PDF**, no una lista literal de las restricciones formales de Fielding. Entre esas restricciones se encuentran cliente-servidor, ausencia de estado en las peticiones, posibilidad de caché, interfaz uniforme y sistema por capas; la ejecución de código bajo demanda es opcional. La aplicación de ejemplo ilustra parte de esa filosofía, pero no pretende demostrar todas las restricciones ni implementar negociación de formatos, caché o enlaces hipermedia.

## REST y los controladores

REST es un estilo para organizar recursos y operaciones sobre HTTP, no una biblioteca de Spring. Aquí `/rest/personas` representa la colección y `/rest/personas/{id}` una persona concreta. El método HTTP indica qué operación se solicita; el ID va en la ruta y el filtro opcional `nombre` va en la consulta, por ejemplo `/rest/personas?nombre=HARRY`. El cuerpo JSON transporta los datos de una persona. No hay páginas Thymeleaf: la respuesta es el dato, no el nombre de una vista.

`@RestController` combina el registro del controlador con la escritura del resultado en el cuerpo HTTP. Es distinto de `@Controller` en [`13_SpringMVC`](../13_SpringMVC/README.md), que selecciona una plantilla. `@RequestMapping` fija la ruta base; `@GetMapping`, `@PostMapping`, `@PutMapping` y `@DeleteMapping` concretan método y ruta. `@PathVariable` lee el ID de la ruta, `@RequestParam` el filtro de la URL y `@RequestBody` convierte el JSON entrante en `Persona`. `ResponseEntity` permite elegir el código y el cuerpo de la respuesta. `produces` indica el formato de salida y `consumes` el de entrada.

Primero prueba `GET /rest/mensaje`: devuelve texto plano. Después prueba `GET /rest/mensajeHTML`: devuelve HTML. El segundo endpoint solo sirve para comparar `Content-Type`; no es el formato habitual del CRUD REST, que utiliza JSON.

## Persistencia y datos de partida

`Persona` es una entidad JPA: `@Entity` la mapea a la tabla `personas`, `@Id` identifica su clave y `@GeneratedValue` delega el ID en H2. `PersonaRepositorio extends JpaRepository<Persona, Long>` aporta `save`, `findById` y `deleteById`; Spring Data genera también las búsquedas declaradas por nombre de método. El controlador inyecta ese repositorio por constructor. `findById` devuelve un `Optional`: si está vacío, el controlador responde `404`. El módulo 12 explica con más detalle JPA, el repositorio y `Optional`.

`DatosIniciales` guarda al arrancar las cinco personas del original: STEVE ROGERS, HARRY POTTER, CHIQUITO DE LA CALZADA, BUD SPENCER y HARRY CALLAHAN. La consulta por `nombre=HARRY` devuelve dos resultados sin distinguir mayúsculas. H2 está en memoria y `ddl-auto=create-drop` crea y elimina las tablas con la aplicación: los cambios duran durante la ejecución y se pierden al reiniciar. Los ID los asigna la base; ya **no son índices de un array**, por lo que borrar una persona no cambia el ID de las demás.

En el alta se ignora cualquier `id` enviado por el cliente y se guarda una entidad nueva. En la modificación manda el ID de la ruta: el cuerpo aporta nombre, apellidos y edad. Para simplificar el ejemplo, el mismo tipo `Persona` sirve como entidad y como representación JSON; en una API mayor convendría separar entidades y objetos de entrada/salida. El controlador comprueba nombre y apellidos no vacíos y edad no negativa; una petición mal formada o con un tipo de dato incompatible también puede producir `400` antes de entrar en el método.

## Rutas y pruebas

| Método y ruta | Resultado esperado |
| --- | --- |
| `GET /rest/mensaje` | `200`, texto plano. |
| `GET /rest/mensajeHTML` | `200`, HTML. |
| `GET /rest/personas` | `200`, JSON con cinco personas al iniciar. |
| `GET /rest/personas?nombre=HARRY` | `200`, JSON con los dos HARRY. |
| `GET /rest/personas/{id}` | `200` con la persona o `404`. |
| `POST /rest/personas` | `201` con la persona creada y su nuevo ID; `400` si faltan datos válidos. |
| `PUT /rest/personas/{id}` | `200` sin cuerpo, `404` si falta el ID, `400` si faltan datos válidos. |
| `DELETE /rest/personas/{id}` | `200` con la persona borrada o `404`. |

Para practicar con Postman, inicia primero la aplicación desde IntelliJ y crea una petición por fila de la tabla: selecciona el método, escribe la URL completa y pulsa **Send**. Observa el código de estado en la respuesta y el contenido en **Body**. En POST y PUT elige **Body → raw → JSON**, que envía `Content-Type: application/json`. Este cuerpo sirve para ambas operaciones:

```json
{"nombre":"ANA","apellidos":"GARCÍA","edad":25}
```

El recorrido recomendado es: consulta primero `/rest/personas`, crea ANA, copia el ID devuelto y úsalo para `GET`, `PUT` y `DELETE`. Tras borrar, repite el `GET` del mismo ID y comprueba el `404`. También puedes probar `GET /rest/personas?nombre=HARRY` y comparar las cabeceras de los dos endpoints de mensaje. Si envías POST/PUT sin `Content-Type: application/json`, Spring puede responder `415`.

## Estructura relevante

```text
15_SpringRest/
├── pom.xml
├── pdf/REST by the Simpsons.pdf
└── src/main/
    ├── java/serviciorest/
    │   ├── Aplicacion.java
    │   ├── controlador/{ControladorMensaje,ControladorPersona}.java
    │   └── modelo/
    │       ├── entidad/Persona.java
    │       └── persistencia/{PersonaRepositorio,DatosIniciales}.java
    └── resources/application.properties
```

## Errores frecuentes y práctica

- Escribir `/personas` en vez de `/rest/personas` o probar POST desde la barra del navegador, que envía GET.
- Confundir `@RequestParam nombre` con el ID de `@PathVariable`.
- Creer que borrar el ID `2` desplaza los demás ID como ocurría con la lista original.
- Esperar que H2 conserve los datos después de detener la aplicación.
- Enviar un cuerpo JSON sin su cabecera `Content-Type` o utilizar nombres de campos distintos de `nombre`, `apellidos` y `edad`.

Para practicar, añade al repositorio una búsqueda por apellidos y expón el filtro como otro parámetro opcional. Después prueba un ID inexistente y una petición con edad negativa, e identifica en cada respuesta el estado HTTP y el cuerpo.
