package bbdd;

import modelo.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Agrupa las cuatro operaciones SQL del ejemplo. Cada método cierra sus recursos JDBC. */
public class ProductoDAO {
    private final ConexionBD baseDatos;

    /** Recibe la configuración de conexión que utilizará cada operación. */
    public ProductoDAO(ConexionBD baseDatos) {
        this.baseDatos = baseDatos;
    }

    /**
     * Recorre el ResultSet y convierte cada fila en un Producto.
     *
     * @return productos ordenados por ID; lista vacía si no hay filas
     * @throws SQLException si falla la consulta
     */
    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY id";
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                productos.add(leerProducto(filas));
            }
        }
        return productos;
    }

    /**
     * Usa un parámetro SQL para buscar por ID sin concatenar datos en la consulta.
     *
     * @return producto encontrado o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Producto buscar(long id) throws SQLException {
        String sql = "SELECT id, nombre, precio FROM productos WHERE id = ?";
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next() ? leerProducto(filas) : null;
            }
        }
    }

    /**
     * Inserta un producto; la base de datos genera el ID.
     *
     * @throws SQLException si falla la inserción
     */
    public void insertar(String nombre, BigDecimal precio) throws SQLException {
        String sql = "INSERT INTO productos (nombre, precio) VALUES (?, ?)";
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            sentencia.setBigDecimal(2, precio);
            sentencia.executeUpdate();
        }
    }

    /**
     * Actualiza la fila indicada y comprueba cuántas filas cambió el SQL.
     *
     * @return true si existía una fila con ese ID
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(long id, String nombre, BigDecimal precio) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, precio = ? WHERE id = ?";
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            sentencia.setBigDecimal(2, precio);
            sentencia.setLong(3, id);
            return sentencia.executeUpdate() == 1;
        }
    }

    /**
     * Borra una fila por ID.
     *
     * @return true si se eliminó una fila; false si el ID no existía
     * @throws SQLException si falla el borrado
     */
    public boolean eliminar(long id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        }
    }

    /** Lee las columnas de la fila actual del ResultSet. */
    private Producto leerProducto(ResultSet filas) throws SQLException {
        return new Producto(filas.getLong("id"), filas.getString("nombre"), filas.getBigDecimal("precio"));
    }
}
