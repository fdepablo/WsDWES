# Sesiones HTTP

En este ejemplo vamos a trabajar con la sesión web. Los atributos de petición duran mientras se atiende una petición HTTP. En la sesión podemos guardar atributos que se mantengan disponibles entre varias peticiones.

## Qué vas a aprender

- Diferenciar atributos de petición y atributos de sesión.
- Distinguir una sesión existente de un usuario autenticado y consultarla sin crear otra.
- Observar cómo cambia el identificador al iniciar sesión con `changeSessionId()`.
- Observar cómo la cookie de sesión enlaza varias peticiones HTTP.
- Proteger rutas y cerrar sesión con `invalidate()`.
- Configurar el tiempo de inactividad y el seguimiento por cookie.
- Mostrar datos con EL y JSTL.

## Conceptos clave

### Para qué sirve una sesión

Cuando utilizamos una aplicación web, el navegador envía una nueva petición cada vez que abre una página o envía un formulario. HTTP trata cada petición de forma independiente. Por sí solo, no relaciona una petición con las anteriores para recordar lo que ha hecho la persona que está utilizando la aplicación.

Hasta ahora hemos usado atributos de petición para entregar datos de un servlet a una JSP. Esos atributos están disponibles mientras se atiende esa petición. Cuando termina y el navegador solicita otra página, comienza una petición nueva y los atributos anteriores ya no están disponibles.

Una **sesión** permite conservar información entre varias peticiones a la misma aplicación. En Java, el servidor la representa mediante un objeto `HttpSession`. Podemos guardar datos dentro de ese objeto y recuperarlos más adelante, aunque el navegador haya cambiado de página. Cada sesión tiene sus propios datos, separados de los de otras sesiones.

Los datos se guardan como **atributos de sesión**, con un nombre que permite recuperarlos. Por ejemplo, después de comprobar un nombre y una contraseña, podemos guardar el usuario que ha accedido. Cuando esa persona abra otra página de la aplicación, el servidor podrá consultar ese atributo sin pedirle otra vez sus credenciales. Este es el uso que vamos a practicar en el módulo.

La sesión conserva esos atributos hasta que se cierra mediante código o caduca por inactividad. Por tanto, sirve para mantener información durante el uso de la aplicación. Si necesitamos conservarla de forma permanente, tendremos que guardarla en otro lugar, como una base de datos.

### Cómo reconoce el servidor la sesión

Los datos de la sesión se guardan en el servidor. El navegador conserva una cookie, normalmente llamada `JSESSIONID`, que contiene el identificador de esa sesión.

1. El servidor crea la sesión y envía su identificador al navegador mediante una cookie.
2. El navegador incluye la cookie en las siguientes peticiones a la aplicación.
3. Tomcat utiliza ese identificador para localizar la sesión y sus atributos.

La cookie contiene el identificador; el objeto `Usuario` permanece en el servidor. Por eso una ventana privada puede tener una sesión distinta de la ventana habitual.

### Crear, consultar y guardar datos en la sesión

Cada atributo de sesión se guarda con un nombre. Usamos ese mismo nombre para recuperarlo después. Por ejemplo, podemos guardar el objeto de un usuario con el nombre `usuario` y consultarlo desde otra petición.

El objeto `request`, de tipo `HttpServletRequest`, nos permite obtener la sesión. A partir del objeto `HttpSession` podemos guardar y consultar sus atributos mediante estos métodos.

| Método | Para qué se utiliza |
|---|---|
| `request.getSession()` | Recupera la sesión actual. Si no existe, crea una nueva. Devuelve un objeto de tipo `HttpSession`. |
| `request.getSession(false)` | Recupera la sesión actual sin crear otra. Si no existe, devuelve `null`. |
| `session.setAttribute("usuario", usuario)` | Guarda el objeto en la sesión con el nombre `usuario`. |
| `session.getAttribute("usuario")` | Recupera el atributo. Si no está guardado, devuelve `null`. |
| `session.invalidate()` | Invalida la sesión en el servidor y elimina sus atributos. La cookie puede permanecer en el navegador, pero su identificador ya no permite recuperar esa sesión. |

