package servicios;

import modelo.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Mantiene los usuarios registrados mientras la aplicación está en ejecución.
 *
 * <p>Es un almacenamiento deliberadamente sencillo: al reiniciar Tomcat se
 * pierden los datos. Los métodos que acceden a la lista están sincronizados
 * porque un mismo servlet puede atender varias peticiones simultáneamente.</p>
 *
 * <p>{@code synchronized} en un método de instancia utiliza el monitor
 * (bloqueo) de este objeto: solo un hilo puede ejecutar uno de sus métodos
 * sincronizados a la vez. Los demás esperan hasta que se libere el bloqueo
 * al salir del método, incluso si se produce una excepción. Esto protege
 * la lista compartida por las peticiones que usan este mismo gestor.</p>
 */
public class GestorUsuarios {

    // Códigos propios del registro; el servlet decide la respuesta HTTP.
    public static final int REGISTRO_CORRECTO = 0;
    public static final int ERROR_EMAIL = 1;
    public static final int ERROR_INTERESES = 2;
    public static final int ERROR_TIPO_CUENTA = 3;
    public static final int ERROR_NOMBRE = 4;
    public static final int EMAIL_DUPLICADO = 5;
    public static final int ERROR_PASSWORD = 6;

    // Una expresión regular (regex) describe un patrón que debe cumplir un texto.
    // Pattern.compile prepara ese patrón y matcher(...).matches() comprueba el correo.
    // Este patrón exige primero un texto sin espacios ni @, luego una @ y
    // a continuación dos partes separadas por un punto (por ejemplo, ana@ejemplo.com).
    // Es una comprobación básica de formato: no verifica que la dirección exista.
    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> INTERESES_PERMITIDOS =
            Set.of("programacion", "bases-datos", "diseno-web");
    private static final Set<String> TIPOS_CUENTA_PERMITIDOS =
            Set.of("estudiante", "docente");

    private final List<Usuario> usuarios = new ArrayList<>();

    /**
     * Valida los datos y registra un usuario si su correo no está registrado.
     * Por ejemplo, registrar dos usuarios con "ana@ejemplo.com" acepta
     * el primero y rechaza el segundo, aunque cambien las mayúsculas.
     *
     * <p>Este método es el punto de entrada a la capa de lógica de negocio.
     * Su contrato permite a la capa web interpretar el resultado sin conocer
     * cómo se valida o almacena el usuario. Los códigos son propios del gestor:
     * el servlet decide el mensaje, el estado HTTP y la vista que corresponde.</p>
     *
     * <p>Resultados posibles:</p>
     * <ul>
     *   <li>{@code 0} — {@link #REGISTRO_CORRECTO}: el usuario se ha añadido.</li>
     *   <li>{@code 1} — {@link #ERROR_EMAIL}: el correo es nulo, supera los
     *       100 caracteres o no cumple el formato básico de correo electrónico
     *       (incluido el correo vacío).</li>
     *   <li>{@code 2} — {@link #ERROR_INTERESES}: algún interés no pertenece a
     *       {@code programacion}, {@code bases-datos} o {@code diseno-web}.
     *       Una lista vacía es válida.</li>
     *   <li>{@code 3} — {@link #ERROR_TIPO_CUENTA}: el tipo de cuenta es nulo
     *       o no es {@code estudiante} ni {@code docente}.</li>
     *   <li>{@code 4} — {@link #ERROR_NOMBRE}: el nombre es nulo, está vacío,
     *       contiene solo espacios en blanco o supera los 60 caracteres.</li>
     *   <li>{@code 5} — {@link #EMAIL_DUPLICADO}: ya existe un usuario con
     *       ese correo, sin distinguir mayúsculas de minúsculas.</li>
     *   <li>{@code 6} — {@link #ERROR_PASSWORD}: la contraseña es nula
     *       o su longitud no está entre 8 y 72 caracteres, ambos incluidos.</li>
     * </ul>
     *
     * <p>Si hay varios errores, devuelve solo el primero según este orden:
     * nombre, correo, contraseña, intereses, tipo de cuenta y correo duplicado.
     * Cualquier código de error deja la lista sin cambios. Este ejemplo conserva
     * la contraseña en memoria en texto plano; en una aplicación real se
     * almacenaría en la base de datos un hash con sal, nunca el texto original.</p>
     *
     * <p>{@code synchronized} mantiene juntas la comprobación del correo y
     * la inserción: otra petición no puede comprobar el mismo correo entre
     * ambas operaciones y registrarlo también.</p>
     *
     * @param usuario usuario no nulo que se desea registrar
     * @return {@link #REGISTRO_CORRECTO} si se añadió; en otro caso, el código
     *         del primer error encontrado, descrito en los resultados posibles,
     *         sin modificar la lista
     * @throws NullPointerException si {@code usuario} es {@code null}; esta
     *         llamada incumple el contrato y no devuelve un código de error
     */
    public synchronized int registrar(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getNombre().length() > 60) {
            return ERROR_NOMBRE;
        }
        if (usuario.getEmail() == null || usuario.getEmail().length() > 100
                || !FORMATO_EMAIL.matcher(usuario.getEmail()).matches()) {
            return ERROR_EMAIL;
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 8
                || usuario.getPassword().length() > 72) {
            return ERROR_PASSWORD;
        }
        if (!INTERESES_PERMITIDOS.containsAll(usuario.getIntereses())) {
            return ERROR_INTERESES;
        }
        if (usuario.getTipoCuenta() == null
                || !TIPOS_CUENTA_PERMITIDOS.contains(usuario.getTipoCuenta())) {
            return ERROR_TIPO_CUENTA;
        }

        for (Usuario usuarioRegistrado : usuarios) {
            if (usuarioRegistrado.getEmail().equalsIgnoreCase(usuario.getEmail())) {
                return EMAIL_DUPLICADO;
            }
        }

        usuarios.add(usuario);
        return REGISTRO_CORRECTO;
    }

    /**
     * Cuenta los registros conservados durante esta ejecución de Tomcat.
     * Por ejemplo, tras un registro válido devuelve 1.
     *
     * <p>Usa el mismo bloqueo que {@link #registrar(Usuario)} para consultar
     * la lista sin que otra petición la modifique durante la lectura.</p>
     *
     * @return número de usuarios registrados
     */
    public synchronized int contarUsuarios() {
        return usuarios.size();
    }
}
