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
        // Consultamos la sesión. Si no existe la sessión, le mandamos a login para que ponga
        // su usuario y su password y se valida.
        HttpSession session = request.getSession(false);
        // Si ya ha iniciado sesión, lo mandamos al inicio sin pedirle los datos otra vez.
        if (session != null && session.getAttribute("usuario") != null) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }
        // El forward muestra la JSP dentro de esta misma petición.
        request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Indicamos la codificación antes de leer los datos enviados por el formulario.
        request.setCharacterEncoding("UTF-8");
        Usuario usuario = gestor.autenticar(request.getParameter("nombre"), request.getParameter("password"));
        // Si los datos no son correctos, volvemos al formulario sin crear una sesión.
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // El mensaje solo se necesita en esta petición y llega a la JSP mediante forward.
            request.setAttribute("error", "Nombre o contraseña incorrectos.");
            request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
            return;
        }

        // Ahora sí creamos la sesión, porque el usuario se ha autenticado.
        HttpSession session = request.getSession();
        request.changeSessionId(); // Renueva el identificador tras iniciar sesión. Esto se hace por seguridad

        // Guardamos el usuario en sesión para reconocerlo en las siguientes peticiones.
        session.setAttribute("usuario", usuario);
        System.out.println(usuario.getNombre());
        System.out.println(session.getAttribute("usuario"));
        System.out.println(((Usuario)session.getAttribute("usuario")).getNombre());
        session.setAttribute("prueba", 5);
        // El redirect provoca un GET a /inicio: actualizar la página (F5) no vuelve a enviar el login.
        response.sendRedirect(request.getContextPath() + "/inicio");
    }
}
