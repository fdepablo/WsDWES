package springmvc.controlador;

/** Datos que Spring MVC recibe de los campos del formulario HTML. */
public class FormularioPelicula {
    private String titulo = "";
    private String genero = "";

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
