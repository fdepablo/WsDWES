package records;

public class Videojuego {
    private String nombre;
    private int nota;
    private double precio;

    public Videojuego(String nombre, int nota, double precio) {
        this.nombre = nombre;
        this.nota = nota;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Videojuego{" +
                "nombre='" + nombre + '\'' +
                ", nota=" + nota +
                ", precio=" + precio +
                '}';
    }

    public boolean esJugable(){
        if(this.nota >= 7){
            return true;
        }
        return false;
    }
}
