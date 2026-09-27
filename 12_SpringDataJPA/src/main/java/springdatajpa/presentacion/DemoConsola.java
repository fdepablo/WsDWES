package springdatajpa.presentacion;

import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import springdatajpa.modelo.entidad.Pelicula;
import springdatajpa.modelo.servicio.PeliculaServicio;

/** Recorre las operaciones principales del repositorio al arrancar. */
@Component
public class DemoConsola implements CommandLineRunner {
    private final PeliculaServicio servicio;

    public DemoConsola(PeliculaServicio servicio) {
        this.servicio = servicio;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Spring Data JPA ===");
        Pelicula llegada = servicio.obtenerOCrear("La llegada", "Ciencia ficción");
        Pelicula coco = servicio.obtenerOCrear("Coco", "Animación");
        System.out.println("IDs de la base: " + llegada.getId() + " y " + coco.getId());

        Optional<Pelicula> encontrada = servicio.buscarPorId(coco.getId());
        if (encontrada.isPresent()) {
            System.out.println("Búsqueda por ID: " + encontrada.get());
        }

        Optional<Pelicula> inexistente = servicio.buscarPorId(-1L);
        if (inexistente.isEmpty()) {
            System.out.println("ID -1: no existe ninguna película");
        }

        System.out.println("Títulos que contienen 'CO', sin distinguir mayúsculas:");
        for (Pelicula pelicula : servicio.buscarPorTitulo("CO")) {
            System.out.println(pelicula);
        }

        System.out.println("Películas del género Ciencia ficción (JPQL):");
        for (Pelicula pelicula : servicio.buscarPorGenero("Ciencia ficción")) {
            System.out.println(pelicula);
        }

        Optional<Pelicula> modificada = servicio.cambiarGenero(coco.getId(), "Familiar");
        if (modificada.isPresent()) {
            System.out.println("Película modificada: " + modificada.get());
        }
        Pelicula temporal = servicio.obtenerOCrear("Temporal", "Prueba");
        servicio.borrar(temporal.getId());
        System.out.println("Listado final tras modificar y borrar:");
        for (Pelicula pelicula : servicio.listar()) {
            System.out.println(pelicula);
        }
    }
}
