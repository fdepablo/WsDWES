package modelo;

import java.util.List;

/**
 * Representa los datos no sensibles de un usuario registrado.
 *
 * <p>La contraseña no forma parte de este objeto porque este ejemplo no
 * implementa todavía almacenamiento seguro de credenciales.</p>
 */
public class Usuario {

    private final String nombre;
    private final String email;
    private final List<String> intereses;
    private final String tipoCuenta;

    /**
     * Reúne los datos aceptados del formulario y copia la lista de intereses.
     * Por ejemplo, un usuario puede tener nombre "Ana" e interés "programacion".
     *
     * @param nombre nombre mostrado en la confirmación
     * @param email correo usado para detectar registros duplicados
     * @param intereses opciones seleccionadas en el formulario
     * @param tipoCuenta tipo de cuenta seleccionado
     */
    public Usuario(String nombre, String email, List<String> intereses, String tipoCuenta) {
        this.nombre = nombre;
        this.email = email;
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