En `LoginServlet`, después de comprobar las credenciales, se ejecuta:

```java
HttpSession session = request.getSession();
request.changeSessionId();
session.setAttribute("usuario", usuario);
```

`changeSessionId()` cambia el identificador de la sesión y conserva sus atributos. Se utiliza después de comprobar las credenciales para que un identificador conocido antes del acceso deje de servir para utilizar la sesión autenticada.

Esta medida no impide que alguien reutilice el nuevo identificador si lo obtiene después de iniciar sesión. La demostración al final del documento permite observar ese límite.

### Observar el cambio del identificador

Abre `http://localhost:8080/sesion/` en una ventana privada nueva. La página de entrada es `index.jsp` y declara `session="true"`. Esto hace que Tomcat cree una sesión si todavía no existe. La página muestra su identificador mediante `${pageContext.session.id}`. Aunque la sesión ya existe, todavía no contiene el atributo `usuario` que permite acceder al área privada.

Anota ese identificador y pulsa **Entrar en la aplicación**. Después de iniciar sesión correctamente, la página de inicio muestra el identificador actual. Será distinto porque `LoginServlet` ha llamado a `changeSessionId()` antes de guardar el usuario y redirigir al área privada.

La sesión sigue siendo la misma y conserva sus atributos. Lo que cambia es el identificador que se envía en la cookie. Mostrarlo en las páginas es una ayuda para esta práctica local y debe retirarse en una aplicación real.

### Comprobar el acceso y cerrar sesión

Tener una sesión no significa haber iniciado sesión: también debe existir el atributo `usuario`. `PaginaPrincipalServlet` y `UsuariosServlet` comprueban ambas cosas antes de mostrar los datos:

```java
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("usuario") == null) {
    response.sendRedirect(request.getContextPath() + "/LoginServlet");
    return;
}
```

Se usa `getSession(false)` porque solo queremos comprobar una sesión existente. El `return` termina el método para no continuar mostrando la página privada después de ordenar la redirección.

El botón **Cerrar sesión** envía un `POST` a `/LogoutServlet`. `LogoutServlet` consulta la sesión sin crear otra, la invalida si existe y redirige al login. Las siguientes peticiones a las rutas privadas volverán a exigir el acceso.

### Atributos de petición (request) y atributos de sesión

Elegimos dónde guardar un dato según cuánto tiempo lo necesitamos:

| | Petición (`request`) | Sesión (`session`) |
|---|---|---|
| Duración | Hasta que termina esa petición, incluido su `forward`. | Entre varias peticiones, hasta que la sesión se invalida o caduca. |
| Ejemplo | La lista `usuarios` que se muestra en `/UsuariosServlet` o UsuariosServlet. | El `usuario` que ha iniciado sesión. |
| Guardar desde Java | `request.setAttribute("usuarios", lista)` | `session.setAttribute("usuario", usuario)` |
| Consultar desde una JSP | `${requestScope.usuarios}` | `${sessionScope.usuario}` |

En las JSP, `${sessionScope.usuario.nombre}` consulta el nombre del usuario guardado en sesión. Se muestra con `<c:out>` para escapar los caracteres HTML.

### Mostrar una vista o cambiar de ruta

**`forward`** permite que el servlet muestre una JSP dentro de la misma petición. Conserva los atributos de `request` y la URL del navegador. Por ejemplo, `UsuariosServlet` guarda la lista en la petición y hace `forward` a `usuarios.jsp`.

**`sendRedirect`** pide al navegador que haga una nueva petición a otra ruta. No conserva los atributos de la petición anterior. La sesión sí puede seguir disponible porque el navegador vuelve a enviar su cookie. Por eso el login guarda `usuario` en sesión antes de redirigir a `/PaginaPrincipalServlet`.

