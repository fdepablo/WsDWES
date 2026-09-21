# 🌐 Guía de Configuración: Servlets con Tomcat 11 y Java 25 en IntelliJ Community

Esta guía explica cómo configurar un entorno de desarrollo web profesional basado en **módulos independientes** utilizando **IntelliJ IDEA Community**, **Maven**, **Java 25** y **Apache Tomcat 11**.

---

## 🛠️ Paso 1: Instalar el plugin "Smart Tomcat"
Dado que la versión *Community* de IntelliJ no incluye soporte nativo para servidores web, utilizaremos un plugin gratuito.

1. Ve a **File > Settings** (o *IntelliJ IDEA > Settings* en Mac).
2. En la barra lateral izquierda, selecciona **Plugins**.
3. Haz clic en la pestaña **Marketplace** (arriba).
4. Busca `Smart Tomcat` e instala el plugin.
5. **Reinicia el IDE** si te lo solicita para aplicar los cambios.

---

## 📦 Paso 2: Crear el Módulo Web Independiente
Para evitar conflictos de dependencias entre ejercicios del curso, crearemos el módulo sin herencia.

1. Haz clic derecho sobre la carpeta raíz de tu espacio de trabajo y selecciona **New > Module...**
2. Configura los parámetros de la ventana exactamente así:
    * **Name:** `nombre-de-tu-practica`
    * **Language:** `Java`
    * **Build system:** `Maven`
    * **Parent:** **`None`** *(¡Crucial para que sea un proyecto aislado y autocontenido!)*
3. Haz clic en **Create**.

### Registrar el módulo en el proyecto raíz

Aunque cada ejercicio tenga su propio `pom.xml` y sus propias dependencias, el `pom.xml` situado en la raíz de `WsDWES` funciona como agregador. Añade allí cada módulo nuevo:

```xml
<packaging>pom</packaging>

<modules>
    <module>00_Java</module>
    <module>01_HolaMundoWeb</module>
    <module>nombre-de-tu-practica</module>
</modules>
```

Después, abre la ventana **Maven** de IntelliJ y pulsa **Reload All Maven Projects**. Así IntelliJ reconocerá el nuevo módulo y Smart Tomcat podrá usar sus clases compiladas.

> Si el módulo todavía no aparece, haz clic derecho sobre su `pom.xml` y selecciona **Add as Maven Project**.

---

## 📝 Paso 3: Configurar el archivo `pom.xml`
Abre el archivo `pom.xml` del nuevo módulo que acabas de crear y sustituye todo su contenido por la siguiente estructura (compatible con **Java 25** y **Tomcat 11**):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.sinergiafp</groupId>
    <artifactId>tu-practica-web</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging> <!-- Define que generará un archivo web comprimido -->

    <properties>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- API Oficial de Jakarta Servlets para Tomcat 11 -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.1.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

> ⚠️ **¡IMPORTANTE!** Tras pegar el código anterior, haz clic en el **botón flotante de Maven** (icono del elefante con flechas azules) que aparece arriba a la derecha en la pestaña del editor para descargar y sincronizar las librerías.

---

## 📁 Paso 4: Crear la Estructura de Carpetas Web y Código

### A) El contenedor de páginas web (`webapp`):
1. Despliega la carpeta `src` del módulo y haz clic derecho sobre la carpeta **`main`**.
2. Selecciona **New > Directory** y escribe exactamente el nombre **`webapp`**.
3. Dentro de `webapp`, crea tu archivo de inicio: Clic derecho > **New > File** llamado `index.html`. Añade un código básico:
   ```html
   <h1>¡Hola Mundo desde mi servidor web local!</h1>
   ```

### B) El código Java (Servlet):
1. Ve a `src/main/java`.
2. Crea un paquete haciendo clic derecho > **New > Package** (ej: `com.servlet`).
3. Dentro del paquete, crea una clase Java llamada `HolaServlet` con este código:
   ```java
   package com.servlet;

   import jakarta.servlet.annotation.WebServlet;
   import jakarta.servlet.http.HttpServlet;
   import jakarta.servlet.http.HttpServletRequest;
   import jakarta.servlet.http.HttpServletResponse;
   import java.io.IOException;

   @WebServlet("/hola")
   public class HolaServlet extends HttpServlet {
       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
           resp.setContentType("text/html");
           resp.getWriter().println("<h2>¡Servlet levantado con éxito en Java 25 y Tomcat 11!</h2>");
       }
   }
   ```

---

## 🚀 Paso 5: Configurar el Lanzador y Ejecutar

1. En la parte superior derecha de IntelliJ, haz clic en el desplegable de configuraciones (al lado del botón de Play verde) y selecciona **Edit Configurations...**
2. Haz clic en el botón **`+`** (arriba a la izquierda) y elige **Smart Tomcat** en la lista.
3. Configura detalladamente los siguientes campos:
    * **Name:** `Tomcat 11`
    * **Tomcat Server:** Pulsa el icono de la carpeta y selecciona el directorio en tu disco duro donde está descomprimido **Tomcat 11**. Si no está instalado, descárgalo desde la [página oficial de Apache Tomcat 11](https://tomcat.apache.org/download-11.cgi).
    * **Deployment Directory:** Selecciona la ruta de tu carpeta **`webapp`** (`.../src/main/webapp`).
    * **Context Path:** Escribe una única barra diagonal estándar **`/`**
    * **Module:** Selecciona el módulo web que vas a ejecutar, por ejemplo **`01_HolaMundoWeb`**. Si se selecciona `WsDWES`, se publicará el HTML pero las clases de los servlets no estarán disponibles.
4. Haz clic en **Apply** y luego en **OK**.

### Trabajar con varios módulos web

Smart Tomcat ejecuta un módulo web por configuración. Para no editarla cada vez, duplica la configuración y crea una por ejercicio, por ejemplo `Tomcat - 01_HolaMundoWeb` y `Tomcat - 02_Formularios`. En cada copia cambia **Deployment Directory** y **Module**.

Las configuraciones de IntelliJ y Smart Tomcat son locales (`.idea/workspace.xml`, `.idea/compiler.xml` y `.smarttomcat/` no se versionan). Por tanto, cada alumno debe crear su configuración de ejecución después de clonar el repositorio; la estructura común de módulos se reconstruye desde los archivos `pom.xml`.

---

## 🌍 Verificación final
Haz clic en el botón de **Play verde** en la barra superior. Una vez que la consola inferior muestre que Tomcat se ha iniciado correctamente, abre tu navegador e ingresa a:

* **Página Web Estática:** `http://localhost:8080/index.html`
* **Controlador Java (Servlet):** `http://localhost:8080/hola`
