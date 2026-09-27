package springmvc.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import springmvc.modelo.entidad.Pelicula;
import springmvc.modelo.servicio.PeliculaServicio;

/** Traduce peticiones HTTP a operaciones del servicio y nombres de vista. */
@Controller
public class PeliculaControlador {
    private final PeliculaServicio servicio;

    public PeliculaControlador(PeliculaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/peliculas";
    }

    @GetMapping("/peliculas")
    public String listar(Model model) {
        model.addAttribute("peliculas", servicio.listar());
        return "listado";
    }

    @GetMapping("/peliculas/nueva")
    public String nueva(Model model) {
        model.addAttribute("formulario", new FormularioPelicula());
        model.addAttribute("tituloPagina", "Nueva película");
        return "formulario";
    }

    @PostMapping("/peliculas")
    public String crear(@ModelAttribute("formulario") FormularioPelicula formulario, Model model) {
        try {
            servicio.crear(formulario.getTitulo(), formulario.getGenero());
            return "redirect:/peliculas";
        } catch (IllegalArgumentException error) {
            model.addAttribute("tituloPagina", "Nueva película");
            model.addAttribute("error", error.getMessage());
            return "formulario";
        }
    }

    @GetMapping("/peliculas/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Pelicula pelicula = buscarO404(id);
        FormularioPelicula formulario = new FormularioPelicula();
        formulario.setTitulo(pelicula.getTitulo());
        formulario.setGenero(pelicula.getGenero());
        model.addAttribute("formulario", formulario);
        model.addAttribute("id", id);
        model.addAttribute("tituloPagina", "Editar película");
        return "formulario";
    }

    @PostMapping("/peliculas/{id}/editar")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute("formulario") FormularioPelicula formulario,
                             Model model) {
        buscarO404(id);
        try {
            servicio.actualizar(id, formulario.getTitulo(), formulario.getGenero());
            return "redirect:/peliculas";
        } catch (IllegalArgumentException error) {
            model.addAttribute("id", id);
            model.addAttribute("tituloPagina", "Editar película");
            model.addAttribute("error", error.getMessage());
            return "formulario";
        }
    }

    @PostMapping("/peliculas/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        if (!servicio.borrar(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/peliculas";
    }

    private Pelicula buscarO404(Long id) {
        return servicio.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