Como regla general, cuando hacemos:

- **GET:** el servlet obtiene los datos y hace `forward` a una JSP para mostrarlos.
- **POST correcto:** después de realizar la operación, el servlet hace `redirect` a una ruta GET. Así, actualizar la página no vuelve a enviar el formulario ni repite la operación. Este patrón se llama **POST/Redirect/GET**.
- **POST con errores:** el servlet puede hacer `forward` al formulario para mostrar el mensaje y conservar los datos introducidos como atributos de petición. Las contraseñas no se vuelven a rellenar.

En este ejemplo, un login correcto redirige a `/PaginaPrincipalServlet`; uno incorrecto hace `forward` a `login.jsp` con el mensaje de error.

### Dónde están las JSP

Todas las JSP están en `src/main/webapp/WEB-INF/vistas`. Tomcat impide que el navegador las abra directamente porque están dentro de `WEB-INF`. Para mostrarlas, el servlet hace un `forward` a la vista correspondiente.

Las JSP participan en la sesión por defecto. En `index.jsp` lo indicamos expresamente con `session="true"` para observar su creación antes del login. Por eso puede existir una sesión aunque las credenciales todavía no se hayan comprobado. El acceso a las rutas privadas depende también de que tenga el atributo `usuario`.

### Cuándo caduca la sesión

En este ejemplo, `WEB-INF/web.xml` configura dos aspectos de la sesión:

- **5 minutos de inactividad:** si no llegan peticiones asociadas a la sesión durante ese tiempo, caduca y se pierden sus atributos. El tiempo se cuenta desde la última petición asociada a la sesión, no desde el login si se sigue utilizando la aplicación.
- **Seguimiento por cookie:** el identificador de sesión se transporta mediante la cookie.

## Estructura relevante

```text
06_Sesion/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/Usuario.java
    │   ├── servicios/GestorUsuarios.java
    │   └── servlets/{Login,PaginaPrincipal,Usuarios,Logout}Servlet.java
    └── webapp/
        ├── index.jsp
        └── WEB-INF/
            ├── web.xml
            └── vistas/{login,paginaPrincipal,usuarios}.jsp
```

## Cómo ejecutar

Necesitas Java 25, Maven y Tomcat 11. Consulta los [requisitos y las instrucciones comunes del workspace](../README.md) para preparar el entorno.

Cada ruta tiene el mismo nombre que el servlet que la atiende. Las mayúsculas y minúsculas deben coincidir.

| Servlet | Ruta dentro de la aplicación | Para qué sirve |
|---|---|---|
| `LoginServlet` | `/LoginServlet` | Muestra el formulario con GET y comprueba las credenciales con POST. |
| `PaginaPrincipalServlet` | `/PaginaPrincipalServlet` | Muestra la página principal al usuario autenticado. |
| `UsuariosServlet` | `/UsuariosServlet` | Muestra el listado al usuario autenticado. |
| `LogoutServlet` | `/LogoutServlet` | Cierra la sesión mediante POST. |

1. Desde la raíz del workspace, compila el módulo con `mvn -pl 06_Sesion -am package`.
2. Despliega `06_Sesion/target/06_Sesion-1.0-SNAPSHOT.war` en Tomcat. En IntelliJ también puedes utilizar Smart Tomcat según las instrucciones comunes.
3. Configura el contexto de la aplicación como `/sesion` y abre `http://localhost:8080/sesion/`.

Las direcciones de este README utilizan `/sesion` como contexto. Si despliegas el WAR con otro nombre o eliges otro contexto, sustituye ese prefijo en las URL.

## Cómo comprobar que funciona

