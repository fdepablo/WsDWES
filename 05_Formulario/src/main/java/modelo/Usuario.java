package modelo;

import java.util.List;

/**
 * Representa los datos de un usuario registrado.
 *
 * <p>Por simplificación didáctica, conserva la contraseña en texto plano
 * en memoria. En una aplicación real, la base de datos debe guardar un hash
 * generado con un algoritmo específico para contraseñas y una sal aleatoria,
 * nunca la contraseña original. La contraseña no debe mostrarse ni escribirse
 * en logs.</p>
 */
public class Usuario {

    private final String nombre;
    private final String email;
    private final String password;
    private final List<String> intereses;
    private final String tipoCuenta;

    /**
     * Reúne los datos recibidos y copia la lista de intereses.
     * El gestor valida estos datos antes de incorporar el usuario al registro.
     * Por ejemplo, un usuario puede tener nombre "Ana" e interés "programacion".
     *
     * @param nombre nombre mostrado en la confirmación
     * @param email correo usado para detectar registros duplicados
     * @param password contraseña en texto plano, solo para esta simplificación didáctica
     * @param intereses opciones seleccionadas en el formulario
     * @param tipoCuenta tipo de cuenta seleccionado
     */
    public Usuario(String nombre, String email, String password, List<String> intereses, String tipoCuenta) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.intereses = List.copyOf(intereses);
        this.tipoCuenta = tipoCuenta;
    }

    /**
     * Devuelve el nombre para la vista; por ejemplo, "Ana".
     *
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el correo; por ejemplo, "ana@ejemplo.com".
     *
     * @return correo del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Permite al gestor validar la contraseña de este ejemplo.
     * No debe utilizarse para mostrarla en las vistas ni escribirla en logs.
     *
     * @return contraseña en texto plano conservada por simplificación didáctica
     */
    public String getPassword() {
        return password;
    }

    /**
     * Devuelve los intereses seleccionados; por ejemplo, ["programacion"].
     *
     * @return lista de intereses que no se puede modificar
     */
    public List<String> getIntereses() {
        return intereses;
    }

    /**
     * Devuelve el tipo de cuenta; por ejemplo, "estudiante".
     *
     * @return tipo de cuenta del usuario
     */
    public String getTipoCuenta() {
        return tipoCuenta;
    }
}
