package modelo.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @Embedded
    private Direccion direccion;

    // Solo Cliente conoce los datos bancarios; la FK queda en clientes.
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "datos_bancarios_id", unique = true)
    private DatosBancarios datosBancarios;

    // El comercial puede atender a varios clientes y no se borra con ellos.
    @ManyToMany
    @JoinTable(name = "clientes_comerciales",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "comercial_id"))
    private List<Comercial> comerciales = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nombre, Direccion direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public DatosBancarios getDatosBancarios() {
        return datosBancarios;
    }

    public void setDatosBancarios(DatosBancarios datosBancarios) {
        this.datosBancarios = datosBancarios;
    }

    public List<Comercial> getComerciales() {
        return comerciales;
    }
}
