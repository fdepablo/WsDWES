package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Usuario;
import servicios.GestorUsuarios;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Procesa y valida el formulario de registro. */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    /**
     * Conserva los registros entre las peticiones atendidas por este servlet.
     * Tomcat reutiliza la instancia del servlet: este gestor se crea con ella,
     * no cada vez que se ejecuta doPost. Si se creara dentro de doPost, cada
     * petición empezaría con una lista vacía, no detectaría correos anteriores
     * y contaría solo su propio registro.
     *
     * <p>Varios hilos pueden usar este mismo gestor simultáneamente; por eso
     * sus métodos son synchronized. La referencia final no cambia, pero la
     * lista del gestor sí puede modificarse: final no garantiza sincronización.
     * Cada envío crea un Usuario que el gestor valida.</p>
     */
    private final GestorUsuarios gestorUsuarios = new GestorUsuarios();

    /**
     * Lee el formulario y traduce el resultado del gestor a HTTP.
     *
     * <p>La codificación debe establecerse antes de leer cualquier parámetro.
     * {@code getParameterValues} se utiliza para las casillas porque varias
     * comparten el mismo nombre.</p>
     *
     * <p>Por ejemplo, un POST válido muestra la confirmación; un correo ya
     * registrado devuelve 409 y muestra la vista de error.</p>
     *
     * @param request petición con los campos del formulario
     * @param response respuesta con la vista y el estado adecuados
     * @throws ServletException si falla el forward a una vista
     * @throws IOException si falla la lectura o escritura de la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String nombre = limpiar(request.getParameter("nombre"));
        String email = limpiar(request.getParameter("email"));
        String password = request.getParameter("password");
        String tipoCuenta = limpiar(request.getParameter("tipoCuenta"));
        String[] interesesRecibidos = request.getParameterValues("intereses");

        // Si no se marca ninguna casilla, getParameterValues devuelve null y usamos
        // una lista vacía. Si se marca alguna, convertimos el array recibido en una lista.
        List<String> intereses = interesesRecibidos == null
                ? List.of()
                : Arrays.asList(interesesRecibidos);

        // No quitamos espacios de la contraseña: forman parte del valor recibido.
        Usuario usuario = new Usuario(nombre, email, password, intereses, tipoCuenta);
        int resultado = gestorUsuarios.registrar(usuario);
        switch (resultado) {
            case GestorUsuarios.REGISTRO_CORRECTO:
                break;
            case GestorUsuarios.ERROR_EMAIL:
                mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debes indicar un correo electrónico válido.");
                return;
            case GestorUsuarios.ERROR_PASSWORD:
                mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "La contraseña debe tener entre 8 y 72 caracteres.");
                return;
            case GestorUsuarios.ERROR_INTERESES:
                mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "Se ha recibido un interés no permitido.");
                return;
            case GestorUsuarios.ERROR_TIPO_CUENTA:
                mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debes seleccionar un tipo de cuenta válido.");
                return;
            case GestorUsuarios.ERROR_NOMBRE:
                mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "El nombre es obligatorio y no puede superar los 60 caracteres.");
                return;
            case GestorUsuarios.EMAIL_DUPLICADO:
                mostrarError(request, response, HttpServletResponse.SC_CONFLICT,
                        "Ya existe un usuario con ese correo electrónico.");
                return;
            default:
                mostrarError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "No se ha podido completar el registro.");
                return;
        }

        request.setAttribute("usuario", usuario);
        request.setAttribute("totalUsuarios", gestorUsuarios.contarUsuarios());
        mostrarVista(request, response, "/WEB-INF/vistas/registro.jsp");
    }

    /**
     * Quita espacios en los extremos y sustituye un valor ausente por vacío.
     * Por ejemplo, {@code limpiar(" Ana ")} devuelve "Ana".
     *
     * @param valor parámetro recibido, que puede ser {@code null}
     * @return texto sin espacios exteriores o cadena vacía
     */
    private String limpiar(String valor) {
        return valor == null ? "" : valor.strip();
    }

    /**
     * Envía un estado de error y muestra su mensaje en la JSP correspondiente.
     * Por ejemplo, un correo duplicado muestra el mensaje con estado 409.
     *
     * @param request petición donde se guarda el mensaje para la vista
     * @param response respuesta cuyo estado se establece
     * @param estado código HTTP de error, por ejemplo 409
     * @param mensaje explicación visible para el usuario
     * @throws ServletException si falla el forward a la vista
     * @throws IOException si falla la respuesta
     */
    private void mostrarError(HttpServletRequest request, HttpServletResponse response,
                              int estado, String mensaje) throws ServletException, IOException {
        response.setStatus(estado);
        request.setAttribute("mensajeError", mensaje);
        mostrarVista(request, response, "/WEB-INF/vistas/error.jsp");
    }

    /**
     * Delega la presentación en una JSP conservando la misma petición.
     * Por ejemplo, la ruta /WEB-INF/vistas/registro.jsp muestra
     * el atributo "usuario" guardado en la petición.
     *
     * @param request petición con los atributos para la vista
     * @param response respuesta que completará la JSP
     * @param ruta ubicación interna de la JSP
     * @throws ServletException si falla el forward
     * @throws IOException si falla la respuesta
     */
    private void mostrarVista(HttpServletRequest request, HttpServletResponse response,
                              String ruta) throws ServletException, IOException {
        request.getRequestDispatcher(ruta).forward(request, response);
    }
}
