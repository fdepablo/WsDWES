package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.UtilsHTML;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Muestra qué parámetros han llegado en una segunda petición HTTP.
 */
@WebServlet("/segundo-servlet")
public class SegundoServlet extends HttpServlet {

    /**
     * Comprueba que una petición nueva no conserva automáticamente los
     * parámetros de la petición anterior.
     *
     * <p>Este método responde a {@code GET}. Los parámetros llegan en la URL y
     * la operación se limita a consultar y mostrar información, sin modificar
     * el estado del servidor.</p>
     *
     * @param request nueva petición creada al enviar el segundo formulario
     * @param response respuesta que se enviará al navegador
     * @throws ServletException si el contenedor no puede procesar la petición
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String nombre = request.getParameter("nombrePersona");
        String edad = request.getParameter("edadPersona");
        String urlInicio = request.getContextPath() + "/";

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Segundo servlet</title>
                    </head>
                    <body>
                    <main>
                        <h1>Una nueva petición HTTP</h1>
                    """);
            out.printf("<p><strong>Edad propagada:</strong> %s</p>%n", UtilsHTML.escapar(edad));

            if (nombre == null) {
                out.println("<p><strong>Nombre:</strong> no está disponible porque el segundo "
                        + "formulario no envió el parámetro <code>nombrePersona</code>.</p>");
            } else {
                out.printf("<p><strong>Nombre:</strong> %s</p>%n", UtilsHTML.escapar(nombre));
            }

            out.println("<p>Cada envío del navegador crea una petición diferente. "
                    + "Los parámetros solo llegan si el cliente los incluye de nuevo.</p>");
            out.printf("<p><a href=\"%s\">Volver al formulario inicial</a></p>%n", urlInicio);
            out.println("""
                    </main>
                    </body>
                    </html>
                    """);
        }
    }

    /**
     * Atiende una petición {@code POST} y delega en {@link #doGet} porque este
     * ejemplo procesa los mismos parámetros y genera la misma respuesta con
     * ambos métodos.
     *
     * <p>A diferencia de {@code GET}, {@code POST} envía normalmente los datos
     * en el cuerpo de la petición. En aplicaciones reales se suele reservar
     * para operaciones que crean o modifican información, por lo que no debe
     * delegarse automáticamente en {@code doGet} si las acciones son distintas.</p>
     *
     * @param request petición HTTP con los datos enviados en el cuerpo
     * @param response respuesta que se enviará al navegador
     * @throws ServletException si el contenedor no puede procesar la petición
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
