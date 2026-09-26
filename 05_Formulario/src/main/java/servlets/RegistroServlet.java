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
import java.util.Set;
import java.util.regex.Pattern;

/** Procesa y valida el formulario de registro. */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> INTERESES_PERMITIDOS =
            Set.of("programacion", "bases-datos", "diseno-web");
    private static final Set<String> TIPOS_CUENTA_PERMITIDOS =
            Set.of("estudiante", "docente");

    private final GestorUsuarios gestorUsuarios = new GestorUsuarios();

    /**
     * Muestra el formulario mediante una vista JSP.
     * Por ejemplo, GET /registro abre formulario.jsp.
     *
     * @param request petición que se pasa a la vista
     * @param response respuesta que completará la JSP
     * @throws ServletException si falla el forward a la vista
     * @throws IOException si falla la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        mostrarVista(request, response, "/WEB-INF/vistas/formulario.jsp");
    }

    /**
     * Lee los controles del formulario, valida sus valores y registra al usuario.
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

        String error = validar(nombre, email, password, intereses, tipoCuenta);
        if (error != null) {
            mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }

        // La contraseña se valida, pero no se registra, almacena ni muestra.
        Usuario usuario = new Usuario(nombre, email, intereses, tipoCuenta);
        if (!gestorUsuarios.registrar(usuario)) {
            mostrarError(request, response, HttpServletResponse.SC_CONFLICT,
                    "Ya existe un usuario con ese correo electrónico.");
            return;
        }

        request.setAttribute("usuario", usuario);
        request.setAttribute("totalUsuarios", gestorUsuarios.contarUsuarios());
        mostrarVista(request, response, "/WEB-INF/vistas/confirmacion.jsp");
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
     * Comprueba los campos antes de crear el usuario.
     * Por ejemplo, un nombre vacío produce un mensaje de error; los datos
     * válidos producen {@code null}.
     *
     * @param nombre nombre ya limpiado
     * @param email correo ya limpiado
     * @param password contraseña recibida, que no se almacenará
     * @param intereses opciones recibidas del formulario
     * @param tipoCuenta tipo de cuenta ya limpiado
     * @return primer mensaje de error o {@code null} si todo es válido
     */
    private String validar(String nombre, String email, String password,
                           List<String> intereses, String tipoCuenta) {
        if (nombre.isBlank() || nombre.length() > 60) {
            return "El nombre es obligatorio y no puede superar los 60 caracteres.";
        }
        if (email.isBlank() || email.length() > 100 || !FORMATO_EMAIL.matcher(email).matches()) {
            return "Debes indicar un correo electrónico válido.";
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            return "La contraseña debe tener entre 8 y 72 caracteres.";
        }
        if (!INTERESES_PERMITIDOS.containsAll(intereses)) {
            return "Se ha recibido un interés no permitido.";
        }
        if (!TIPOS_CUENTA_PERMITIDOS.contains(tipoCuenta)) {
            return "Debes seleccionar un tipo de cuenta válido.";
        }
        return null;
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
     * Por ejemplo, la ruta /WEB-INF/vistas/confirmacion.jsp muestra
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
