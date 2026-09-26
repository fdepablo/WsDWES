package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Muestra que una redirección crea otra petición y pierde sus atributos. */
@WebServlet("/inicio-redirect")
public class InicioServletRedirect extends HttpServlet {

    /**
     * Guarda un atributo y redirige a una JSP pública para comprobar que se pierde.
     * Por ejemplo, GET /inicio-redirect termina en /resultado-redirect.jsp,
     * donde el atributo "mensaje" ya no está disponible.
     *
     * @param request petición inicial donde se guarda el atributo
     * @param response respuesta que indica al navegador la nueva URL
     * @throws IOException si falla el envío de la redirección
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setAttribute("mensaje", "Dato creado en InicioServletRedirect");
        response.sendRedirect(request.getContextPath() + "/resultado-redirect.jsp");
    }
}
