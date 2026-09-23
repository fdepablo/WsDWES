package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Devuelve una redirección para poder observar el estado y la cabecera Location. */
@WebServlet("/redirigir")
public class RedireccionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + "/recursos/15");
    }
}
