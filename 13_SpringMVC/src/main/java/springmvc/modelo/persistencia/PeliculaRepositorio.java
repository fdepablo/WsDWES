package springmvc.modelo.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import springmvc.modelo.entidad.Pelicula;

/** Spring Data JPA genera la implementación de este repositorio. */
public interface PeliculaRepositorio extends JpaRepository<Pelicula, Long> {
    List<Pelicula> findAllByOrderByTituloAsc();
}
