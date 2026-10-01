# Workspace DWES

Ejemplos progresivos de **Desarrollo Web en Entorno Servidor (DWES)** para el ciclo de **Desarrollo de Aplicaciones Web (DAW)**. Cada carpeta es un módulo Maven independiente y se centra en un concepto. Este repositorio es material de aprendizaje, no una única aplicación que haya que desplegar completa.

## Antes de empezar

- **Java 25** y **Maven 3.9 o compatible** para compilar los módulos.
- **IntelliJ IDEA** como entorno de referencia. La edición Community permite trabajar con Maven y Java; para ejecutar módulos web puede utilizarse Smart Tomcat.
- **Apache Tomcat 11** solo para los ejemplos con servlets o JSP. Los ejemplos de consola no lo necesitan.
- **H2** se incluye como dependencia en los ejemplos de base de datos y funciona sin instalar un servidor. **MariaDB de XAMPP** es una alternativa opcional; cada módulo explica su configuración.

Los módulos usan Maven y codificación UTF-8; los ejemplos Jakarta EE emplean `jakarta.*`. `11_SpringBoot` descarga sus dependencias mediante Maven y no requiere instalar Spring por separado.

## Recorrido de los módulos

Sigue el orden numérico; cada README contiene objetivos, explicación del código y pasos concretos para probar el ejemplo.

| Módulo | Idea principal | Ejecución |
| --- | --- | --- |
| [`00_Java`](00_Java/) | Repaso de clases, colecciones y otros fundamentos de Java. | Consola |
| [`01_HolaMundoWeb`](01_HolaMundoWeb/README.md) | Primer servlet y configuración de Tomcat en IntelliJ Community. | Web |
| [`02_ServletsRequest`](02_ServletsRequest/README.md) | Peticiones, parámetros y respuestas de un servlet. | Web |
| [`03_HTTP`](03_HTTP/README.md) | Métodos HTTP, códigos de estado, cabeceras y pruebas de peticiones. | Web |
| [`04_JSP`](04_JSP/README.md) | JSP, EL, JSTL y presentación de datos enviados por un servlet. | Web |
| [`05_Formulario`](05_Formulario/README.md) | Procesamiento y validación de formularios. | Web |
| [`06_Sesion`](06_Sesion/README.md) | Sesiones HTTP y datos entre peticiones. | Web |
| [`07_JDBC`](07_JDBC/README.md) | CRUD de productos con JDBC y H2 o MariaDB. | Web |
| [`08_MVC`](08_MVC/README.md) | Separación entre controlador, servicio, persistencia y vistas. | Web |
| [`09_JPA`](09_JPA/README.md) | Primeras operaciones JPA: **primero consola y después web**. | Consola + web |
| [`10_JPARelaciones`](10_JPARelaciones/README.md) | Relaciones JPA unidireccionales. | Consola |
| [`11_SpringBoot`](11_SpringBoot/README.md) | Contenedor Spring e inyección de dependencias con Spring Boot. | Consola |
| [`12_SpringDataJPA`](12_SpringDataJPA/README.md) | Repositorios Spring Data JPA y consultas sobre H2. | Consola |
| [`13_SpringMVC`](13_SpringMVC/README.md) | Spring MVC con controladores, formularios y vistas Thymeleaf. | Web con servidor integrado |
| [`14_JSON`](14_JSON/README.md) | Conversión entre JSON, objetos Java y ficheros con Gson. | Consola |
| [`15_SpringRest`](15_SpringRest/README.md) | API REST con JSON, métodos HTTP y CRUD de personas sobre Spring Data JPA y H2. | Web con servidor integrado |

## Abrir y compilar

Abre la **carpeta raíz `WsDWES`** en IntelliJ y recarga los proyectos Maven. El `pom.xml` de esta carpeta es un **agregador**: enumera los módulos, pero no es la aplicación web que debes seleccionar para ejecutar Tomcat. Cada módulo tiene su propio `pom.xml` y sus dependencias.

Desde esta carpeta puedes compilar un módulo concreto, por ejemplo:

```bash
mvn -pl 07_JDBC -am clean package
```

Sustituye `07_JDBC` por el nombre del módulo que estés estudiando. Para intentar construir todos los módulos del workspace, ejecuta `mvn clean package` desde la raíz. Los WAR de los módulos web y los JAR de consola se generan en la carpeta `target/` de cada módulo; esos archivos son resultado de la compilación y no se versionan.

## Ejecutar los ejemplos

**Módulos de consola:** ejecuta la clase `main` indicada en el README del módulo desde IntelliJ. Los JAR de los módulos Java anteriores a Spring Boot no incluyen por sí solos todas sus dependencias; IntelliJ usa el classpath del módulo. En cambio, el JAR de `11_SpringBoot` se puede ejecutar con `java -jar`. En `09_JPA`, realiza primero el recorrido de consola y después pasa a la parte web.

**Módulos web anteriores a Spring Boot:** necesitas Tomcat 11. En IntelliJ Community, una configuración de Smart Tomcat debe apuntar a `src/main/webapp` del módulo elegido como **Deployment Directory** y utilizar ese mismo módulo en **Use classpath of module**. Alternativamente, despliega en Tomcat el WAR generado por Maven. El **Context Path** determina el prefijo de la URL: si usas `/jdbc`, la ruta `/productos` se abre en `http://localhost:8080/jdbc/productos`. Si despliegas un WAR con otro contexto, cambia ese prefijo. Conserva una configuración de Tomcat por módulo para no mezclar directorios ni classpaths. La [guía del primer módulo](01_HolaMundoWeb/README.md) muestra la configuración inicial; cada README indica su ruta de comprobación.

**`13_SpringMVC` y `15_SpringRest`:** son excepciones a las instrucciones de Smart Tomcat anteriores: Boot arranca su propio servidor al ejecutar `Aplicacion.main` o el JAR. Consulta sus README para las rutas y pruebas.

**Bases de datos:** `07_JDBC`, `08_MVC`, `09_JPA`, `10_JPARelaciones`, `12_SpringDataJPA`, `13_SpringMVC` y `15_SpringRest` incluyen H2 para empezar sin XAMPP. El módulo 12 explica la alternativa MariaDB; los módulos 13 y 15 utilizan H2 y se centran en MVC y REST, respectivamente. No copies contraseñas reales al repositorio. Los datos y la configuración de conexión de un módulo no deben suponerse compartidos por los demás.

## Si algo falla

- Comprueba que IntelliJ utiliza **Java 25**, que Maven se ha recargado y que compilas el módulo correcto.
- Si Tomcat no encuentra un servlet o una dependencia, revisa el **Deployment Directory**, el módulo del classpath y el contexto configurado. Tomcat 9 no sirve para estos imports `jakarta.*`.
- Si aparece un `404`, comprueba primero el contexto de despliegue y la ruta exacta del README del módulo. Las JSP situadas en `WEB-INF` no se abren directamente desde el navegador.
- Si falla una conexión a MariaDB, verifica que XAMPP está iniciado, que existe la base indicada y que URL, usuario y contraseña corresponden a tu instalación.

Las explicaciones comunes están aquí; los pasos específicos y resultados esperados permanecen en el README de cada módulo.
