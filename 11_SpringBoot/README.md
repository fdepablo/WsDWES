# Primeros pasos con Spring Boot: catálogo en consola

Este módulo retoma la idea de películas y capas de `WsSpringUpgrade` para mostrar cómo Spring crea y conecta objetos en una aplicación pequeña. Se ejecuta en consola; la aplicación web con MVC y Thymeleaf llegará en [`13_SpringMVC`](../13_SpringMVC/README.md).

La organización `modelo.entidad`, `modelo.persistencia` y `modelo.servicio` se mantiene en [`12_SpringDataJPA`](../12_SpringDataJPA/README.md): así se puede comparar la misma arquitectura cuando el almacenamiento pasa de una lista a JPA. Todos los paquetes quedan bajo `springboot` para que la búsqueda de componentes de Boot los incluya.

## Qué vas a aprender

- Distinguir una clase Java normal de un **bean** gestionado por Spring.
- Reconocer inversión de control e inyección de dependencias en el código.
- Comparar la inyección por constructor con la inyección en campo usando `@Autowired`.
- Identificar las funciones de `@Component`, `@Service` y `@Repository`.
- Arrancar Spring Boot con `@SpringBootApplication` y ejecutar código al inicio con `CommandLineRunner`.
- Separar presentación, reglas de negocio y almacenamiento temporal.
- Leer un valor de `application.properties` mediante `@Value`.

## Requisitos

Java 25, Maven e IntelliJ IDEA (Community sirve). Consulta el [README raíz](../README.md) para los requisitos comunes. No hace falta instalar Spring, Tomcat ni una base de datos: Maven descarga las bibliotecas y el ejemplo no incluye servidor web.

## Spring Framework y Spring Boot

**Spring Framework** ofrece, entre otras herramientas, un contenedor de inversión de control (IoC). En este ejemplo, el contenedor crea los objetos que forman la aplicación y conecta sus dependencias. **Spring Boot** utiliza Spring Framework y facilita el arranque y la configuración inicial de la aplicación. El `spring-boot-starter` del `pom.xml` reúne las dependencias básicas; su versión la gestiona `spring-boot-starter-parent`. Maven las descarga, así que no se instala Spring manualmente.

El código anterior de `WsSpringUpgrade` configuraba ejemplos con XML, anotaciones y clases Java. Aquí usamos anotaciones y la configuración que proporciona Boot. XML sigue siendo una posibilidad en Spring, pero no aporta nada a este primer proyecto. Tampoco necesitamos un complemento específico de Eclipse: IntelliJ IDEA y Maven bastan.

### Inversión de control, beans e inyección

Sin contenedor, `DemoConsola` tendría que construir un `PeliculaRepositorio` y pasarlo a un nuevo `PeliculaServicio`. Aquí las clases declaran qué necesitan mediante un constructor o un campo anotado, y Spring crea y conecta esas instancias. Esa cesión del control de su creación al contenedor es **inversión de control**; pasar a una clase los objetos de los que depende es **inyección de dependencias**.

Un **bean** es un objeto cuya creación y ciclo de vida gestiona el contenedor. En el ejemplo, `PeliculaRepositorio`, `PeliculaServicio` y `DemoConsola` son beans. No todos los objetos Java lo son: cada `Pelicula` se crea al guardar un dato y no la gestiona Spring. Por tanto, Spring no elimina `new` del programa; ayuda a conectar los componentes de la aplicación. Por defecto, estos beans tienen alcance *singleton*: se mantiene una instancia de cada uno dentro de este contexto, no una instancia universal para cualquier aplicación o proceso.

`ApplicationContext` es la interfaz habitual del contenedor en una aplicación Spring. Se apoya en las capacidades de `BeanFactory` y añade otras funciones. `SpringApplication.run(...)` arranca el contexto en este módulo, por lo que no tenemos que construirlo manualmente. Aunque se puede pedir un bean al contexto con `getBean`, la inyección permite que cada clase declare sus dependencias sin buscarlas por su cuenta. El contenedor resuelve aquí los argumentos de constructor por tipo; los beans también tienen nombres, pero la inyección de este ejemplo no depende de escribirlos.

### Qué hace Boot al arrancar

