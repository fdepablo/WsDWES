# Spring MVC con Thymeleaf

Este módulo adapta el catálogo de películas de `WorkspaceSpring/16_SpringBootMVC` y continúa lo aprendido en [`11_SpringBoot`](../11_SpringBoot/README.md) y [`12_SpringDataJPA`](../12_SpringDataJPA/README.md). Sigue la organización de [`08_MVC`](../08_MVC/README.md): `controlador`, `modelo.entidad`, `modelo.servicio` y `modelo.persistencia`. Ahora el usuario trabaja desde el navegador: Spring MVC recibe la petición, el controlador llama al servicio, Spring Data JPA guarda los datos en H2 y Thymeleaf genera el HTML. El foco aquí es el recorrido MVC; los detalles de JPA se explican en el módulo 12.

Todos esos paquetes están bajo `springmvc`, donde se encuentra `Aplicacion`: Boot busca automáticamente los componentes en ese paquete raíz y sus subpaquetes. `FormularioPelicula` vive con el controlador porque representa datos de entrada HTTP, mientras que la entidad `Pelicula` pertenece al modelo.

## Qué vas a aprender

- Seguir el recorrido petición HTTP → controlador → servicio → repositorio JPA → modelo de la respuesta → vista.
- Usar `@Controller`, `@GetMapping`, `@PostMapping`, `@PathVariable` y `@ModelAttribute`.
- Entregar datos a una plantilla con `Model` y devolver su nombre desde el controlador.
- Mostrar datos y construir enlaces y formularios con Thymeleaf.
- Validar datos en el servicio, mostrar un error y aplicar el patrón POST → redirección → GET tras un alta correcta.
- Reutilizar la persistencia con Spring Data JPA y H2 del módulo 12 en una aplicación web.

## Requisitos y arranque

Necesitas Java 25, Maven e IntelliJ IDEA; consulta el [README raíz](../README.md). Con `spring-boot-starter-webmvc`, Boot arranca Tomcat integrado. `spring-boot-starter-thymeleaf` añade el motor de plantillas: **Thymeleaf no viene incluido automáticamente** por usar Boot. `spring-boot-starter-data-jpa` y H2 aportan la persistencia sin instalar un servidor de base de datos. No hay que configurar un Tomcat externo.

Desde la raíz del workspace:

```bash
mvn -pl 13_SpringMVC -am clean package
java -jar 13_SpringMVC/target/13_SpringMVC-1.0-SNAPSHOT.jar
```

En IntelliJ Community puedes ejecutar `springmvc.Aplicacion.main` con el JDK 25. Abre `http://localhost:8080/peliculas`. Para usar otro puerto, añade `server.port=8081` a `src/main/resources/application.properties`, reinicia y cambia el puerto de la URL. No hace falta una configuración de Smart Tomcat.

## MVC en este proyecto

| Pieza | Archivo | Responsabilidad |
| --- | --- | --- |
| Entidad | `modelo.entidad.Pelicula` | Representar una fila de la tabla `peliculas`; H2 genera su ID. |
| Datos del formulario | `controlador.FormularioPelicula` | Recibir únicamente los campos editables enviados por el navegador. |
| Persistencia | `modelo.persistencia.PeliculaRepositorio` | Extender `JpaRepository<Pelicula, Long>` para guardar, buscar y borrar. |
| Servicio | `modelo.servicio.PeliculaServicio` | Aplicar las reglas de título y género obligatorios. |
| Controlador | `controlador.PeliculaControlador` | Asociar rutas y métodos HTTP, llamar al servicio y decidir la vista, redirección o `404`. |
| Vista | `listado.html` y `formulario.html` | Convertir los datos del modelo en HTML mediante Thymeleaf. |

Aquí «modelo» tiene dos sentidos relacionados: las clases de datos y negocio forman el modelo de la aplicación; el objeto `Model` de Spring MVC contiene los **atributos para una respuesta concreta**. `model.addAttribute("peliculas", ...)` permite que `listado.html` lea `${peliculas}`. No guarda los datos entre peticiones.

### Del navegador a la plantilla

Al pedir `GET /peliculas`, Spring MVC selecciona `PeliculaControlador.listar()` por su `@GetMapping`. El controlador pide la lista al servicio, la coloca en `Model` con el nombre `peliculas` y devuelve `"listado"`. Boot busca la plantilla `src/main/resources/templates/listado.html`; Thymeleaf sustituye sus expresiones por el contenido del modelo y el navegador recibe HTML. El controlador devuelve un **nombre de vista**, no el contenido HTML ni el nombre de un fichero JSP.

`@Controller` registra la clase como componente de presentación y permite que los `String` devueltos por sus métodos se interpreten como nombres de vista o instrucciones `redirect:`. `@RestController` se usaría para devolver datos directamente en el cuerpo de la respuesta, no para este recorrido de plantillas. La dependencia `PeliculaServicio` llega al controlador por su único constructor, igual que en el módulo anterior.

