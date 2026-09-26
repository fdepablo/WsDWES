# Sesiones HTTP

Este ejemplo adapta `_11_Session` del antiguo workspace para estudiar cómo conservar la identidad de un usuario entre peticiones con `HttpSession`. El acceso, el listado y el cierre de sesión pasan por servlets; las JSP presentan los datos.

## Qué vas a aprender

- Diferenciar atributos de petición y atributos de sesión.
- Crear una sesión después de autenticar y consultarla sin crear una nueva.
- Observar cómo la cookie de sesión enlaza varias peticiones HTTP.
- Proteger rutas y cerrar sesión con `invalidate()`.
- Configurar el tiempo de inactividad y el seguimiento por cookie.
- Mostrar datos con EL y JSTL.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Haber visto servlets, JSP y formularios de los módulos anteriores.

## Conceptos clave

HTTP trata cada petición por separado. La sesión permite asociar a varias peticiones un objeto `HttpSession` del servidor. El navegador conserva una cookie de sesión y la envía en peticiones posteriores. La cookie contiene un identificador, no el objeto `Usuario`.

`getSession()` crea una sesión si aún no existe; `getSession(false)` devuelve `null` en ese caso. Por eso las rutas privadas usan la segunda forma: visitar una página protegida no debe crear una sesión nueva. Tras un acceso correcto se renueva el identificador con `changeSessionId()` y se guarda solo el usuario público en el atributo `usuario`. Al salir, `invalidate()` elimina la sesión.

El atributo `usuario` de sesión está disponible en `/inicio` y `/usuarios`. En cambio, la lista `usuarios` se crea en la petición de `/usuarios` y solo llega a su JSP mediante `forward`. Después del `POST /login` correcto se usa una redirección: la nueva petición conserva la sesión, aunque ya no comparte los atributos de petición.

`WEB-INF` es una carpeta especial reconocida por Tomcat cuando está dentro de `src/main/webapp`. Basta con crearla en esa ubicación: no hay que activarla ni declararla en `web.xml`. El navegador no puede solicitar directamente las JSP guardadas en `WEB-INF/vistas`; los servlets llegan a ellas mediante `forward`.

En este ejemplo, `WEB-INF/web.xml` tiene otra función: configura la sesión con 15 minutos de inactividad y seguimiento por cookie. No es necesario para proteger las JSP. Además, las JSP usan `session="false"` para no crear una sesión implícitamente.

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

## Cómo compilar y ejecutar

Desde la raíz del workspace:

```bash
mvn -pl 06_Sesion -am clean package
```

El WAR se genera en `06_Sesion/target/06_Sesion-1.0-SNAPSHOT.war`. En IntelliJ, recarga Maven y configura Tomcat 11 con `06_Sesion/src/main/webapp` como **Deployment Directory**, `06_Sesion` en **Use classpath of module** y `/sesion` como **Context Path**. Abre `http://localhost:8080/sesion/`. Si despliegas el WAR con otro contexto, sustituye `/sesion` por el contexto elegido.

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
