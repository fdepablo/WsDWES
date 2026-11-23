package bbdd;

import bbdd.interfaces.ConexionBD;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexión al servidor MariaDB de XAMPP. La base y la tabla se crean con el script SQL. */
public class ConexionBDMySQL implements ConexionBD {
    // Valores de demostración para XAMPP local; adapta estos datos a tu instalación.
    private static final String URL = "jdbc:mysql://localhost:3307/dwes_07";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "root";

    @Override
    public Connection abrir() throws SQLException {
        try {
            // Carga explícita del controlador que Maven incluye en WEB-INF/lib.
            Class.forName("org.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encuentra el controlador JDBC de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    /** Comprueba la conexión al arrancar; no crea ni modifica tablas en MariaDB. */
    @Override
    public void prepararBaseDatos() throws SQLException {
        try (Connection conexion = abrir()) {
            if (!conexion.isValid(5)) {
                throw new SQLException("La conexión con MySQL no es válida.");
            }
        }
    }
}
