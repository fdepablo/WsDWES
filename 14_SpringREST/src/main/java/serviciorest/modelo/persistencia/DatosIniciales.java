package serviciorest.modelo.persistencia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import serviciorest.modelo.entidad.Persona;

/** Carga las cinco personas del ejemplo original al arrancar la base H2 vacía. */
@Component
public class DatosIniciales implements CommandLineRunner {
    private final PersonaRepositorio repositorio;

    public DatosIniciales(PersonaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        repositorio.save(new Persona("STEVE", "ROGERS", 39));
        repositorio.save(new Persona("HARRY", "POTTER", 19));
        repositorio.save(new Persona("CHIQUITO", "DE LA CALZADA", 79));
        repositorio.save(new Persona("BUD", "SPENCER", 85));
        repositorio.save(new Persona("HARRY", "CALLAHAN", 87));
    }
}
