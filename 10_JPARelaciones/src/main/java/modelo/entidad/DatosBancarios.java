package modelo.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "datos_bancarios")
public class DatosBancarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String banco;
    private String iban;

    protected DatosBancarios() {
    }

    public DatosBancarios(String banco, String iban) {
        this.banco = banco;
        this.iban = iban;
    }

    public Long getId() {
        return id;
    }

    public String getBanco() {
        return banco;
    }

    public String getIban() {
        return iban;
    }
}
