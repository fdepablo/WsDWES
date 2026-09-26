package servicios;

import modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene los usuarios registrados mientras la aplicación está en ejecución.
 *
 * <p>Es un almacenamiento deliberadamente sencillo: al reiniciar Tomcat se
 * pierden los datos. Los métodos que acceden a la lista están sincronizados
 * porque un mismo servlet puede atender varias peticiones simultáneamente.</p>
 */
public class GestorUsuarios {

    private final List<Usuario> usuarios = new ArrayList<>();

    /**
     * Registra un usuario si no existe otro con el mismo correo electrónico.
     * Por ejemplo, registrar dos usuarios con "ana@ejemplo.com" acepta
     * el primero y rechaza el segundo, aunque cambien las mayúsculas.
     *
     * @param usuario usuario que se desea registrar
     * @return {@code true} si se registró; {@code false} si el correo ya existía
     */
    public synchronized boolean registrar(Usuario usuario) {
        for (Usuario usuarioRegistrado : usuarios) {
            if (usuarioRegistrado.getEmail().equalsIgnoreCase(usuario.getEmail())) {
                return false;
            }
        }

        usuarios.add(usuario);
        return true;
    }

    /**
     * Cuenta los registros conservados durante esta ejecución de Tomcat.
     * Por ejemplo, tras un registro válido devuelve 1.
     *
     * @return número de usuarios registrados
     */
    public synchronized int contarUsuarios() {
        return usuarios.size();
    }
}
