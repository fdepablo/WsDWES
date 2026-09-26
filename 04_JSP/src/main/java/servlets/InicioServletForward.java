package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Curso;
import utils.NumeroAleatorio;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Prepara los datos dinámicos y los comparte con una JSP mediante forward. */
@WebServlet("/inicio-forward")
public class InicioServletForward extends HttpServlet {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Prepara los atributos y abre la vista JSP mediante un forward.
     * Por ejemplo, GET /inicio-forward muestra el título y un número entre 1 y 100.
     *
     * @param request petición donde se guardan los datos para la vista
     * @param response respuesta que completará la JSP
     * @throws ServletException si falla el forward a la vista
     * @throws IOException si falla la lectura o escritura de la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Curso curso = new Curso("Desarrollo Web en Entorno Servidor", "DAW");

        request.setAttribute("titulo", "Introducción a JSP");
        request.setAttribute("fechaServidor", LocalDateTime.now().format(FORMATO_FECHA));
        request.setAttribute("numeroAleatorio", NumeroAleatorio.generar(1, 100));
        request.setAttribute("curso", curso);

        request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp")
                .forward(request, response);
    }
}
