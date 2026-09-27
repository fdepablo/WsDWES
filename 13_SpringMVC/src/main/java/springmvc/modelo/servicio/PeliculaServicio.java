package springmvc.modelo.servicio;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import springmvc.modelo.entidad.Pelicula;
import springmvc.modelo.persistencia.PeliculaRepositorio;

/** Aplica las reglas del catálogo sin conocer HTTP ni Thymeleaf. */
@Service
public class PeliculaServicio {
    private final PeliculaRepositorio repositorio;

    public PeliculaServicio(PeliculaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Pelicula> listar() {
        return repositorio.findAllByOrderByTituloAsc();
    }

    public Optional<Pelicula> buscar(Long id) {
        return repositorio.findById(id);
    }

    public void crear(String titulo, String genero) {
        validar(titulo, genero);
        repositorio.save(new Pelicula(titulo.trim(), genero.trim()));
    }

    public boolean actualizar(Long id, String titulo, String genero) {
        Optional<Pelicula> encontrada = repositorio.findById(id);
        if (encontrada.isEmpty()) {
            return false;
        }
        validar(titulo, genero);
        Pelicula pelicula = encontrada.get();
        pelicula.setTitulo(titulo.trim());
        pelicula.setGenero(genero.trim());
        repositorio.save(pelicula);
        return true;
    }

    public boolean borrar(Long id) {
        if (!repositorio.existsById(id)) {
            return false;
        }
        repositorio.deleteById(id);
        return true;
    }

    private void validar(String titulo, String genero) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (genero == null || genero.isBlank()) {
            throw new IllegalArgumentException("El género es obligatorio");
        }
    }
}
