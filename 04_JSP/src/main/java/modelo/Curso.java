package modelo;

/** Datos sencillos de un curso para practicar el acceso a propiedades desde EL. */
public class Curso {

    private final String nombre;
    private final String ciclo;

    /**
     * Crea los datos que la JSP mostrará mediante EL.
     * Por ejemplo, {@code new Curso("DWES", "DAW")}.
     *
     * @param nombre nombre del curso
     * @param ciclo ciclo al que pertenece
     */
    public Curso(String nombre, String ciclo) {
        this.nombre = nombre;
        this.ciclo = ciclo;
    }

    /**
     * Devuelve el nombre; la JSP puede leerlo con {@code curso.nombre}.
     *
     * @return nombre indicado al crear el curso, por ejemplo "DWES"
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el ciclo; la JSP puede leerlo con {@code curso.ciclo}.
     *
     * @return ciclo indicado al crear el curso, por ejemplo "DAW"
     */
    public String getCiclo() {
        return ciclo;
    }
}
