package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Muestra el formulario de entrada de la aplicación. */
@WebServlet("/formulario")
public class FormularioServlet extends HttpServlet {

    /**
     * Muestra el formulario mediante una vista JSP.
     * La ruta /formulario es relativa al contexto de la aplicación.
     *
     * @param request petición que se pasa a la vista
     * @param response respuesta que completará la JSP
     * @throws ServletException si falla el forward a la vista
     * @throws IOException si falla la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/formulario.jsp")
                .forward(request, response);
    }
}
