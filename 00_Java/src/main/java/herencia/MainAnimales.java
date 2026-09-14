package herencia;

import java.util.ArrayList;

public class MainAnimales {
    static void main() {
        Animal a = new Tiburon();
        a.setNombre("Ferran");
        a.setEdad(3);
        a.setPeso(190);
        //MEjor utilizar la referencia propia en este caso
        Tiburon t = (Tiburon)a;
        t.setNumAletas(4);

        OsoPanda yogui = new OsoPanda();
        yogui.setNombre("Yogui");
        yogui.setPeso(450);
        yogui.setEdad(8);

        OsoPanda bubu = new OsoPanda();
        bubu.setNombre("Bubu");
        bubu.setPeso(69);
        bubu.setEdad(2);

        ArrayList<Animal> listaAnimales = new ArrayList<>();
        listaAnimales.add(t);
        listaAnimales.add(yogui);
        listaAnimales.add(bubu);

        String comida = "leche";

        for (Animal animal : listaAnimales){
            animal.comer(comida);
        }



        listaAnimales.forEach(animal -> {
            animal.comer(yogui);
        });

        //En java podemos recurrir la las clases anonimas para instanciar
        //clases abstractas

        //Creamos la clase y el objeto al mismo tiempo, heredando la clase
        //de la clase padre, en este caso animal.
        Animal zorro = new Animal() {
            @Override
            public void comer(Object comida) {
                if(comida instanceof OsoPanda op){
                    System.out.println("Voy a comerme un oso!!!");
                }
            }
        };
        System.out.println("------------");
        listaAnimales.add(zorro);//Se puede meter en la clase animal
        zorro.comer(yogui);
    }
}
