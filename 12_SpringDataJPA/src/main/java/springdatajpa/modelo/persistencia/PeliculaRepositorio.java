package springdatajpa.modelo.persistencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import springdatajpa.modelo.entidad.Pelicula;

/** Spring Data JPA proporciona la implementación de esta interfaz al arrancar. */
public interface PeliculaRepositorio extends JpaRepository<Pelicula, Long> {
    List<Pelicula> findAllByOrderByTituloAsc();

    List<Pelicula> findByTituloContainingIgnoreCase(String texto);

    Optional<Pelicula> findByTituloIgnoreCase(String titulo);

    /** Consulta JPQL: usa el nombre de la entidad y de su propiedad, no la tabla SQL. */
    @Query("select p from Pelicula p where p.genero = :genero order by p.titulo")
    List<Pelicula> buscarPorGenero(@Param("genero") String genero);
}
