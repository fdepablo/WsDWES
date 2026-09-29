# Introducción a JSP

Este módulo introduce JSP como tecnología de vistas y compara dos recorridos: un servlet hace `forward` a una JSP protegida y otro redirige el navegador a una JSP pública. La comparación permite observar qué ocurre con la URL y los atributos de la petición.

## Qué vas a aprender

- Comprender qué papel tiene una JSP dentro de una aplicación web.
- Separar la preparación de datos de su presentación.
- Crear atributos con `HttpServletRequest#setAttribute`.
- Diferenciar los parámetros enviados por el cliente de los atributos creados en el servidor.
- Transferir una petición con `RequestDispatcher#forward`.
- Comparar `forward` con una redirección temporal `307` mediante dos servlets.
- Mostrar atributos mediante Expression Language (`${...}`).
- Acceder desde EL a las propiedades de un objeto Java.
- Proteger las JSP colocándolas dentro de `WEB-INF`.
- Comprobar por qué un atributo de petición no llega al destino de una redirección.

## Requisitos específicos

Los requisitos comunes están en el [README raíz](../README.md).

- Conocer los fundamentos de servlets explicados en los módulos anteriores.

## Conceptos clave

### Una JSP es una vista

Tomcat transforma internamente una JSP en un servlet. Sin embargo, no utilizaremos la JSP como lugar para escribir lógica Java. El servlet se encarga de preparar los datos y la JSP se limita a generar la presentación.

### Separación de responsabilidades con MVC

La combinación de servlets y JSP permite aplicar una versión sencilla del patrón **Modelo-Vista-Controlador (MVC)**:

- **Modelo:** representa los datos y las reglas de la aplicación. En este ejemplo, `Curso` forma parte del modelo.
- **Vista:** presenta la información al usuario. Esta responsabilidad corresponde a `inicio.jsp` y `resultado-redirect.jsp`.
- **Controlador:** recibe la petición, prepara los datos y elige qué vista debe mostrarlos. Esta responsabilidad corresponde a `InicioServletForward` y `InicioServletRedirect`.

El flujo del ejemplo es el siguiente:

```text
Navegador ── GET / ──► index.html
    │
    │ pulsa el enlace: GET /inicio-forward
    ▼
InicioServletForward ─ crea o consulta ────────► Modelo
    │                                             Curso
    │ añade los datos como atributos
    │ y ejecuta forward
    ▼
inicio.jsp
    │
    │ genera HTML
    ▼
Navegador
```

Esta separación evita que el servlet construya etiquetas HTML y que la JSP contenga lógica de negocio o código Java. En aplicaciones mayores, el modelo suele incluir también servicios y clases de acceso a datos. El segundo recorrido permite comparar este `forward` con una redirección.

### Atributos de la petición

El servlet crea datos con llamadas como:

```java
request.setAttribute("titulo", "Introducción a JSP");
```

La JSP accede al mismo dato mediante:

```jsp
${titulo}
```

Un atributo lo crea el código del servidor. Un parámetro procede normalmente de la URL o de un formulario y se lee con `getParameter`.

### Parámetros y atributos de la petición

Los parámetros y los atributos pueden estar dentro del mismo objeto `HttpServletRequest`, pero representan información diferente y se manejan con métodos distintos.

| Parámetro | Atributo |
|---|---|
| Lo envía normalmente el cliente. | Lo crea normalmente el código del servidor. |
| Puede proceder de la URL o de un formulario. | Se utiliza para comunicar servlets, filtros y vistas durante una petición. |
| Se lee con `getParameter`. | Se lee con `getAttribute`. |
| Siempre se obtiene como `String`, o como varios `String` mediante `getParameterValues`. | Puede contener cualquier objeto Java. |
| No se crea mediante `setAttribute`. | Se crea o sustituye mediante `setAttribute`. |

Por ejemplo, una petición podría incluir este parámetro:

```text
/inicio-forward?nombre=Ana
```

El servlet lo leería así:

```java
String nombre = request.getParameter("nombre");
```

En cambio, el propio servlet puede crear atributos antes de enviar la petición a la JSP:

```java
Curso curso = new Curso("Desarrollo Web en Entorno Servidor", "DAW");

request.setAttribute("curso", curso);
request.setAttribute("mensaje", "Datos preparados correctamente");
```

Desde Java, `getAttribute` devuelve `Object`. Por eso, cuando se recupera un atributo en otra clase Java es necesario convertirlo a su tipo concreto:

```java
Curso curso = (Curso) request.getAttribute("curso");
```

Si no existe un parámetro o atributo con el nombre indicado, tanto `getParameter` como `getAttribute` devuelven `null`.

### Acceso a los atributos desde JSP

Expression Language permite consultar los atributos sin realizar conversiones de tipo dentro de la JSP:

