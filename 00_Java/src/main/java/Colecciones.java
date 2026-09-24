import org.example.Persona;

import java.util.HashMap;

public class Colecciones {
    static void main() {
        HashMap<Integer, Persona> equipoFutbol = new HashMap<>();
        Persona p = new Persona();
        p.setNombre("Casillas");

        equipoFutbol.put(1,p);

        p = new Persona();
        p.setNombre("Xabi Alonso");

        equipoFutbol.put(2,p);

        System.out.println(equipoFutbol.get(1).getNombre());

        p = new Persona();
        p.setNombre("Raul Gonzalez");
        equipoFutbol.put(2,p);

        System.out.println(equipoFutbol.get(2).getNombre());

        System.out.println(equipoFutbol.get(3).getNombre());


    }
}
