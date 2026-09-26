# Workspace DWES

Ejemplos progresivos de **Desarrollo Web en Entorno Servidor (DWES)** para el ciclo de **Desarrollo de Aplicaciones Web (DAW)**. Cada carpeta es un módulo Maven independiente y se centra en un concepto. Este repositorio es material de aprendizaje, no una única aplicación que haya que desplegar completa.

## Antes de empezar

- **Java 25** y **Maven 3.9 o compatible** para compilar los módulos.
- **IntelliJ IDEA** como entorno de referencia. La edición Community permite trabajar con Maven y Java; para ejecutar módulos web puede utilizarse Smart Tomcat.
- **Apache Tomcat 11** solo para los ejemplos con servlets o JSP. Los ejemplos de consola no lo necesitan.
- **H2** se incluye como dependencia en los ejemplos de base de datos y funciona sin instalar un servidor. **MariaDB de XAMPP** es una alternativa opcional; cada módulo explica su configuración.

Los módulos usan las APIs actuales de **Jakarta EE** (`jakarta.*`), Maven y codificación UTF-8. No hay que instalar Spring para los ejemplos presentes.

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

## Abrir y compilar

Abre la **carpeta raíz `WsDWES`** en IntelliJ y recarga los proyectos Maven. El `pom.xml` de esta carpeta es un **agregador**: enumera los módulos, pero no es la aplicación web que debes seleccionar para ejecutar Tomcat. Cada módulo tiene su propio `pom.xml` y sus dependencias.

Desde esta carpeta puedes compilar un módulo concreto, por ejemplo:

```bash
mvn -pl 07_JDBC -am clean package
```

Sustituye `07_JDBC` por el nombre del módulo que estés estudiando. Para intentar construir todos los módulos del workspace, ejecuta `mvn clean package` desde la raíz. Los WAR de los módulos web y los JAR de consola se generan en la carpeta `target/` de cada módulo; esos archivos son resultado de la compilación y no se versionan.

## Ejecutar los ejemplos

**Módulos de consola:** ejecuta la clase `main` indicada en el README del módulo desde IntelliJ. Compilar un JAR con Maven no significa que ese JAR incluya todas las dependencias para ejecutarlo por sí solo; IntelliJ usa el classpath del módulo. En `09_JPA`, realiza primero el recorrido de consola y después pasa a la parte web.

**Módulos web:** necesitas Tomcat 11. En IntelliJ Community, una configuración de Smart Tomcat debe apuntar a `src/main/webapp` del módulo elegido como **Deployment Directory** y utilizar ese mismo módulo en **Use classpath of module**. Alternativamente, despliega en Tomcat el WAR generado por Maven. El **Context Path** determina el prefijo de la URL: si usas `/jdbc`, la ruta `/productos` se abre en `http://localhost:8080/jdbc/productos`. Si despliegas un WAR con otro contexto, cambia ese prefijo. Conserva una configuración de Tomcat por módulo para no mezclar directorios ni classpaths. La [guía del primer módulo](01_HolaMundoWeb/README.md) muestra la configuración inicial; cada README indica su ruta de comprobación.

**Bases de datos:** `07_JDBC`, `08_MVC`, `09_JPA` y `10_JPARelaciones` incluyen H2 para empezar sin XAMPP. Si quieres usar MariaDB, sigue las instrucciones y el script SQL del módulo correspondiente. No copies contraseñas reales al repositorio. Los datos y la configuración de conexión de un módulo no deben suponerse compartidos por los demás.

## Si algo falla

- Comprueba que IntelliJ utiliza **Java 25**, que Maven se ha recargado y que compilas el módulo correcto.
- Si Tomcat no encuentra un servlet o una dependencia, revisa el **Deployment Directory**, el módulo del classpath y el contexto configurado. Tomcat 9 no sirve para estos imports `jakarta.*`.
- Si aparece un `404`, comprueba primero el contexto de despliegue y la ruta exacta del README del módulo. Las JSP situadas en `WEB-INF` no se abren directamente desde el navegador.
- Si falla una conexión a MariaDB, verifica que XAMPP está iniciado, que existe la base indicada y que URL, usuario y contraseña corresponden a tu instalación.

Las explicaciones comunes están aquí; los pasos específicos y resultados esperados permanecen en el README de cada módulo.
