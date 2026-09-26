package modelo.servicio;

import modelo.entidad.Producto;
import modelo.persistencia.ConexionBD;
import modelo.persistencia.ProductoDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio del catálogo. El controlador llama a este servicio;
 * solo el servicio conoce el DAO y la configuración de la base de datos.
 */
public class ProductoServicio {
    private final ProductoDAO productos;

    /** Prepara la base configurada y crea el DAO que atenderá las operaciones. */
    public ProductoServicio() throws SQLException {
        ConexionBD baseDatos = new ConexionBD();
        baseDatos.prepararBaseDatos();
        productos = new ProductoDAO(baseDatos);
    }

    /** Devuelve todos los productos para el listado. */
    public List<Producto> listar() throws SQLException {
        return productos.listar();
    }

    /** Devuelve el producto solicitado o null cuando no existe. */
    public Producto buscar(long id) throws SQLException {
        return productos.buscar(id);
    }

    /**
     * Valida los datos y registra un producto nuevo.
     *
     * @throws IllegalArgumentException si el nombre o el precio no son válidos
     * @throws SQLException si falla la inserción
     */
    public void crear(String nombre, String textoPrecio) throws SQLException {
        Producto producto = validar(0, nombre, textoPrecio);
        productos.insertar(producto.getNombre(), producto.getPrecio());
    }

    /**
     * Valida los datos y modifica un producto existente.
     *
     * @return false si ya no existe una fila con ese ID
     * @throws IllegalArgumentException si el nombre o el precio no son válidos
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(long id, String nombre, String textoPrecio) throws SQLException {
        Producto producto = validar(id, nombre, textoPrecio);
        return productos.actualizar(id, producto.getNombre(), producto.getPrecio());
    }

    /** Devuelve false si el producto que se quiso borrar ya no existe. */
    public boolean eliminar(long id) throws SQLException {
        return productos.eliminar(id);
    }

    /**
     * Aplica las reglas del catálogo antes de cualquier escritura. El formulario
     * HTML ayuda al usuario, pero esta validación también cubre peticiones manipuladas.
     */
    private Producto validar(long id, String nombre, String textoPrecio) {
        String nombreLimpio = nombre == null ? "" : nombre.strip();
        BigDecimal precio = null;
        try {
            if (textoPrecio != null) precio = new BigDecimal(textoPrecio);
        } catch (NumberFormatException ignored) {
            // La regla común de abajo comunica el error al controlador.
        }
        if (nombreLimpio.isBlank() || nombreLimpio.length() > 80 || precio == null
                || precio.signum() < 0 || precio.scale() > 2
                || precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Indica un nombre de hasta 80 caracteres y un precio entre 0 y 99999999,99 con hasta dos decimales.");
        }
        return new Producto(id, nombreLimpio, precio);
    }
}