| Anotación | Uso en `PeliculaControlador` |
| --- | --- |
| `@GetMapping` | Asocia una petición GET con el listado o un formulario. |
| `@PostMapping` | Asocia un envío POST con crear, actualizar o borrar. |
| `@PathVariable` | Lee el `id` incluido en una ruta como `/peliculas/3/editar`. |
| `@ModelAttribute` | Construye `FormularioPelicula` con los campos enviados por el formulario. |

`@SpringBootApplication` y `@Service` cumplen las funciones explicadas en `11_SpringBoot`. No hace falta poner `@Repository` en la interfaz que extiende `JpaRepository`: Spring Data JPA crea su implementación. Se usa un objeto `FormularioPelicula` en el paquete del controlador para que el navegador solo entregue título y género: el identificador lo asigna H2, no un campo oculto manipulable por el usuario. Ese objeto de formulario tiene constructor sin argumentos y getters y setters para el enlace de campos; `Pelicula` es una clase `@Entity` con constructor sin argumentos para JPA, no un `record`.

### Persistencia reutilizada del módulo 12

El servicio inyecta `PeliculaRepositorio`. Para listar usa `findAllByOrderByTituloAsc`; para crear y actualizar usa `save`, para buscar usa `findById` y para borrar usa `deleteById` tras comprobar `existsById`. `findById` devuelve `Optional<Pelicula>`: si no hay película, el controlador responde `404`. El formulario sigue siendo independiente de la entidad para no enlazar directamente los datos HTTP con una fila de la base.

`application.properties` selecciona una base H2 en memoria y `ddl-auto=create-drop` crea la tabla al arrancar y la elimina al detener la aplicación. Los datos sobreviven a peticiones sucesivas, pero no a un reinicio. Este módulo no configura MariaDB: para estudiar el cambio de base de datos y el perfil correspondiente, consulta [`12_SpringDataJPA`](../12_SpringDataJPA/README.md).

### Thymeleaf y el formulario

Las plantillas son HTML en `src/main/resources/templates`, la ubicación predeterminada con el starter de Thymeleaf. El prefijo `th:` identifica atributos que Thymeleaf procesa **en el servidor** antes de enviar la página al navegador. El atributo `xmlns:th` declara ese prefijo en el documento; no es una URL a la que el navegador deba llamar. El texto de ejemplo dentro de algunas etiquetas, como `Ejemplo` en una celda, ayuda a leer el HTML sin procesar y se sustituye al renderizar la vista.

En estas plantillas aparecen tres tipos de expresiones:

| Expresión | Significado | Ejemplo del módulo |
| --- | --- | --- |
| `${...}` | Lee un atributo que el controlador entregó en `Model` o una propiedad de un objeto disponible. | `${peliculas}`, `${tituloPagina}`, `${pelicula.titulo}`. |
| `*{...}` | Lee una propiedad del objeto seleccionado por `th:object`. | `*{titulo}` se refiere a `formulario.titulo`. |
| `@{...}` | Construye una URL de la aplicación, respetando su ruta de contexto si cambia. | `@{/peliculas}` o `@{/peliculas/{id}/editar(id=${pelicula.id})}`. |

En `listado.html`, `th:if="${#lists.isEmpty(peliculas)}"` muestra el aviso solo cuando no hay películas; `th:unless` hace lo contrario y muestra la tabla cuando sí las hay. `#lists.isEmpty(...)` es una utilidad de expresiones de Thymeleaf para consultar una lista. `th:each="pelicula : ${peliculas}"` repite una fila por cada elemento y llama `pelicula` al elemento actual dentro de esa fila. `th:text="${pelicula.titulo}"` coloca el título como **texto escapado**: caracteres como `<` no se interpretan como etiquetas HTML. `th:href` construye el enlace de edición; la forma `{id}(id=${pelicula.id})` sustituye el segmento `{id}` por el identificador de esa fila. `th:action` construye de la misma manera la ruta del formulario POST para borrar.

En `formulario.html`, el controlador añade al `Model` un `FormularioPelicula` con el nombre `formulario`. `th:object="${formulario}"` lo selecciona para ese `<form>`. Por eso `th:field="*{titulo}"` enlaza el `<input>` con `getTitulo()` y `setTitulo(...)`: genera los atributos HTML necesarios para enviar el campo con nombre `titulo` y mostrar su valor actual. Ocurre lo mismo con `genero`. En el alta el objeto empieza vacío; en la edición el controlador lo rellena con los datos existentes. Si el servicio rechaza la entrada, se devuelve la misma vista con ese objeto y los valores escritos reaparecen en los campos. `required` es validación **HTML del navegador**, no una instrucción `th:` ni un sustituto de la validación del servicio.