1. En una ventana privada nueva, abre `/sesion/` y anota el identificador. Si intentas abrir `/sesion/PaginaPrincipalServlet` sin autenticarte, te redirige a `/sesion/LoginServlet`, aunque ya exista una sesión.
2. Prueba `felix` / `1234` o `marta` / `4321`: llegas a `/sesion/PaginaPrincipalServlet` y ves el nombre del usuario guardado en la sesión. El identificador mostrado será distinto del que anotaste antes del login.
3. Abre `/sesion/UsuariosServlet`: aparecen los dos nombres y sus ID, sin contraseñas.
4. Envía una contraseña incorrecta: se muestra un error y no se crea una sesión autenticada.
5. Cierra sesión y vuelve a pedir `/sesion/UsuariosServlet`: se redirige al acceso.
6. En las herramientas de red del navegador, observa la cookie de sesión en las peticiones posteriores al acceso. Después, espera más de 5 minutos sin enviar peticiones a la aplicación y vuelve a abrir `/sesion/PaginaPrincipalServlet`. La sesión habrá caducado y se te redirigirá al login.

## Explicación guiada

`index.jsp` crea una sesión si no existe y muestra su identificador. `LoginServlet` recibe el formulario por `POST` y valida las credenciales de demostración. Si son correctas, recupera la sesión o crea una si no existe, renueva su identificador y guarda el usuario. `PaginaPrincipalServlet` y `UsuariosServlet` comprueban el atributo antes de mostrar sus JSP. `UsuariosServlet` añade la lista como atributo de petición, mientras el nombre del usuario se obtiene desde `sessionScope`. `LogoutServlet` atiende un `POST`, invalida la sesión y vuelve al formulario de acceso.

Las credenciales fijas permiten centrarse en la sesión. Son solo datos de práctica: una aplicación real almacenaría hashes de contraseñas, usaría HTTPS y añadiría protección CSRF a formularios que modifican estado. El listado nunca muestra contraseñas. Las vistas escapan los valores mediante `<c:out>`.

## Demostración local de secuestro de sesión

El **secuestro de sesión** (*session hijacking*) consiste en utilizar el identificador de una sesión ajena para acceder como su usuario. Vamos a simularlo únicamente con esta aplicación en local, dos navegadores y las cuentas de prueba.

### Compartir el identificador entre dos navegadores

1. En el primer navegador, abre `http://localhost:8080/sesion/LoginServlet` e inicia sesión con `felix` / `1234`.
2. Abre las herramientas de desarrollo y busca las cookies de `http://localhost:8080`, en **Aplicación → Cookies** o **Almacenamiento → Cookies**, según el navegador. Localiza `JSESSIONID` y copia su valor **después del login**, porque `changeSessionId()` lo renueva al autenticarse. Anota también su dominio y ruta.
3. En otro navegador, abre `http://localhost:8080/sesion/LoginServlet` e inicia sesión con `marta` / `4321`. Comprueba que en `/sesion/PaginaPrincipalServlet` aparece Marta. Ahora cada navegador utiliza una sesión distinta.
4. En las herramientas de desarrollo del segundo navegador, busca su cookie `JSESSIONID`. Edita únicamente su **valor** y pega el que has copiado de la sesión de Félix. Conserva el nombre, el dominio y la ruta de la cookie. Mantén `localhost` en ambos navegadores: no cambies a `127.0.0.1`.
5. Vuelve a cargar `http://localhost:8080/sesion/PaginaPrincipalServlet` en el segundo navegador. Donde antes aparecía Marta, ahora aparecerá Félix, sin haber introducido su contraseña en ese navegador. El cambio se produce al enviar la siguiente petición con la cookie sustituida.
6. Cierra sesión desde el primer navegador. Después, vuelve a solicitar `/sesion/PaginaPrincipalServlet` desde el segundo: te redirigirá al login. No basta con mirar la página que ya estaba abierta, porque su contenido puede seguir visible.

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

1. Añade un contador de visitas a `/PaginaPrincipalServlet` guardado en la sesión.
2. Muestra la hora del último acceso con un atributo de sesión.
3. Compara una ventana normal con una privada para observar sesiones distintas.
