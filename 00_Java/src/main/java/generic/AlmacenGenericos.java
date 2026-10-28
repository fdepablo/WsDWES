package generic;

import herencia.Animal;

public class AlmacenGenericos <T> {
    private T valor;

    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        this.valor = valor;
    }
}
