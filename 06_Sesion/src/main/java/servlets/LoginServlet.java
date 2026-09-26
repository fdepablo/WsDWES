package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Usuario;
import servicios.GestorUsuarios;

import java.io.IOException;

/** Crea la sesión únicamente cuando las credenciales son correctas. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final GestorUsuarios gestor = new GestorUsuarios();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Usuario usuario = gestor.autenticar(request.getParameter("nombre"), request.getParameter("password"));
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("error", "Nombre o contraseña incorrectos.");
            request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        request.changeSessionId(); // Renueva el identificador tras iniciar sesión.
        session.setAttribute("usuario", usuario);
        response.sendRedirect(request.getContextPath() + "/inicio");
    }
}
