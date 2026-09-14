package herencia;

public class OsoPanda extends Animal{
    private String colorPelo;

    @Override
    public void comer(Object comida) {
        if(comida == this){
            System.out.println("No me voy a comer a mimsmo :P");
            return;
        }

        if(this.getEdad() <= 2){
            if(comida instanceof String){
                String s = (String)comida;
                if(s.equals("leche")){
                    System.out.println("Soy un osito bebe :) y voy a tomar leche");
                    this.setPeso(this.getPeso() + 0.5);
                }
            }else{
                System.out.println("Solo bebo leche \uD83D\uDE05");
            }
        }else{
            //Esto es equivalente a preguntar y a convertir como arriba
            if(comida instanceof Animal animal){
                System.out.println("Ummm soy un oso adulto y voy a comer un animal " +
                        " :) :)");
                this.setPeso(this.getPeso() + (animal.getPeso()  / 10));
            }else{
                System.out.println("solo como animales");
            }
        }
    }
}