El mismo `formulario.html` sirve para alta y edición. Su `th:action` usa una expresión condicional: si no hay `id` en el modelo, envía el POST a `/peliculas`; si existe, lo envía a `/peliculas/{id}/editar`. Por ejemplo, al editar la película `3`, Thymeleaf genera una acción equivalente a `/peliculas/3/editar`. `th:text="${tituloPagina}"` cambia el título de la página. `th:if="${error}"` muestra el párrafo de error únicamente cuando el controlador añadió ese atributo, y `th:text="${error}"` escribe el mensaje escapado. El enlace «Volver al listado» usa `th:href="@{/peliculas}"`.

Al enviar el formulario, Spring MVC usa los nombres de los campos HTML para rellenar `FormularioPelicula` en el parámetro `@ModelAttribute("formulario")` del controlador. Thymeleaf interviene al **generar la página**; Spring MVC interviene al **recibir el POST**. Esa separación explica el recorrido completo de `th:field` hasta `PeliculaControlador`.

El navegador aplica `required`, pero el servicio también valida: una petición HTTP puede enviarse sin pasar por el formulario. Si falta un dato, el controlador vuelve a la vista con un mensaje y conserva lo escrito. Cuando el alta o la edición terminan correctamente, devuelve `"redirect:/peliculas"`: el navegador hace una nueva petición GET, por lo que recargar el listado no repite el POST. Esto es el patrón **Post/Redirect/Get**. No todos los GET obligan a usar una vista ni todos los POST obligan a redirigir: si hay un error de formulario, este controlador devuelve la vista en la misma petición.

Si se pide editar o borrar un ID que no existe, el controlador responde `404`. Borrar se hace por POST mediante un formulario; seguir un enlace GET no modifica el catálogo.

## Estructura relevante

```text
13_SpringMVC/
├── pom.xml
└── src/main/
    ├── java/springmvc/
    │   ├── Aplicacion.java
    │   ├── controlador/{PeliculaControlador,FormularioPelicula}.java
    │   └── modelo/
    │       ├── entidad/Pelicula.java
    │       ├── servicio/PeliculaServicio.java
    │       └── persistencia/PeliculaRepositorio.java
    └── resources/
        ├── application.properties
        └── templates/{listado,formulario}.html
```

## Cómo comprobar que funciona

1. Abre `/peliculas`: aparece el listado vacío y el enlace «Añadir película».
2. Crea `Coco` con género `Animación`: vuelves a `/peliculas` y aparece la fila con el ID generado por H2 (habitualmente `1` en una base recién creada).
3. Pulsa «Editar», cambia el género y comprueba el resultado en el listado.
4. Pulsa «Borrar»: la fila desaparece.
5. Envía un título vacío mediante una petición POST directa, sin la validación del navegador: se muestra «El título es obligatorio» y no se guarda nada.
6. Abre `/peliculas/999/editar`: recibes `404`.

Al reiniciar la aplicación se pierde el catálogo porque H2 está en memoria; la base genera de nuevo los IDs.

## Qué cambia respecto al proyecto antiguo

El README original indicaba correctamente que Boot puede arrancar un servidor integrado y que `application.properties` permite cambiar el puerto. Aquí empaquetamos un **JAR ejecutable**, no un WAR para desplegar en un Tomcat externo. No trasladamos `web.xml`, `ServletInitializer`, Jasper, JSTL ni `spring.mvc.view.prefix/suffix` para JSP: las plantillas Thymeleaf se resuelven desde `templates` con su starter. Tampoco hace falta `src/main/webapp`.

El texto antiguo afirmaba que Thymeleaf venía incluido por defecto; en realidad hay que añadir `spring-boot-starter-thymeleaf`. También usaba Java 8, Spring Boot 2 y páginas ISO-8859-1: este módulo usa Java 25, Spring Boot 4 y UTF-8. El login del original mantenía estado de usuario en un bean compartido y mostraba una contraseña en un mensaje de error; no se traslada porque no es un ejemplo adecuado de sesiones o autenticación. La capa JPA y H2 aprovecha lo aprendido en el módulo 12 sin repetir aquí toda su teoría.

## Errores frecuentes y práctica

- Buscar una JSP bajo `WEB-INF`: estas vistas son plantillas HTML bajo `src/main/resources/templates`.
- Devolver `"listado.html"` desde el controlador: se devuelve el nombre lógico `"listado"`.
- Usar `@RestController` y esperar que un `String` seleccione una vista.
- Hacer que la plantilla llame al repositorio o que el controlador guarde directamente en él.
- Confiar solo en `required` y omitir la validación del servidor.

Para practicar, añade una búsqueda por título en el servicio y un formulario GET en el listado. Después muestra un mensaje cuando no haya coincidencias, manteniendo `th:text` para los datos introducidos por el usuario.

## Documentación para ampliar

- [Controladores anotados de Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html).
- [Guía oficial de contenido web con Thymeleaf](https://spring.io/guides/gs/serving-web-content/).
- [Tutorial oficial de expresiones y atributos de Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html).
- [Tutorial oficial de formularios Thymeleaf con Spring MVC](https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html).
- [Starters de Spring Boot](https://docs.spring.io/spring-boot/reference/using/build-systems.html).
