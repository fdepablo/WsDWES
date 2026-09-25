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

    /**
     * Consulta el recurso de ejemplo. GET /recursos/15 devuelve 200;
     * GET /recursos/99 devuelve 404.
     *
     * @param request petición de la que se obtiene el identificador
     * @param response respuesta con el estado, las cabeceras y el texto
     * @throws IOException si falla la escritura de la respuesta
     */
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

    /**
     * Simula la creación de un recurso a partir de texto plano.
     * Por ejemplo, POST /recursos con el cuerpo "Nuevo recurso" devuelve 201
     * y la cabecera Location; el cuerpo "duplicado" devuelve 409.
     *
     * @param request petición con la ruta, el tipo de contenido y el cuerpo
     * @param response respuesta con el resultado de la operación
     * @throws IOException si falla la lectura o la escritura del cuerpo
     */
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

    /**
     * Simula la sustitución del recurso 15. Por ejemplo, PUT /recursos/15
     * con un cuerpo de texto no vacío devuelve 200.
     *
     * @param request petición con el identificador y el nuevo contenido
     * @param response respuesta con el estado y el texto resultante
     * @throws IOException si falla la lectura o la escritura del cuerpo
     */
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

    /**
     * Simula la eliminación y confirma el resultado con texto. Por ejemplo,
     * DELETE /recursos/15 devuelve 200; otro identificador devuelve 404.
     *
     * @param request petición de la que se obtiene el identificador
     * @param response respuesta con el estado y el mensaje de la operación
     * @throws IOException si falla la escritura de la respuesta
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!ID_EXISTENTE.equals(obtenerId(request))) {
            enviarTexto(response, HttpServletResponse.SC_NOT_FOUND,
                    "No se puede eliminar un recurso inexistente.");
            return;
        }

        enviarTexto(response, HttpServletResponse.SC_OK, "Recurso 15 eliminado.");
    }

    /**
     * Extrae la parte de la ruta posterior a /recursos. Por ejemplo,
     * /recursos/15 produce "15" y /recursos produce {@code null}.
     *
     * @param request petición cuya ruta adicional se consulta
     * @return identificador de la ruta o {@code null} si no hay ninguno
     */
    private String obtenerId(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        if (ruta == null || ruta.equals("/") || ruta.length() < 2) {
            return null;
        }
        return ruta.substring(1);
    }

    /**
     * Comprueba si la ruta apunta a la colección y no a un elemento.
     * Por ejemplo, /recursos y /recursos/ son válidas para POST.
     *
     * @param request petición cuya ruta adicional se consulta
     * @return {@code true} si no aparece un identificador en la ruta
     */
    private boolean esRutaDeColeccion(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        return ruta == null || ruta.equals("/");
    }

    /**
     * Comprueba el tipo de contenido declarado por el cliente.
     * Por ejemplo, acepta text/plain;charset=UTF-8 y rechaza application/json.
     *
     * @param request petición con la cabecera Content-Type
     * @return {@code true} si se declara texto plano
     */
    private boolean esTextoPlano(HttpServletRequest request) {
        String tipoContenido = request.getContentType();
        return tipoContenido != null && tipoContenido.toLowerCase().startsWith("text/plain");
    }

    /**
     * Lee el cuerpo como UTF-8 y conserva los saltos entre líneas.
     * Por ejemplo, un cuerpo con "uno" y "dos" en líneas separadas
     * produce un texto de dos líneas.
     *
     * @param request petición cuyo cuerpo se lee
     * @return contenido completo del cuerpo como texto
     * @throws IOException si falla la lectura del cuerpo
     */
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

    /**
     * Escribe una respuesta de texto plano en UTF-8 con el estado indicado.
     * Por ejemplo, estado 404 y contenido "No existe" producen una respuesta
     * 404 cuyo cuerpo contiene ese mensaje.
     *
     * @param response respuesta que recibirá el estado y el texto
     * @param estado código de estado HTTP, por ejemplo 404
     * @param contenido mensaje que se enviará en el cuerpo
     * @throws IOException si falla la escritura de la respuesta
     */
    private void enviarTexto(HttpServletResponse response, int estado, String contenido)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("text/plain;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println(contenido);
        }
    }
}
