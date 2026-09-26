package modelo.entidad;

import jakarta.persistence.Embeddable;

/** Sus campos se guardan en la tabla de Cliente, sin tabla propia. */
@Embeddable
public class Direccion {
    private String calle;
    private String ciudad;

    protected Direccion() {
    }

    public Direccion(String calle, String ciudad) {
        this.calle = calle;
        this.ciudad = ciudad;
    }

    public String getCalle() {
        return calle;
    }

    public String getCiudad() {
        return ciudad;
    }
}