`@SpringBootApplication` reúne la configuración de Spring Boot, la **búsqueda de componentes** y la **configuración automática**. Al estar `Aplicacion` en el paquete `springboot`, Spring encuentra las clases anotadas en ese paquete y sus subpaquetes. La configuración automática tiene en cuenta las dependencias presentes; no significa que Boot cree cualquier infraestructura imaginable. Como no hemos añadido un starter web ni dependencias de base de datos, este proyecto no arranca un servidor HTTP ni configura una base.

Spring reconoce las clases anotadas durante la búsqueda de componentes y crea sus instancias. `PeliculaServicio` recibe el repositorio por su único constructor, sin necesitar `@Autowired`. Para mostrar la otra forma, `DemoConsola` tiene el campo `servicio` anotado con `@Autowired`: Spring crea la instancia y después asigna el bean `PeliculaServicio` a ese campo.

Ambas formas funcionan. Spring Boot [recomienda la inyección por constructor](https://docs.spring.io/spring-boot/reference/using/spring-beans-and-dependency-injection.html) para las dependencias obligatorias: el constructor muestra lo que necesita la clase, permite marcar el campo como `final` y deja el objeto listo desde su creación. Con inyección en campo, `new DemoConsola()` deja `servicio` a `null` si Spring no interviene; para probar la clase de forma aislada hay que arrancar Spring o recurrir a técnicas adicionales para asignar el campo privado. En este módulo la inyección en campo sirve para reconocer una forma que todavía puede encontrarse en proyectos existentes.

### Anotaciones de Spring usadas en el módulo

| Anotación | Dónde aparece | Qué hace aquí |
| --- | --- | --- |
| `@SpringBootApplication` | `Aplicacion` | Habilita la configuración de Boot, la búsqueda de componentes y la configuración automática. |
| `@Component` | `DemoConsola` | Registra la clase como bean para que Boot pueda ejecutar su `CommandLineRunner`. |
| `@Service` | `PeliculaServicio` | Registra el bean de lógica de negocio; es una especialización de `@Component`. |
| `@Repository` | `PeliculaRepositorio` | Registra el bean de acceso a datos; es una especialización de `@Component`. Aquí usa una lista, no una base de datos. |
| `@Autowired` | Campo `DemoConsola.servicio` | Pide a Spring que asigne un bean `PeliculaServicio` a ese campo. |
| `@Value` | Constructor de `PeliculaRepositorio` | Obtiene de la configuración el valor de `catalogo.maximo-peliculas`. |

`@Override` también aparece en `DemoConsola`, pero es una anotación de **Java**, no de Spring: indica que `run` implementa el método de `CommandLineRunner`. `@Bean` se menciona como otra posibilidad, aunque no se usa en este código.

Después de arrancar el contexto, Boot ejecuta `DemoConsola.run(...)` porque la clase implementa `CommandLineRunner`. Así se puede observar la aplicación desde la consola sin mezclar todavía controladores HTTP, vistas ni Thymeleaf.

**¿Cómo llega `Aplicacion` a `DemoConsola` si no la llama?** `Aplicacion.main()` invoca `SpringApplication.run(...)`; esa llamada inicia la búsqueda de componentes. Spring encuentra `DemoConsola` por `@Component`, crea su instancia e inyecta `PeliculaServicio` en el campo anotado con `@Autowired`. Finalmente, Boot detecta que implementa `CommandLineRunner` y llama a `run(...)`. Por eso `Aplicacion` no contiene `new DemoConsola(...)` ni una llamada directa a su método: la conexión y la ejecución forman parte del arranque del contenedor.

### Propiedades de configuración

Boot carga `src/main/resources/application.properties` al arrancar. Aquí contiene `catalogo.maximo-peliculas=2`. En el constructor de `PeliculaRepositorio`, `@Value("${catalogo.maximo-peliculas}")` pide a Spring ese valor y lo recibe como `int`. El repositorio rechaza una tercera película cuando ya hay dos. El límite se configura fuera del código Java; cambia el valor del fichero y vuelve a arrancar para probar otra capacidad. Si falta la propiedad o no puede convertirse a un número, la aplicación no podrá crear el repositorio al arrancar.

En la misma carpeta está `banner.txt`. Boot lo detecta automáticamente y muestra su texto al iniciar, antes de ejecutar `DemoConsola`. Puedes cambiar el mensaje y volver a arrancar para comprobar que es un recurso de configuración, no una cadena escrita en una clase Java.

El proyecto original solo fijaba `spring.application.name`, que da nombre a la aplicación pero no se consultaba en su código. Esta propiedad propia permite observar un cambio de comportamiento. El `@Value` con un número literal que había en otro ejemplo antiguo tampoco leía `application.properties`.

## Estructura relevante

```text
11_SpringBoot/
├── pom.xml
└── src/main/
    ├── resources/
    │   ├── application.properties
    │   └── banner.txt
    └── java/springboot/
        ├── Aplicacion.java
        ├── presentacion/DemoConsola.java
        └── modelo/
            ├── entidad/Pelicula.java
            ├── persistencia/PeliculaRepositorio.java
            └── servicio/PeliculaServicio.java
```

`PeliculaRepositorio` guarda datos en una lista, sin base de datos. `PeliculaServicio` comprueba las entradas y coordina las operaciones. `DemoConsola` muestra el resultado. La dirección de las dependencias es `DemoConsola → PeliculaServicio → PeliculaRepositorio`; `Pelicula` representa los datos que pasan por las tres capas.

`Pelicula` es un `record`, incorporado de forma definitiva en **Java 16**. Java genera su constructor, los métodos de acceso `id()`, `titulo()` y `genero()`, y métodos como `toString()`. Por eso `PeliculaRepositorio` puede crearla con `new Pelicula(...)` y consultar su identificador con `pelicula.id()` sin que tengamos que escribir esos métodos.

## Cómo ejecutar y comprobar

Desde la raíz del workspace:

```bash
mvn -pl 11_SpringBoot -am clean package
java -jar 11_SpringBoot/target/11_SpringBoot-1.0-SNAPSHOT.jar
```

También puedes abrir la raíz en IntelliJ, recargar Maven y ejecutar `springboot.Aplicacion` con JDK 25. No configures Smart Tomcat: este módulo no atiende peticiones HTTP.

Con el límite inicial `2`, en la consola aparecerán dos películas, la búsqueda del ID `1`, `null` para el ID inexistente `99`, el mensaje `Alta rechazada: El título no puede estar vacío`, otro mensaje al intentar guardar `Amélie` porque el catálogo está lleno y un total final de `2`. Al ejecutar de nuevo, el catálogo vuelve a empezar vacío: la lista vive solo mientras está en marcha el proceso.

## Sigue el código

Empieza en `Aplicacion.main`: Boot arranca el contexto. Sigue el campo `DemoConsola.servicio`, el constructor de `PeliculaServicio` y el constructor de `PeliculaRepositorio` para comparar las dos formas de inyección y el valor configurado. En `run`, el alta pasa por `PeliculaServicio.crear`, que comprueba título y género antes de llamar a `guardar`; el repositorio comprueba la capacidad antes de añadir la película. La búsqueda recorre la lista con un bucle. Los rechazos se capturan solo en la demostración para que la ejecución continúe; no se inserta ninguna película rechazada.

En particular, observa el constructor de `PeliculaServicio`: recibe un `PeliculaRepositorio`, pero no lo instancia. Al arrancar, Spring encuentra el repositorio anotado, crea el servicio con esa dependencia y luego lo asigna al campo de `DemoConsola`. Si faltara un bean necesario, el contexto no podría arrancar. Esta relación hace visible la diferencia entre **declarar una dependencia** y **construirla**.

## Errores frecuentes

- Ejecutar con un JDK distinto del 25 configurado en Maven.
- Esperar una página en `localhost:8080`: no se ha añadido el starter web.
- Confundir una película creada con `new Pelicula(...)` con un bean del contenedor.
- Esperar que los datos persistan entre ejecuciones: todavía no hay JPA ni base de datos.

## Para practicar

1. Cambia `catalogo.maximo-peliculas` a `3` y comprueba que `Amélie` se guarda con el ID `3`.
2. Intenta crear una película con género vacío y comprueba que el total no cambia.
3. Añade al servicio una búsqueda por título usando un bucle y muéstrala desde `DemoConsola`.

## Documentación para ampliar

- [Contenedor IoC y beans de Spring Framework](https://docs.spring.io/spring-framework/reference/core/beans/introduction.html).
- [Inyección por constructor y `@Autowired`](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html).
- [`@SpringBootApplication` y sus funciones](https://docs.spring.io/spring-boot/reference/using/using-the-springbootapplication-annotation.html).
- [`CommandLineRunner` en el arranque](https://docs.spring.io/spring-boot/reference/features/spring-application.html).
