package bbdd.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

/** Contrato común para usar H2 o MariaDB sin cambiar el código de ProductoDAO. */
public interface ConexionBD {
    /**
     * Abre una conexión nueva. Quien la recibe debe cerrarla con try-with-resources.
     * @return conexión JDBC con la base de datos de la implementación elegida
     * @throws SQLException si no se puede cargar el controlador o conectar
     */
    Connection abrir() throws SQLException;

    /**
     * Prepara la base para el ejemplo: H2 crea la tabla; MariaDB comprueba el acceso.
     * En MariaDB hay que ejecutar previamente el script sql/mariadb.sql.
     * @throws SQLException si falla la preparación o la conexión
     */
    void prepararBaseDatos() throws SQLException;
}
