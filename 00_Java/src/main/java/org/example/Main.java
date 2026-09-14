package org.example;

import java.util.ArrayList;

public class Main {
    static void main() {
        Persona p = new Persona();
        p.setNombre("Peter Parker");//A donde apunte p, ejecuta el metodo
                                    //setNombre
        p.setEdad(18);
        p.setPeso(65);

        Persona p2 = new Persona();
        p2.setNombre("Ironman");
        p2.setPeso(100);
        p2.setEdad(40);

        p = p2;
        p.setPeso(55);
        System.out.println(p2.getPeso());
        System.out.println(p.getPeso());

        int n1 = 50;
        int n2 = 100;
        n1 = n2;
        n1 = 75;
        System.out.println(n1);
        System.out.println(n2);

        int cp = 04567;//los numeros que empiezan por 0 son formato octal
        System.out.println(cp);

        int mac = 0xFFAA;
        System.out.println(mac);

        int binario = 0b0101;
        System.out.println(binario);

        Persona p3 = new Persona();
        p3.setNombre("Natasha");
        p3.setPeso(60);
        p3.setEdad(35);
        Direccion d1 = new Direccion();
        p3.setDireccion(d1);
        p3.getDireccion().setNombreVia("Calla de la Plata");
        System.out.println(p3.getDireccion().getNombreVia());
        System.out.println(d1.getNombreVia());

        d1.setCiudad("Madrid");
        System.out.println(p3.getDireccion().getCiudad());

        d1 = new Direccion();
        d1.setCiudad("Barcelona");
        System.out.println(p3.getDireccion().getCiudad());

        p3.setDireccion(d1);


        ArrayList<Persona> listaPersonas = new ArrayList<>();
        Persona p4 = new Persona();
        listaPersonas.add(p4);
        p4.setNombre("Steve Rogers");
        listaPersonas.get(0).setNombre("Steve Roger");//totalmente equivalente

        System.out.println(listaPersonas.get(0).getNombre());

        p4 = new Persona();
        p4.setNombre("Hulk");
        listaPersonas.add(p4);

        Direccion d2 = new Direccion();
        listaPersonas.get(0).setDireccion(d2);
        System.out.println(listaPersonas.get(0).getDireccion().getCiudad());
        listaPersonas.get(0).getDireccion().setCiudad("Zaragoza");
        System.out.println(d2.getCiudad());

        //Nuevo comentario de prueba para subir a github
    }
}
