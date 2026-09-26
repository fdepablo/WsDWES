package modelo;

/** Datos públicos de un usuario; la contraseña nunca se guarda aquí ni en la sesión. */
public class Usuario {
    private final int id;
    private final String nombre;

    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
