package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import servicios.GestorUsuarios;

import java.io.IOException;

/** Protege el listado y lo entrega a la vista como atributo de petición. */
@WebServlet("/usuarios")
public class ListadoUsuariosServlet extends HttpServlet {
    private final GestorUsuarios gestor = new GestorUsuarios();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        request.setAttribute("usuarios", gestor.listar());
        request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request, response);
    }
}
