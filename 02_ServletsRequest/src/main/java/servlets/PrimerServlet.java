package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.UtilsHTML;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lee los parámetros enviados por el formulario y genera una página dinámica.
 *
 * <p>Tomcat crea los objetos {@link HttpServletRequest} y
 * {@link HttpServletResponse} para cada petición e invoca este servlet.</p>
 */
@WebServlet("/primer-servlet")
public class PrimerServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Atiende el envío GET del formulario inicial.
     *
     * <p>En una petición {@code GET}, los parámetros se añaden a la URL
     * después del signo {@code ?}. Esto permite verlos, copiar la dirección y
     * repetir la petición fácilmente. {@code GET} se utiliza normalmente para
     * consultar información y no debería modificar el estado del servidor.</p>
     *
     * <p>Por ejemplo, este formulario produce una URL similar a:</p>
     * <pre>{@code
     * /primer-servlet?nombrePersona=Ana&edadPersona=24
     * }</pre>
     *
     * @param request petición HTTP, que contiene los parámetros del formulario
     * @param response respuesta HTTP que se devolverá al navegador
     * @throws ServletException si el contenedor no puede procesar la petición
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String nombre = request.getParameter("nombrePersona");
        String edadRecibida = request.getParameter("edadPersona");

        if (nombre == null || nombre.isBlank() || edadRecibida == null || edadRecibida.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Debes indicar un nombre y una edad.");
            return;
        }

        final int edad;
        try {
            edad = Integer.parseInt(edadRecibida);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La edad debe ser un número entero.");
            return;
        }

        if (edad < 0 || edad > 130) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La edad debe estar comprendida entre 0 y 130 años.");
            return;
        }

        String nombreSeguro = UtilsHTML.escapar(nombre.strip());
        String ahora = LocalDateTime.now().format(FORMATO_FECHA);
        String urlSegundoServlet = request.getContextPath() + "/segundo-servlet";

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Primer servlet</title>
                    </head>
                    <body>
                    <main>
                    """);
            out.printf("<h1>Hola, %s</h1>%n", nombreSeguro);
            out.printf("<p>Tu edad es %d años.</p>%n", edad);
            out.printf("<p>Fecha y hora del servidor: %s</p>%n", ahora);
            out.println("<h2>Crear una segunda petición</h2>");
            out.println("<p>El siguiente formulario solo propagará la edad. "
                    + "Observa qué ocurre con el nombre en el segundo servlet.</p>");
            out.printf("<form action=\"%s\" method=\"get\">%n", urlSegundoServlet);
            out.printf("<input type=\"hidden\" name=\"edadPersona\" value=\"%d\">%n", edad);
            out.println("<button type=\"submit\">Ir al segundo servlet</button>");
            out.println("""
                    </form>
                    </main>
                    </body>
                    </html>
                    """);
        }
    }

    /**
     * Atiende una petición POST reutilizando el procesamiento de
     * {@link #doGet(HttpServletRequest, HttpServletResponse)}.
     *
     * <p>En una petición {@code POST}, los datos del formulario se envían
     * normalmente en el cuerpo de la petición y no aparecen en la URL. Se usa
     * habitualmente para crear o modificar información, enviar formularios
     * extensos o transmitir datos que no conviene mostrar en la dirección.</p>
     *
     * <p>Delegar en {@code doGet} evita duplicar código en este ejemplo porque
     * ambos métodos solo muestran los parámetros recibidos. En una aplicación
     * real suelen tener responsabilidades diferentes: {@code GET} muestra o
     * consulta datos, mientras que {@code POST} procesa cambios.</p>
     *
     * <p>Que los datos no aparezcan en la URL no hace que {@code POST} sea
     * seguro por sí mismo. Para protegerlos es necesario utilizar HTTPS.</p>
     *
     * @param request petición HTTP, que contiene los datos enviados
     * @param response respuesta HTTP que se devolverá al navegador
     * @throws ServletException si el contenedor no puede procesar la petición
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
