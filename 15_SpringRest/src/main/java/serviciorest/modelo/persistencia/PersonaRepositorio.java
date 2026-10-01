package serviciorest.modelo.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import serviciorest.modelo.entidad.Persona;

/** Spring Data JPA genera la implementación; no hay una lista de personas manual. */
public interface PersonaRepositorio extends JpaRepository<Persona, Long> {
    List<Persona> findAllByOrderByIdAsc();

    List<Persona> findByNombreIgnoreCaseOrderByIdAsc(String nombre);
}
