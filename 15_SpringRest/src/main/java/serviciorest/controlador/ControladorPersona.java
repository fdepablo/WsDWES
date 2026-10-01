package serviciorest.controlador;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serviciorest.modelo.entidad.Persona;
import serviciorest.modelo.persistencia.PersonaRepositorio;

/** Conserva el CRUD del ejemplo original, ahora sobre una tabla H2. */
@RestController
@RequestMapping("/rest/personas")
public class ControladorPersona {
    private final PersonaRepositorio repositorio;

    public ControladorPersona(PersonaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Persona> getPersona(@PathVariable("id") Long id) {
        Optional<Persona> encontrada = repositorio.findById(id);
        if (encontrada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(encontrada.get());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Persona> altaPersona(@RequestBody Persona persona) {
        if (!datosValidos(persona)) {
            return ResponseEntity.badRequest().build();
        }
        // No se acepta el ID del cliente: JPA lo genera para cada alta.
        Persona nueva = new Persona(persona.getNombre().trim(), persona.getApellidos().trim(), persona.getEdad());
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(nueva));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Persona>> listarPersonas(
            @RequestParam(name = "nombre", required = false) String nombre) {
        List<Persona> personas = nombre == null
                ? repositorio.findAllByOrderByIdAsc()
                : repositorio.findByNombreIgnoreCaseOrderByIdAsc(nombre);
        return ResponseEntity.ok(personas);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Persona> modificarPersona(
            @PathVariable("id") Long id, @RequestBody Persona datos) {
        Optional<Persona> encontrada = repositorio.findById(id);
        if (encontrada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!datosValidos(datos)) {
            return ResponseEntity.badRequest().build();
        }
        Persona persona = encontrada.get();
        persona.setNombre(datos.getNombre().trim());
        persona.setApellidos(datos.getApellidos().trim());
        persona.setEdad(datos.getEdad());
        repositorio.save(persona);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Persona> borrarPersona(@PathVariable("id") Long id) {
        Optional<Persona> encontrada = repositorio.findById(id);
        if (encontrada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repositorio.deleteById(id);
        return ResponseEntity.ok(encontrada.get());
    }

    private boolean datosValidos(Persona persona) {
        return persona.getNombre() != null && !persona.getNombre().isBlank()
                && persona.getApellidos() != null && !persona.getApellidos().isBlank()
                && persona.getEdad() >= 0;
    }
}
