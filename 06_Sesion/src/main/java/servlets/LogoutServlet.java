package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Invalida la sesión actual sin crear otra. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Consultamos sin crear sesión: puede que ya haya caducado o se haya cerrado.
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidar la sesión elimina todos sus atributos, incluido el usuario.
            session.invalidate();
        }
        // Volvemos al formulario con una nueva petición GET.
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
