# Sesiones HTTP

Este ejemplo vamos a trabajar con la sesión web. Mientras que los atributos en la petición HTTP solo duran lo que durá la petición, en la sesión podemos guardar atributos que se mantegan durante más tiempo.

## Qué vas a aprender

- Diferenciar atributos de petición y atributos de sesión.
- Crear una sesión después de autenticar y consultarla sin crear una nueva.
- Observar cómo la cookie de sesión enlaza varias peticiones HTTP.
- Proteger rutas y cerrar sesión con `invalidate()`.
- Configurar el tiempo de inactividad y el seguimiento por cookie.
- Mostrar datos con EL y JSTL.

## Conceptos clave

### Para qué sirve una sesión

HTTP trata cada petición por separado. Después de comprobar el nombre y la contraseña en `/login`, necesitamos recordar quién ha entrado cuando el navegador pida `/inicio` o `/usuarios`.

Para ello, el servidor dispone de un objeto `HttpSession` en el que podemos guardar **atributos de sesión**. En este ejemplo guardamos un `Usuario` en el atributo `usuario`. Ese dato se puede consultar en las siguientes peticiones del mismo navegador mientras la sesión siga activa.

### Cómo reconoce el servidor la sesión

Los datos de la sesión se guardan en el servidor. El navegador conserva una cookie, normalmente llamada `JSESSIONID`, que contiene el identificador de esa sesión.

1. El servidor crea la sesión y envía su identificador al navegador mediante una cookie.
2. El navegador incluye la cookie en las siguientes peticiones a la aplicación.
3. Tomcat utiliza ese identificador para localizar la sesión y sus atributos.

La cookie contiene el identificador; el objeto `Usuario` permanece en el servidor. Por eso una ventana privada puede tener una sesión distinta de la ventana habitual.

### Crear, consultar y guardar datos en la sesión

Los atributos de sesión funcionan como una estructura de tipo hash, es decir clave valor. Estos son los métodos más útiles que podemos usar en los objetos HttpRequest y Session:

| Método | Para qué se utiliza |
|---|---|
| `request.getSession()` | Recupera la sesión actual. Si no existe, crea una nueva. Devuelve un objeto de tipo Session. |
| `request.getSession(false)` | Recupera la sesión actual sin crear otra. Si no existe, devuelve `null`. |
| `session.setAttribute("usuario", usuario)` | Guarda el objeto en la sesión con el nombre `usuario`. |
| `session.getAttribute("usuario")` | Recupera el atributo. Si no está guardado, devuelve `null`. |
| `session.invalidate()` | Invalida la sesión y elimina todos sus atributos. La cookie de sesión tambien quedaría invalidada|

En `LoginServlet`, después de comprobar las credenciales, se ejecuta:

```java
HttpSession session = request.getSession();
request.changeSessionId();
session.setAttribute("usuario", usuario);
```

`changeSessionId()` renueva el identificador, se suele hacer después del login por seguridad, ya que si alguien ha conseguido robar la cookie de sesión, tendría una manera para entrar sin necesidad de tener su password.

### Comprobar el acceso y cerrar sesión

Tener una sesión no significa haber iniciado sesión: también debe existir el atributo `usuario`. `InicioServlet` y `ListadoUsuariosServlet` comprueban ambas cosas antes de mostrar los datos:

```java
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("usuario") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
}
```

Se usa `getSession(false)` porque solo queremos comprobar una sesión existente. El `return` termina el método para no continuar mostrando la página privada después de ordenar la redirección.

El botón **Cerrar sesión** envía un `POST` a `/logout`. `LogoutServlet` consulta la sesión sin crear otra, la invalida si existe y redirige al login. Las siguientes peticiones a las rutas privadas volverán a exigir el acceso.

### Atributos de petición (request) y atributos de sesión

Elegimos dónde guardar un dato según cuánto tiempo lo necesitamos:

| | Petición (`request`) | Sesión (`session`) |
|---|---|---|
| Duración | Hasta que termina esa petición, incluido su `forward`. | Entre varias peticiones, hasta que la sesión se invalida o caduca. |
| Ejemplo | La lista `usuarios` que se muestra en `/usuarios` o ListadoUsuariosServlet. | El `usuario` que ha iniciado sesión. |
| Guardar desde Java | `request.setAttribute("usuarios", lista)` | `session.setAttribute("usuario", usuario)` |
| Consultar desde una JSP | `${requestScope.usuarios}` | `${sessionScope.usuario}` |

