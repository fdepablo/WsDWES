package serviciorest.controlador;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dos respuestas sencillas para observar la cabecera Content-Type. */
@RestController
@RequestMapping("/rest")
public class ControladorMensaje {
    @GetMapping(value = "/mensaje", produces = MediaType.TEXT_PLAIN_VALUE)
    public String obtenerMensaje() {
        return "Esto es un mensaje de prueba";
    }

    @GetMapping(value = "/mensajeHTML", produces = MediaType.TEXT_HTML_VALUE)
    public String obtenerMensajeHTML() {
        return "<!DOCTYPE html><html><head><title>Prueba html</title></head>"
                + "<body><h1 style='color:red'>ESTO SERIA UN MENSAJE EN HTML</h1></body></html>";
    }
}
