package springboot.modelo.servicio;

import java.util.List;
import org.springframework.stereotype.Service;
import springboot.modelo.entidad.Pelicula;
import springboot.modelo.persistencia.PeliculaRepositorio;

/** Aplica las reglas del catálogo y delega el almacenamiento al repositorio. */
@Service
public class PeliculaServicio {
    private final PeliculaRepositorio repositorio;

    /** Spring inyecta el único constructor sin necesidad de @Autowired. */
    public PeliculaServicio(PeliculaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Pelicula crear(String titulo, String genero) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (genero == null || genero.isBlank()) {
            throw new IllegalArgumentException("El género no puede estar vacío");
        }
        return repositorio.guardar(titulo.trim(), genero.trim());
    }

    public List<Pelicula> listar() {
        return repositorio.listar();
    }

    public Pelicula buscarPorId(int id) {
        return repositorio.buscarPorId(id);
    }
}
