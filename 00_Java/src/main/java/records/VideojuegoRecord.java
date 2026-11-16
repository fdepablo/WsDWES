package records;

/**
 * Los records tienen las siguientes caracteristicas
 * 1- Se crean sus atributos privados
 * 2- Se crea un constructor con todos los atributos
 * 3- Se crea los getters (no se crean lo setters, por lo tanto son inmutables)
 * 4- Se crea el toString con todos los atributos.
 * */
public record VideojuegoRecord(String nombre, int nota, double precio) {
    public boolean esJugable(){
        if(this.nota >= 7){
            return true;
        }
        return false;
    }
}
