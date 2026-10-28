package _04_lambdas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class _00_Lambdas {


    public static void main(String[] args) {
        //Antes de Java 8 podiamos hacer funcionalidad parecida a las funciones
        //lambda mediante clases anonimas

        //En este caso voy a crearme una clase anonima cuya funcionalidad
        //sera imprimir por fichero
        _00_Imprimible imprimible = new _00_Imprimible() {

            @Override
            public void imprimir(String cadena) {
                File fichero = new File("prueba.txt");

                try (FileWriter fw = new FileWriter(fichero)) {
                    fw.write(cadena);
                } catch (IOException e) {
                    e.printStackTrace();
                } //un fw.close() de manera implicita
            }
        };

        imprimible.imprimir("Esto es una cadena escrita con una clase anonima");

        imprimible = new _00_ImprimirPantalla();
        imprimible.imprimir("Esto es una cadena escrita con una clase anonima");

        //Ahora con funciones lambda
        _00_Imprimible imprimiblePantallaLambda = (cadena) -> System.out.println(cadena);
        _00_Imprimible imprimibleFicheroLambda = (cadena) -> {
            File fichero = new File("prueba.txt");
            try (FileWriter fw = new FileWriter(fichero)) {
                fw.write(cadena);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        imprimibleFicheroLambda.imprimir("Ahora imprimo con lambda");
        imprimiblePantallaLambda.imprimir("Ahora imprimo con lambda");

        var numero = 45;//El tipo de la variable se define cuando se le asigna un valor
                        //En este caso será de tipo int
        System.out.println(numero);
        //numero = 45.67; No puedo, es de tipo int

        //Nota, esto solo vale para variables, no vale para atributos de clase
        //Nota 2, tampoco vale para funciones lambda

        _00_Imprimible imprimirPantallaError = (cadena) -> System.err.println(cadena);
        imprimirPantallaError.imprimir("Esto es un error");
        imprimibleFicheroLambda.imprimir("ESto se imprimer por fichero");
    }
}