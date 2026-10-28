package generic;

import java.sql.ClientInfoStatus;
import java.util.ArrayList;

public class EjemploGenericos {
    static void main() {
        ArrayList listObjetos = new ArrayList();
        listObjetos.add(5);
        listObjetos.add("cinco");

        listObjetos.forEach( v -> System.out.println(v));


        AlmacenEnteros ae = new AlmacenEnteros();
        ae.setValor(5);

        AlmacenCadenas ac = new AlmacenCadenas();
        ac.setValor("Cinco");

        AlmacenObjetos ao = new AlmacenObjetos();
        ao.setValor(5);
        ao.setValor("Cinco");

        AlmacenGenericos<String> almacenCadenas = new AlmacenGenericos<>();
        almacenCadenas.setValor("cinco");
        //almacenCadenas.setValor(5);

        ArrayList<String> stringArrayList = new ArrayList<>();
        //Sabes como va

    }
}
