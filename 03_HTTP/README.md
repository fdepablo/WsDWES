# Fundamentos del protocolo HTTP

Este módulo permite observar cómo se comunican un cliente y un servidor mediante HTTP. El ejemplo responde en texto plano para que la atención se centre en los métodos, las rutas, las cabeceras, los cuerpos y los códigos de estado, no en la generación de páginas web.

No pretende construir todavía una API REST completa. Utiliza un recurso simulado con identificador `15` para experimentar con HTTP mediante Postman o `curl`.

## Qué vas a aprender

- Reconocer las partes de una petición y una respuesta HTTP.
- Diferenciar método, ruta, cabeceras y cuerpo.
- Utilizar `GET`, `POST`, `PUT` y `DELETE` según su finalidad habitual.
- Comprender las propiedades de seguridad e idempotencia de los métodos.
- Interpretar las cinco familias de códigos de estado.
- Devolver estados como `200`, `201`, `204`, `303`, `400`, `404`, `409` y `415`.
- Utilizar las cabeceras `Content-Type` y `Location`.
- Distinguir una redirección HTTP de un `forward` interno.
- Inspeccionar mensajes con Postman y `curl`.

## Requisitos

- Java 25.
- Maven 3.9 o compatible.
- Apache Tomcat 11.
- IntelliJ IDEA. En la edición Community puede utilizarse Smart Tomcat.
- Postman o `curl`. Windows 10 y 11 incluyen habitualmente `curl.exe`.

## Formato de los mensajes HTTP

### Petición

Una petición HTTP contiene una línea inicial, cabeceras, una línea vacía y, opcionalmente, un cuerpo:

```http
POST /http/recursos HTTP/1.1
Host: localhost:8080
Content-Type: text/plain;charset=UTF-8
Accept: */*
Content-Length: 13

Nuevo recurso
```

- `POST` es el método.
- `/http/recursos` es el recurso solicitado.
- `HTTP/1.1` es la versión representada en este ejemplo textual.
- `Host`, `Content-Type`, `Accept` y `Content-Length` son cabeceras.
- `Nuevo recurso` es el cuerpo.

### Respuesta

Una posible respuesta del servidor sería:

```http
HTTP/1.1 201 Created
Content-Type: text/plain;charset=UTF-8
Location: /http/recursos/15
Content-Length: 45

Recurso creado con el contenido: Nuevo recurso
```

La primera línea contiene la versión, el código y su descripción. Después aparecen las cabeceras, una línea vacía y el cuerpo.

> [!NOTE]
> Postman y `curl` presentan los mensajes de forma más cómoda y pueden ocultar algunos detalles de transporte. Los bloques anteriores son representaciones didácticas; cabeceras como `Content-Length`, `Date` o `Connection` pueden variar porque Tomcat o el cliente las calculan.

## Métodos principales

| Método | Uso habitual | ¿Seguro? | ¿Idempotente? | ¿Formulario HTML normal? |
|---|---|---:|---:|---:|
| `GET` | Consultar un recurso | Sí | Sí | Sí |
| `POST` | Crear o procesar información | No | No necesariamente | Sí |
| `PUT` | Sustituir un recurso conocido | No | Sí | No |
| `DELETE` | Eliminar un recurso | No | Sí | No |

Un método **seguro** no debería modificar el estado del servidor. Un método **idempotente** produce el mismo efecto final aunque se repita varias veces. Esto no significa que todas sus respuestas tengan que ser idénticas.

Los formularios HTML permiten directamente `GET` y `POST`. Para practicar `PUT` y `DELETE` utilizaremos Postman o `curl`.

## Familias de códigos de estado

