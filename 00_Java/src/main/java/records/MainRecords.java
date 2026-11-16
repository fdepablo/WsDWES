package records;

public class MainRecords {
    static void main() {
        Videojuego v = new Videojuego("Halo", 9, 39.95);
        VideojuegoRecord vr = new VideojuegoRecord("GTA VI", 10, 99.95);

        //Ojo, el record no podemos modificar sus valores
        v.setNombre("Hollow Night");
        //vr.setNombre("GTA V");

        System.out.println(v);
        System.out.println(vr);
        System.out.println(vr.esJugable());
    }
}
