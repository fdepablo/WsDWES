package modelo.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private LocalDateTime fecha;

    // Muchos pedidos pueden apuntar a un cliente; Cliente no guarda una lista.
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    protected Pedido() {
    }

    public Pedido(String codigo, LocalDateTime fecha, Cliente cliente) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }
}