| Familia | Significado | Ejemplos |
|---|---|---|
| `1xx` | Respuesta informativa | `100 Continue` |
| `2xx` | La petición se procesó correctamente | `200 OK`, `201 Created`, `204 No Content` |
| `3xx` | El cliente debe realizar otra acción | `301 Moved Permanently`, `303 See Other` |
| `4xx` | Hay un problema con la petición del cliente | `400`, `401`, `403`, `404`, `409`, `415` |
| `5xx` | El servidor no pudo completar una petición válida | `500`, `503` |

Algunos códigos especialmente importantes son:

- `200 OK`: operación correcta con una representación en la respuesta.
- `201 Created`: se ha creado un recurso; suele acompañarse de `Location`.
- `204 No Content`: operación correcta sin cuerpo de respuesta.
- `303 See Other`: indica otra dirección que el cliente debe consultar con `GET`.
- `400 Bad Request`: los datos o la forma de la petición no son válidos.
- `401 Unauthorized`: falta autenticación válida, pese a su nombre histórico.
- `403 Forbidden`: la identidad puede ser conocida, pero no tiene permiso.
- `404 Not Found`: no existe el recurso solicitado.
- `405 Method Not Allowed`: el recurso no admite ese método.
- `409 Conflict`: la operación entra en conflicto con el estado actual.
- `415 Unsupported Media Type`: el formato del cuerpo no es aceptado.
- `500 Internal Server Error`: se produjo un fallo inesperado en el servidor.
- `503 Service Unavailable`: el servicio no está disponible temporalmente.

## Cabeceras HTTP importantes

Las cabeceras añaden información sobre la petición, la respuesta o su cuerpo. Su nombre no distingue entre mayúsculas y minúsculas, aunque normalmente se escriben como `Content-Type` o `Set-Cookie` para facilitar su lectura.

Una misma cabecera no tiene por qué aparecer en todos los mensajes. Algunas pertenecen principalmente a las peticiones, otras a las respuestas y otras pueden utilizarse en ambos sentidos.

| Cabecera | Dirección habitual | Finalidad |
|---|---|---|
| `Host` | Petición | Indica el nombre y, si es necesario, el puerto del servidor solicitado. |
| `Accept` | Petición | Indica qué formatos de respuesta puede procesar el cliente. |
| `Content-Type` | Ambas | Describe el formato del cuerpo que se está enviando. |
| `Content-Length` | Ambas | Indica el tamaño del cuerpo en bytes. Normalmente lo calcula el cliente o el servidor. |
| `Authorization` | Petición | Transporta las credenciales definidas por un esquema de autenticación. |
| `Cookie` | Petición | Devuelve al servidor cookies almacenadas previamente por el cliente. |
| `Set-Cookie` | Respuesta | Solicita al cliente que almacene una cookie. |
| `Location` | Respuesta | Indica la ubicación de un recurso creado o el destino de una redirección. |
| `Cache-Control` | Ambas | Define cómo pueden almacenar temporalmente la respuesta los clientes y servidores intermedios. |

### `Content-Type` y los tipos MIME

`Content-Type` describe la representación incluida en el **cuerpo del mensaje actual**. Puede aparecer tanto en una petición como en una respuesta:

```http
Content-Type: text/plain;charset=UTF-8
```

El valor principal se denomina **tipo de medio** o **tipo MIME** y sigue normalmente esta estructura:

```text
tipo/subtipo; parámetro=valor
```

En el ejemplo anterior:

- `text` es el tipo principal.
- `plain` es el subtipo.
- `charset=UTF-8` es un parámetro que indica cómo se han codificado los caracteres.

Algunos tipos frecuentes son:

| Tipo MIME | Contenido representado |
|---|---|
| `text/plain` | Texto sin formato. |
| `text/html` | Documento HTML. |
| `text/css` | Hoja de estilos CSS. |
| `application/json` | Documento JSON. |
| `application/xml` | Documento XML de propósito general. |
| `application/pdf` | Documento PDF. |
| `application/x-www-form-urlencoded` | Formulario codificado como parejas nombre-valor. |
| `multipart/form-data` | Formulario dividido en partes, habitual al subir archivos. |
| `image/jpeg` | Imagen JPEG. |
| `image/png` | Imagen PNG. |
| `image/svg+xml` | Imagen vectorial SVG. |

