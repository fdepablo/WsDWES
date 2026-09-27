package springboot.presentacion;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springboot.modelo.entidad.Pelicula;
import springboot.modelo.servicio.PeliculaServicio;

/** Ejecuta un recorrido fijo al arrancar, sin interfaz web. */
@Component
public class DemoConsola implements CommandLineRunner {

    @Autowired
    private PeliculaServicio servicio;

    @Override
    public void run(String... args) {
        System.out.println("=== Catálogo de películas ===");
        servicio.crear("La llegada", "Ciencia ficción");
        servicio.crear("Coco", "Animación");
        for (Pelicula pelicula : servicio.listar()) {
            System.out.println(pelicula);
        }
        System.out.println("Búsqueda del ID 1: " + servicio.buscarPorId(1));
        System.out.println("Búsqueda del ID 99: " + servicio.buscarPorId(99));
        try {
            servicio.crear("  ", "Drama");
        } catch (IllegalArgumentException error) {
            System.out.println("Alta rechazada: " + error.getMessage());
        }
        try {
            servicio.crear("Amélie", "Comedia");
        } catch (IllegalStateException error) {
            System.out.println("Alta rechazada: " + error.getMessage());
        }
        System.out.println("Total tras el rechazo: " + servicio.listar().size());
    }
}