En las JSP, `${sessionScope.usuario.nombre}` consulta el nombre del usuario guardado en sesión. Se muestra con `<c:out>` para escapar los caracteres HTML.

### Mostrar una vista o cambiar de ruta

**`forward`** permite que el servlet muestre una JSP dentro de la misma petición. Conserva los atributos de `request` y la URL del navegador. Por ejemplo, `ListadoUsuariosServlet` guarda la lista en la petición y hace `forward` a `usuarios.jsp`.

**`sendRedirect`** pide al navegador que haga una nueva petición a otra ruta. No conserva los atributos de la petición anterior. La sesión sí puede seguir disponible porque el navegador vuelve a enviar su cookie. Por eso el login guarda `usuario` en sesión antes de redirigir a `/inicio`.

Como regla general, cuando hacemos:

- **GET:** el servlet obtiene los datos y hace `forward` a una JSP para mostrarlos.
- **POST correcto:** después de realizar la operación, el servlet hace `redirect` a una ruta GET. Así, actualizar la página no vuelve a enviar el formulario ni repite la operación. Este patrón se llama **POST/Redirect/GET**.
- **POST con errores:** el servlet puede hacer `forward` al formulario para mostrar el mensaje y conservar los datos introducidos como atributos de petición. Las contraseñas no se vuelven a rellenar.

En este ejemplo, un login correcto redirige a `/inicio`; uno incorrecto hace `forward` a `login.jsp` con el mensaje de error.

### Dónde están las JSP y cuándo caduca la sesión

Todas las JSP están en `src/main/webapp/WEB-INF/vistas`. Tomcat impide que el navegador las abra directamente ya que están en la carpeta de WEB_INF.

En este ejemplo, `WEB-INF/web.xml` configura dos aspectos de la sesión:

- **15 minutos de inactividad:** si no llegan peticiones asociadas a la sesión durante ese tiempo, caduca y se pierden sus atributos. No son 15 minutos desde el login si se sigue utilizando la aplicación.
- **Seguimiento por cookie:** el identificador de sesión se transporta mediante la cookie.

Las JSP declaran `session="false"` para no crear una sesión automáticamente, de esta manera dejamos delegado al Servlet el que la gestione. Pueden seguir leyendo una sesión existente mediante `sessionScope`.

## Estructura relevante

```text
06_Sesion/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/Usuario.java
    │   ├── servicios/GestorUsuarios.java
    │   └── servlets/{Login,Inicio,ListadoUsuarios,Logout}Servlet.java
    └── webapp/
        ├── index.html
        └── WEB-INF/
            ├── web.xml
            └── vistas/{login,inicio,usuarios}.jsp
```

## Cómo comprobar que funciona

1. Abre `/sesion/inicio` sin entrar: te redirige a `/sesion/login`.
2. Prueba `felix` / `1234` o `marta` / `4321`: llegas a `/sesion/inicio` y ves el nombre de la sesión.
3. Abre `/sesion/usuarios`: aparecen los dos nombres y sus ID, sin contraseñas.
4. Envía una contraseña incorrecta: se muestra un error y no se crea una sesión autenticada.
5. Cierra sesión y vuelve a pedir `/sesion/usuarios`: se redirige al acceso.
6. En las herramientas de red del navegador, observa la cookie de sesión en las peticiones posteriores al acceso. Esperar 15 minutos sin actividad también invalida la sesión.

## Explicación guiada

`LoginServlet` recibe el formulario por `POST`, valida las credenciales de demostración y crea la sesión solo si son correctas. `InicioServlet` y `ListadoUsuariosServlet` comprueban el atributo antes de mostrar sus JSP. `ListadoUsuariosServlet` añade la lista como atributo de petición, mientras el nombre del usuario se obtiene desde `sessionScope`. `LogoutServlet` atiende un `POST`, invalida la sesión y vuelve al formulario de acceso.

Las credenciales fijas permiten centrarse en la sesión. Son solo datos de práctica: una aplicación real almacenaría hashes de contraseñas, usaría HTTPS y añadiría protección CSRF a formularios que modifican estado. El listado nunca muestra contraseñas. Las vistas escapan los valores mediante `<c:out>`.

