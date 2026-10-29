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
@WebServlet("/UsuariosServlet")
public class UsuariosServlet extends HttpServlet {
    private final GestorUsuarios gestor = new GestorUsuarios();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Comprobamos la sesión existente sin crear una nueva.
        HttpSession session = request.getSession(false);
        // Sin un usuario autenticado, mandamos al login y no mostramos el listado.
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        // La lista solo se necesita para esta página: la guardamos en la petición, no en sesión.
        request.setAttribute("usuarios", gestor.listar());
        // El forward permite que la JSP reciba la lista en la misma petición.
        request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request, response);
    }
}
