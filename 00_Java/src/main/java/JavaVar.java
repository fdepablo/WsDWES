import org.example.Persona;

public class JavaVar {
    static void main() {
        Persona p = new Persona();
        String s = "Felix";
        //Java adminte inferencia de tipos, esto es
        // que el tipo de la variable se define cuando se le asigna
        //el valor.
        //ojo, no confundir con el var de Javascript,
        //java seguira siendo tipado, por lo que solamente
        //podremos asignarle el tipo inicial
        var persona = new Persona();
        var cadena = "En un lugar de la mancha";
        //cadena = 5;Error

    }
}
