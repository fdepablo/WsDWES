package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simula operaciones sobre un recurso para observar mensajes HTTP.
 *
 * <p>No utiliza una base de datos: el objetivo es comparar métodos, cuerpos,
 * cabeceras y códigos de estado, no construir todavía una API REST.</p>
 */
@WebServlet("/recursos/*")
public class RecursoServlet extends HttpServlet {

    private static final String ID_EXISTENTE = "15";

    /** Consulta el recurso de ejemplo. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String id = obtenerId(request);
        if (!ID_EXISTENTE.equals(id)) {
            enviarTexto(response, HttpServletResponse.SC_NOT_FOUND,
                    "No existe un recurso con el identificador solicitado.");
            return;
        }

        response.setHeader("X-Ejemplo", "consulta-http");
        enviarTexto(response, HttpServletResponse.SC_OK,
                "Recurso 15: ejemplo para estudiar HTTP.");
    }

    /** Simula la creación de un recurso a partir de un cuerpo de texto. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!esRutaDeColeccion(request)) {
            enviarTexto(response, HttpServletResponse.SC_BAD_REQUEST,
                    "POST debe enviarse a /recursos, sin identificador.");
            return;
        }
        if (!esTextoPlano(request)) {
            enviarTexto(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "El cuerpo debe enviarse como text/plain.");
            return;
        }

        String contenido = leerCuerpo(request);
        if (contenido.isBlank()) {
            enviarTexto(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El cuerpo de la petición no puede estar vacío.");
            return;
        }
        if (contenido.equalsIgnoreCase("duplicado")) {
            enviarTexto(response, HttpServletResponse.SC_CONFLICT,
                    "El recurso ya existe.");
            return;
        }

        response.setHeader("Location", request.getContextPath() + "/recursos/15");
        enviarTexto(response, HttpServletResponse.SC_CREATED,
                "Recurso creado con el contenido: " + contenido);
    }

    /** Simula la sustitución del recurso identificado por el número 15. */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!ID_EXISTENTE.equals(obtenerId(request))) {
            enviarTexto(response, HttpServletResponse.SC_NOT_FOUND,
                    "No se puede actualizar un recurso inexistente.");
            return;
        }
        if (!esTextoPlano(request)) {
            enviarTexto(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "El cuerpo debe enviarse como text/plain.");
            return;
        }

        String contenido = leerCuerpo(request);
        if (contenido.isBlank()) {
            enviarTexto(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El cuerpo de la petición no puede estar vacío.");
            return;
        }

        enviarTexto(response, HttpServletResponse.SC_OK,
                "Recurso 15 actualizado con el contenido: " + contenido);
    }

    /** Simula la eliminación del recurso sin devolver un cuerpo. */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!ID_EXISTENTE.equals(obtenerId(request))) {
            enviarTexto(response, HttpServletResponse.SC_NOT_FOUND,
                    "No se puede eliminar un recurso inexistente.");
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private String obtenerId(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        if (ruta == null || ruta.equals("/") || ruta.length() < 2) {
            return null;
        }
        return ruta.substring(1);
    }

    private boolean esRutaDeColeccion(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        return ruta == null || ruta.equals("/");
    }

    private boolean esTextoPlano(HttpServletRequest request) {
        String tipoContenido = request.getContentType();
        return tipoContenido != null && tipoContenido.toLowerCase().startsWith("text/plain");
    }

    private String leerCuerpo(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder cuerpo = new StringBuilder();

        try (BufferedReader lector = request.getReader()) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!cuerpo.isEmpty()) {
                    cuerpo.append(System.lineSeparator());
                }
                cuerpo.append(linea);
            }
        }
        return cuerpo.toString();
    }

    private void enviarTexto(HttpServletResponse response, int estado, String contenido)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("text/plain;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println(contenido);
        }
    }
}