```jsp
${curso.nombre}
```

EL busca el nombre `curso` en varios ámbitos. Para indicar expresamente que debe buscarse en la petición se utiliza `requestScope`:

```jsp
${requestScope.curso.nombre}
```

Las dos expresiones producen el mismo resultado en este ejemplo. La segunda deja más claro que `curso` fue añadido mediante `request.setAttribute`.

La propiedad `nombre` se obtiene llamando de forma indirecta a `getNombre()`. Por eso los objetos mostrados mediante EL deben exponer getters públicos siguiendo las convenciones habituales de JavaBeans.

### Duración de un atributo de petición

Un atributo guardado en `request` existe solamente mientras se procesa esa petición. El `forward` conserva el mismo objeto `HttpServletRequest`, de modo que la JSP puede consultar los atributos creados por el servlet:

```text
InicioServletForward
    │ request.setAttribute("curso", curso)
    │
    └── forward ──► inicio.jsp
                       └── ${requestScope.curso.nombre}
```

Cuando termina la respuesta, esos atributos dejan de estar disponibles. Una redirección solicita al navegador que cree otra petición, por lo que tampoco conserva los atributos de la petición anterior. Para mantener información durante más tiempo se necesitan otros mecanismos, como la sesión o la persistencia, que se estudiarán posteriormente.

### Propiedades Java desde EL

Cuando la JSP evalúa `${curso.nombre}`, busca la propiedad `nombre` y llama al método `getNombre()` del objeto. Esto permite presentar objetos sin escribir llamadas Java dentro de la página.

Las JSP de este módulo también usan otras expresiones de EL:

- `${pageContext.request.contextPath}` obtiene el contexto en el que se ha desplegado la aplicación. Se antepone a los enlaces para que funcionen tanto en `/` como en otro contexto.
- `empty requestScope.mensaje` comprueba si el atributo `mensaje` no existe o está vacío. En `resultado-redirect.jsp` se cumple porque la redirección crea otra petición.
- `${condición ? valorSiSeCumple : valorSiNoSeCumple}` elige uno de dos textos. La JSP de la redirección lo usa para mostrar «Ninguno: esta es otra petición.» cuando no encuentra el atributo.

### `forward` mantiene la misma petición

`forward` transfiere el procesamiento internamente a la JSP. El navegador no realiza una segunda petición y los atributos continúan disponibles. La dirección del navegador sigue mostrando `/inicio-forward` si el contexto es `/`.

La transferencia se realiza mediante un `RequestDispatcher`:

```java
request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp")
        .forward(request, response);
```

El servlet puede elegir distintas vistas según el resultado del procesamiento. Por ejemplo, podría enviar una operación correcta a una JSP de confirmación y una operación incorrecta a una JSP de error. El módulo `05_Formulario` aplica esta idea.

El segundo servlet responde con `307 Temporary Redirect` y la cabecera `Location`, igual que en `03_HTTP`. El navegador realiza otra petición al destino y conserva el método HTTP original; en este ejemplo, sigue siendo `GET`. La comparación es:

| `forward` | Redirección `307` |
|---|---|
| La transferencia ocurre dentro del servidor. | El servidor pide al navegador que realice otra petición. |
| Se conserva el mismo objeto `request`. | Se crea una petición nueva. |
| Se conservan los atributos de la petición. | Los atributos de la petición original se pierden. |
| La URL del navegador no cambia. | La URL del navegador cambia. |
| No hay respuesta de redirección intermedia. | El navegador recibe primero `307` y después solicita la vista. |
| Se usa normalmente para llegar a una vista interna. | Se usa para indicar al cliente que visite otra URL. |

### Vistas bajo `WEB-INF`

El navegador puede abrir directamente el `index.html` situado en `webapp`. En cambio, no puede solicitar la JSP situada en `WEB-INF`: para mostrar esa vista debe pasar por el servlet controlador.

`InicioServletRedirect` redirige a `resultado-redirect.jsp`, situada fuera de `WEB-INF` para que el navegador pueda solicitarla. El servlet añade antes un atributo llamado `mensaje`, pero la JSP recibe otra petición y muestra que ese atributo no está disponible. Esta JSP pública sirve solo para hacer visible la diferencia; una vista interna que necesite atributos de la petición se presenta normalmente mediante `forward`.

## Estructura relevante

```text
04_JSP/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/Curso.java
    │   ├── servlets/InicioServletForward.java
    │   ├── servlets/InicioServletRedirect.java
    │   └── utils/NumeroAleatorio.java
    └── webapp/
        ├── index.html
        ├── resultado-redirect.jsp
        └── WEB-INF/vistas/inicio.jsp
```

## Recorrido del ejemplo