## Demostración local de secuestro de sesión

El **secuestro de sesión** (*session hijacking*) consiste en utilizar el identificador de una sesión ajena para acceder como su usuario. Vamos a simularlo únicamente con esta aplicación en local, dos navegadores y las cuentas de prueba.

### Compartir el identificador entre dos navegadores

1. En el primer navegador, abre `http://localhost:8080/sesion/login` e inicia sesión con `felix` / `1234`.
2. Abre las herramientas de desarrollo y busca las cookies de `http://localhost:8080`, en **Aplicación → Cookies** o **Almacenamiento → Cookies**, según el navegador. Localiza `JSESSIONID` y copia su valor **después del login**, porque `changeSessionId()` lo renueva al autenticarse. Anota también su dominio y ruta.
3. En otro navegador, abre `http://localhost:8080/sesion/login` e inicia sesión con `marta` / `4321`. Comprueba que en `/sesion/inicio` aparece Marta. Ahora cada navegador utiliza una sesión distinta.
4. En las herramientas de desarrollo del segundo navegador, busca su cookie `JSESSIONID`. Edita únicamente su **valor** y pega el que has copiado de la sesión de Félix. Conserva el nombre, el dominio y la ruta de la cookie. Mantén `localhost` en ambos navegadores: no cambies a `127.0.0.1`.
5. Vuelve a cargar `http://localhost:8080/sesion/inicio` en el segundo navegador. Donde antes aparecía Marta, ahora aparecerá Félix, sin haber introducido su contraseña en ese navegador. El cambio se produce al enviar la siguiente petición con la cookie sustituida.
6. Cierra sesión desde el primer navegador. Después, vuelve a solicitar `/sesion/inicio` desde el segundo: te redirigirá al login. No basta con mirar la página que ya estaba abierta, porque su contenido puede seguir visible.

### Qué demuestra

El servidor reconoce el **identificador de sesión**, no a la persona ni al navegador. Tras sustituir la cookie, ambos navegadores envían el mismo `JSESSIONID`, por lo que Tomcat localiza la sesión de Félix y su atributo `usuario`. No hemos convertido a Marta en Félix ni modificado su usuario: el segundo navegador ha dejado de utilizar la sesión de Marta y ahora accede a la de Félix. La sesión de Marta permanece en el servidor hasta que caduque o se invalide.

Al cerrar sesión desde cualquiera de los dos navegadores, `invalidate()` elimina esa sesión en el servidor. El identificador anterior deja de dar acceso, aunque el otro navegador todavía conserve la cookie. La caducidad por inactividad también termina la sesión.

### Qué protecciones ayudan y qué límites tienen

- **`HttpOnly`:** impide que JavaScript lea la cookie mediante `document.cookie`. No impide copiarla manualmente desde las herramientas de desarrollo ni reutilizar un identificador que ya se conoce.
- **HTTPS y la marca `Secure`:** HTTPS cifra la comunicación y `Secure` indica al navegador que envíe la cookie solo por HTTPS. Ayudan a proteger su transporte; esta demostración local utiliza HTTP.
- **`changeSessionId()`:** cambia el identificador al iniciar sesión para evitar que se siga usando uno conocido antes de autenticarse. No impide reutilizar el nuevo identificador si alguien lo obtiene después del login, como acabamos de simular.
- **Cerrar sesión y limitar la inactividad:** reducen el tiempo durante el que una sesión puede utilizarse.

Una cookie de sesión activa debe tratarse como una credencial: no debe compartirse ni publicarse en capturas o registros.

## Errores frecuentes

- Confundir la cookie del navegador con el objeto `HttpSession` que está en el servidor.
- Usar `getSession()` al comprobar un acceso y crear sesiones por accidente.
- Suponer que un atributo de petición sobrevive a una redirección.
- Proteger solo la JSP y dejar accesible el servlet que entrega los datos.
- Confiar en el botón «Atrás» para comprobar el cierre: el navegador puede mostrar una página almacenada; vuelve a solicitar la URL protegida.

## Propuestas para practicar

1. Añade un contador de visitas a `/inicio` guardado en la sesión.
2. Muestra la hora del último acceso con un atributo de sesión.
3. Compara una ventana normal con una privada para observar sesiones distintas.
