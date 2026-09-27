package springboot.modelo.persistencia;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import springboot.modelo.entidad.Pelicula;

/** Almacén temporal: los datos desaparecen al terminar la aplicación. */
@Repository
public class PeliculaRepositorio {
    private final List<Pelicula> peliculas = new ArrayList<>();
    private final int maximoPeliculas;
    private int siguienteId = 1;

    /** Spring toma el límite de application.properties y lo pasa al constructor. */
    public PeliculaRepositorio(@Value("${catalogo.maximo-peliculas}") int maximoPeliculas) {
        if (maximoPeliculas < 1) {
            throw new IllegalArgumentException("El máximo de películas debe ser positivo");
        }
        this.maximoPeliculas = maximoPeliculas;
    }

    public Pelicula guardar(String titulo, String genero) {
        if (peliculas.size() >= maximoPeliculas) {
            throw new IllegalStateException("El catálogo está lleno (máximo " + maximoPeliculas + ")");
        }
        Pelicula pelicula = new Pelicula(siguienteId++, titulo, genero);
        peliculas.add(pelicula);
        return pelicula;
    }

    public List<Pelicula> listar() {
        return List.copyOf(peliculas);
    }

    public Pelicula buscarPorId(int id) {
        for (Pelicula pelicula : peliculas) {
            if (pelicula.id() == id) {
                return pelicula;
            }
        }
        return null;
    }
}
