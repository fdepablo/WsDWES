package servicios;

import modelo.Usuario;

import java.util.List;

/** Usuarios fijos para observar el funcionamiento de la sesión, sin base de datos. */
public class GestorUsuarios {
    private static final List<Usuario> USUARIOS = List.of(
            new Usuario(1, "felix"),
            new Usuario(2, "marta")
    );

    /** Devuelve el usuario autenticado o null si las credenciales no coinciden. */
    public Usuario autenticar(String nombre, String password) {
        // Solo para esta práctica: una aplicación real usaría contraseñas con hash y almacenamiento seguro.
        if ("felix".equals(nombre) && "1234".equals(password)) {
            return USUARIOS.get(0);
        }
        if ("marta".equals(nombre) && "4321".equals(password)) {
            return USUARIOS.get(1);
        }
        return null;
    }

    public List<Usuario> listar() {
        return USUARIOS;
    }
}