La lista completa y oficial de tipos registrados puede consultarse en el [registro de tipos de medios de IANA](https://www.iana.org/assignments/media-types). Está organizada por categorías como `application`, `audio`, `font`, `image`, `text` y `video`.

`Content-Type` no debe confundirse con `Accept`:

```http
POST /http/recursos HTTP/1.1
Content-Type: text/plain;charset=UTF-8
Accept: application/json

Nuevo recurso
```

En esa petición, `Content-Type` dice que el cliente **envía texto**, mientras que `Accept` comunica que preferiría **recibir JSON**. El servidor puede rechazar un cuerpo cuyo formato no admite con `415 Unsupported Media Type`. Si no puede generar ninguno de los formatos solicitados mediante `Accept`, puede responder con `406 Not Acceptable`.

> [!IMPORTANT]
> Declarar un `Content-Type` no transforma el contenido. Si se indica `application/json`, el cuerpo debe contener realmente JSON válido. Tampoco debe confundirse el tipo MIME con la codificación de caracteres: `text/plain` describe el formato y `charset=UTF-8` indica cómo convertir sus bytes en texto.

### `Cookie` y `Set-Cookie`

HTTP no recuerda automáticamente las peticiones anteriores. Las cookies permiten que el cliente conserve un dato pequeño y lo envíe de nuevo en peticiones posteriores.

El servidor crea o actualiza una cookie mediante una cabecera de respuesta `Set-Cookie`:

```http
HTTP/1.1 200 OK
Set-Cookie: tema=oscuro; Path=/; Max-Age=3600; HttpOnly; Secure; SameSite=Lax
```

Si la cookie continúa vigente y coincide con el dominio y la ruta solicitados, el navegador la devuelve mediante la cabecera de petición `Cookie`:

```http
GET /preferencias HTTP/1.1
Host: ejemplo.com
Cookie: tema=oscuro
```

Por tanto, son cabeceras diferentes:

- `Set-Cookie` viaja normalmente del servidor al cliente y contiene una cookie junto con sus atributos.
- `Cookie` viaja del cliente al servidor y reúne los nombres y valores aplicables a esa petición.

Algunos atributos importantes de `Set-Cookie` son:

- `Path`: limita las rutas en las que debe enviarse.
- `Max-Age` o `Expires`: establece su duración. Sin ellos suele ser una cookie de sesión.
- `HttpOnly`: impide que JavaScript acceda a ella, lo que reduce determinados riesgos de robo.
- `Secure`: hace que el navegador solo la envíe mediante HTTPS, salvo consideraciones especiales de desarrollo local.
- `SameSite`: controla su envío en peticiones iniciadas desde otros sitios y ayuda a reducir ataques CSRF.

Las cookies no deben utilizarse para guardar contraseñas ni información sensible directamente. En la gestión de sesiones suele almacenarse en el navegador únicamente un identificador aleatorio; los datos de la sesión permanecen en el servidor.

### `Location`

`Location` se utiliza principalmente en dos situaciones:

```http
HTTP/1.1 201 Created
Location: /http/recursos/15
```

Aquí indica dónde se encuentra el recurso creado. En una respuesta `3xx`, señala la URL que el cliente debe consultar:

```http
HTTP/1.1 303 See Other
Location: /http/recursos/15
```

El ejemplo `/redirigir` permite observar este segundo caso.

## Estructura relevante

```text
03_HTTP/
├── pom.xml
├── README.md
└── src/main/
    ├── java/servlets/
    │   ├── RecursoServlet.java
    │   └── RedireccionServlet.java
    └── webapp/
        └── index.html
```

`RecursoServlet` implementa los cuatro métodos principales. `RedireccionServlet` devuelve deliberadamente un `303` y una cabecera `Location`.

## Cómo compilar

Desde la raíz del workspace:

```bash
mvn -pl 03_HTTP -am clean package
```

El resultado será `03_HTTP/target/03_HTTP-1.0-SNAPSHOT.war`.

## Cómo ejecutar con Smart Tomcat

1. Recarga los proyectos desde la ventana **Maven**.
2. Crea o duplica una configuración de tipo **Smart Tomcat**.
3. Selecciona Tomcat 11.
4. Usa `03_HTTP/src/main/webapp` como **Deployment Directory**.
5. Selecciona `03_HTTP` en **Use classpath of module**.
6. Utiliza `/http` como **Context Path**.
7. Ejecuta Tomcat.

> [!WARNING]
> **Deployment Directory** y **Use classpath of module** deben apuntar siempre a `03_HTTP`.

## Pruebas con Postman

En lugar de importar una colección, crea cada petición manualmente. El objetivo es identificar qué partes del mensaje HTTP estás configurando.

### Consultar un recurso

1. Crea una nueva petición.
2. Selecciona el método `GET`.
3. Escribe `http://localhost:8080/http/recursos/15`.
4. Pulsa **Send**.
5. Comprueba el estado `200 OK`.
6. Abre **Headers** y localiza `Content-Type` y `X-Ejemplo`.
7. Abre **Body** y observa el texto enviado por el servidor.

Cambia el identificador `15` por `99` y repite la petición. En este caso debes recibir `404 Not Found`.

### Crear un recurso

1. Crea una petición con el método `POST`.
2. Usa la URL `http://localhost:8080/http/recursos`.
3. Abre **Headers** y añade `Content-Type` con el valor `text/plain;charset=UTF-8`.
4. Abre **Body**, selecciona **raw** y elige **Text** como formato.
5. Escribe `Nuevo recurso`.
6. Pulsa **Send**.
7. Comprueba el estado `201 Created`, la cabecera `Location` y el cuerpo de la respuesta.

Cambia el cuerpo por `duplicado`. La respuesta debe ser `409 Conflict`.

### Actualizar un recurso

1. Crea una petición con el método `PUT`.
2. Usa la URL `http://localhost:8080/http/recursos/15`.
3. Añade la cabecera `Content-Type: text/plain;charset=UTF-8`.
4. En **Body**, selecciona **raw**, elige **Text** y escribe `Recurso actualizado`.
5. Envía la petición y comprueba el estado `200 OK`.

### Eliminar un recurso

1. Crea una petición con el método `DELETE`.
2. Usa la URL `http://localhost:8080/http/recursos/15`.
3. Pulsa **Send**.
4. Comprueba el estado `204 No Content`.
5. Observa que la respuesta no contiene cuerpo.

### Observar una redirección

1. Desactiva temporalmente **Automatically follow redirects** en la configuración de Postman.
2. Crea una petición `GET` a `http://localhost:8080/http/redirigir`.
3. Pulsa **Send**.
4. Comprueba el estado `303 See Other` y la cabecera `Location`.
5. Vuelve a activar el seguimiento automático cuando termines la práctica.

Si no se desactiva esa opción, Postman realiza automáticamente la segunda petición y puede mostrar directamente el `200 OK` del recurso de destino, ocultando la respuesta intermedia `303`.

## Pruebas equivalentes con `curl`

La opción `-i` incluye las cabeceras de respuesta. En Windows se utiliza `curl.exe` para evitar posibles alias de PowerShell.

### Consultar un recurso: `200 OK`

```bash
curl.exe -i http://localhost:8080/http/recursos/15
```

### Consultar un recurso inexistente: `404 Not Found`

```bash
curl.exe -i http://localhost:8080/http/recursos/99
```

### Crear un recurso: `201 Created`

```bash
curl.exe -i -X POST -H "Content-Type: text/plain;charset=UTF-8" --data "Nuevo recurso" http://localhost:8080/http/recursos
```

Observa tanto el cuerpo como la cabecera `Location`.

### Provocar un conflicto: `409 Conflict`

```bash
curl.exe -i -X POST -H "Content-Type: text/plain;charset=UTF-8" --data "duplicado" http://localhost:8080/http/recursos
```

### Actualizar el recurso: `200 OK`

```bash
curl.exe -i -X PUT -H "Content-Type: text/plain;charset=UTF-8" --data "Recurso actualizado" http://localhost:8080/http/recursos/15
```

### Eliminar el recurso: `204 No Content`

```bash
curl.exe -i -X DELETE http://localhost:8080/http/recursos/15
```

La respuesta no debe contener cuerpo.

### Observar una redirección: `303 See Other`

```bash
curl.exe -i http://localhost:8080/http/redirigir
```

Sin `-L`, `curl` muestra el `303` y no sigue automáticamente la dirección indicada en `Location`. Puedes repetir la petición con `-L` para comparar el resultado.

## Redirección y `forward`

Una redirección es visible para el cliente y provoca otra petición:

```text
Navegador ── GET /redirigir ──► servidor
Navegador ◄── 303 + Location ── servidor
Navegador ── GET /recursos/15 ► servidor
```

Un `forward` ocurre enteramente dentro del servidor:

```text
Navegador ── GET /inicio ──► servlet ── forward ──► JSP
Navegador ◄──────────────────── respuesta HTML ─────┘
```

En el `forward` se conserva la petición y la URL del navegador no cambia. El siguiente módulo utiliza este mecanismo para conectar un servlet con una JSP.

## Cómo comprobar que funciona

- Las peticiones creadas manualmente en Postman devuelven los estados esperados.
- `GET /recursos/15` devuelve `200` y la cabecera `X-Ejemplo`.
- `POST /recursos` devuelve `201` y `Location`.
- Un cuerpo vacío devuelve `400`.
- Un cuerpo con un tipo diferente de `text/plain` devuelve `415`.
- `DELETE /recursos/15` devuelve `204` sin cuerpo.
- `/redirigir` devuelve `303` antes de seguir la redirección.
- Un método no implementado por el servlet produce `405` automáticamente.

## Explicación guiada

Tomcat analiza el mensaje HTTP y crea `HttpServletRequest` y `HttpServletResponse`. Según el método recibido, `HttpServlet` llama a `doGet`, `doPost`, `doPut` o `doDelete`.

`request.getPathInfo()` permite obtener la parte de la ruta situada después de `/recursos`. `request.getContentType()` consulta el formato declarado y `request.getReader()` permite leer el cuerpo como texto.

En la respuesta, `setStatus` fija el código, `setHeader` añade una cabecera y `setContentType` declara el formato del cuerpo. Tomcat completa otras partes del mensaje HTTP.

## Errores frecuentes

- Confundir el método HTTP con el nombre de una operación Java.
- Enviar un cuerpo sin indicar su `Content-Type`.
- Interpretar `POST` como una medida de cifrado o seguridad.
- Devolver siempre `200`, incluso cuando se ha creado, eliminado o rechazado algo.
- Incluir un cuerpo en una respuesta `204 No Content`.
- Seguir automáticamente una redirección y no observar la respuesta `3xx` intermedia.
- Confundir `401 Unauthorized` con `403 Forbidden`.
- Suponer que idempotente significa que la respuesta siempre es idéntica.

## Propuestas para practicar

1. Envía `POST` sin cuerpo y explica el `400` recibido.
2. Envía el cuerpo como `application/json` y explica el `415`.
3. Prueba el método `PATCH` y observa el `405` generado por el servlet.
4. Añade una cabecera propia a la petición y recupérala con `getHeader`.
5. Crea un endpoint que devuelva `503` junto con la cabecera `Retry-After`.
6. Compara la redirección con y sin seguimiento automático en Postman y `curl`.
