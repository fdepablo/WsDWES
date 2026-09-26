package modelo.entidad;

import java.math.BigDecimal;

/** Una fila de la tabla productos. */
public class Producto {
    private final long id;
    private final String nombre;
    private final BigDecimal precio;

    public Producto(long id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
