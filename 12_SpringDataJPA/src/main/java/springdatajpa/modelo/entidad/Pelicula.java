package springdatajpa.modelo.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA asociada a la tabla {@code peliculas}.
 * JPA necesita un constructor sin argumentos; el ID lo genera la base de datos.
 * Esta clase no es un bean de Spring ni un record: JPA gestiona sus instancias
 * y necesita asignar el ID después de crear cada objeto.
 */
@Entity
@Table(name = "peliculas")
public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String titulo;

    @Column(nullable = false)
    private String genero;

    protected Pelicula() {
        // Constructor requerido por JPA; el alumnado usa el constructor público.
    }

    public Pelicula(String titulo, String genero) {
        this.titulo = titulo;
        this.genero = genero;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    @Override
    public String toString() {
        return "Pelicula{id=" + id + ", titulo='" + titulo + "', genero='" + genero + "'}";
    }
}
