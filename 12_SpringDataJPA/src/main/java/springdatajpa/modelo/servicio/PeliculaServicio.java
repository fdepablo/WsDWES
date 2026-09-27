package springdatajpa.modelo.servicio;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import springdatajpa.modelo.entidad.Pelicula;
import springdatajpa.modelo.persistencia.PeliculaRepositorio;

/** Aplica las reglas de entrada y delega el acceso a la base de datos al repositorio JPA. */
@Service
public class PeliculaServicio {
    private final PeliculaRepositorio repositorio;

    public PeliculaServicio(PeliculaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Pelicula crear(String titulo, String genero) {
        validar(titulo, genero);
        return repositorio.save(new Pelicula(titulo.trim(), genero.trim()));
    }

    /** Evita duplicar los datos de demostración al usar una base persistente. */
    public Pelicula obtenerOCrear(String titulo, String genero) {
        validar(titulo, genero);
        Optional<Pelicula> existente = repositorio.findByTituloIgnoreCase(titulo.trim());
        if (existente.isPresent()) {
            return existente.get();
        }
        return crear(titulo, genero);
    }

    /** Devuelve un Optional presente si existe el ID, o vacío si no existe. */
    public Optional<Pelicula> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public List<Pelicula> listar() {
        return repositorio.findAllByOrderByTituloAsc();
    }

    public List<Pelicula> buscarPorTitulo(String texto) {
        return repositorio.findByTituloContainingIgnoreCase(texto);
    }

    public List<Pelicula> buscarPorGenero(String genero) {
        return repositorio.buscarPorGenero(genero);
    }

    /** Actualiza una película existente; un Optional vacío indica que no hay tal ID. */
    public Optional<Pelicula> cambiarGenero(Long id, String nuevoGenero) {
        Optional<Pelicula> encontrada = repositorio.findById(id);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Pelicula pelicula = encontrada.get();
        validar(pelicula.getTitulo(), nuevoGenero);
        pelicula.setGenero(nuevoGenero.trim());
        return Optional.of(repositorio.save(pelicula));
    }

    public void borrar(Long id) {
        repositorio.deleteById(id);
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
