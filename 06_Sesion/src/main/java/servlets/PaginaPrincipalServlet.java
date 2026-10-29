package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Comprueba la sesión antes de mostrar la página privada. */
@WebServlet("/PaginaPrincipalServlet")
public class PaginaPrincipalServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Comprobamos que existe la sesión, si no existe NO la creamos
        HttpSession session = request.getSession(false);
        //Si no existe, mandamos al LoginServlet
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/paginaPrincipal.jsp").forward(request, response);
    }
}
