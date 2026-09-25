package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Devuelve una redirección para poder observar el estado y la cabecera Location. */
@WebServlet("/redirigir")
public class RedireccionServlet extends HttpServlet {

    /**
     * Responde a GET con 307 y señala la ubicación temporal del recurso.
     * Por ejemplo, GET /redirigir devuelve Location: /recursos/15
     * si la aplicación está desplegada en el contexto raíz.
     *
     * @param request petición utilizada para conocer la ruta de contexto
     * @param response respuesta en la que se escriben el estado y la cabecera
     * @throws IOException si el contenedor no puede procesar la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        response.setHeader("Location", request.getContextPath() + "/recursos/15");
    }
}