1. El navegador abre `index.html`, una página pública con dos enlaces.
2. Con **forward**, solicita `GET /inicio-forward`. `InicioServletForward` crea los datos y los guarda como atributos de la petición.
3. El servlet hace `forward` a `/WEB-INF/vistas/inicio.jsp`. La JSP usa EL para mostrar los datos y la URL no cambia.
4. Con **redirect**, solicita `GET /inicio-redirect`. `InicioServletRedirect` guarda un atributo y responde con `307` y `Location: /resultado-redirect.jsp` (precedido del contexto de la aplicación si lo hay).
5. El navegador solicita esa JSP en una segunda petición. La URL cambia y el atributo creado por el servlet ya no está disponible.

## Cómo compilar

Desde la raíz del workspace:

```bash
mvn -pl 04_JSP -am clean package
```

El resultado será `04_JSP/target/04_JSP-1.0-SNAPSHOT.war`.

## Cómo ejecutar en IntelliJ con Smart Tomcat

1. Recarga los proyectos desde la ventana **Maven**.
2. Crea o duplica una configuración de tipo **Smart Tomcat**.
3. Selecciona Tomcat 11.
4. Usa `04_JSP/src/main/webapp` como **Deployment Directory**.
5. Selecciona `04_JSP` en **Use classpath of module**.
6. Utiliza `/` como **Context Path**.
7. Abre `http://localhost:8080/` y prueba los dos enlaces.

> [!WARNING]
> **Deployment Directory** y **Use classpath of module** deben apuntar siempre a `04_JSP`.

Si usas **Tomcat Server** con el WAR desplegado en **Application context** `/04_JSP_war`, abre `http://localhost:8080/04_JSP_war/`. Los enlaces relativos del índice y las URL construidas con `request.getContextPath()` funcionan con ambos contextos.

## Cómo comprobar que funciona

- `http://localhost:8080/` muestra los enlaces **forward** y **redirect** con Smart Tomcat; con Tomcat Server usa `http://localhost:8080/04_JSP_war/`.
- `/inicio-forward` muestra la fecha del servidor y un número entre 1 y 100. Con Tomcat Server, la ruta es `/04_JSP_war/inicio-forward`.
- El módulo y el ciclo proceden del objeto `Curso`.
- Al recargar cambian la fecha o el número.
- La URL continúa siendo `/inicio-forward` después del `forward` (o `/04_JSP_war/inicio-forward` con Tomcat Server).
- El enlace **redirect** termina en `/resultado-redirect.jsp` (o `/04_JSP_war/resultado-redirect.jsp`) y muestra «Ninguno: esta es otra petición.»; en la pestaña **Red** del navegador pueden observarse las dos peticiones, el `307` intermedio y su cabecera `Location`.
- El enlace para abrir la JSP protegida directamente devuelve `404`: la URL es `/WEB-INF/vistas/inicio.jsp` con Smart Tomcat y `/04_JSP_war/WEB-INF/vistas/inicio.jsp` con Tomcat Server.

## Explicación guiada

`index.html` es una página estática de entrada con enlaces relativos a ambos servlets dentro del contexto configurado. `InicioServletForward` conserva el comportamiento anterior: prepara los atributos sin construir HTML y cede la presentación a la JSP protegida.

La JSP contiene HTML normal y pequeñas expresiones `${...}`. Los valores de este primer ejemplo los crea totalmente el servidor. Cuando los datos procedan del usuario será necesario escaparlos antes de mostrarlos; el módulo `05_Formulario` introduce JSTL para hacerlo.

El controlador no envía los datos llamando directamente a la JSP. Primero los guarda en el objeto `request` mediante `setAttribute`. Como el `forward` mantiene esa misma petición, la JSP puede recuperar los atributos con Expression Language.

`InicioServletRedirect` también llama a `request.setAttribute`, pero después establece el estado `307` y la cabecera `Location`. La respuesta de redirección indica al navegador una URL nueva. Tomcat crea otra petición para `resultado-redirect.jsp`, de modo que `${requestScope.mensaje}` no encuentra el atributo anterior. Como el navegador debe solicitar esa URL, la JSP de destino se coloca fuera de `WEB-INF`.

## Errores frecuentes

- Intentar abrir directamente la JSP situada en `WEB-INF`.
- Escribir la ruta del `forward` sin la barra inicial.
- Esperar que una redirección conserve los atributos de la petición anterior.
- Confundir `${curso.nombre}` con el acceso directo a un campo: EL utiliza el getter.
- Introducir lógica Java mediante scriptlets en la vista.
- Suponer que Expression Language escapa automáticamente el HTML.

## Propuestas para practicar

1. Añade al objeto `Curso` el número de horas y muéstralo con EL.
2. Crea otro atributo con el nombre del servidor.
3. Cambia el rango del número aleatorio y comprueba los límites.
4. Abre la JSP pública directamente y compara el resultado con el recorrido que pasa por `InicioServletRedirect`.
