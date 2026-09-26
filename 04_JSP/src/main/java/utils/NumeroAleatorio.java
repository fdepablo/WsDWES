package utils;

import java.util.concurrent.ThreadLocalRandom;

/** Genera números para el contenido dinámico del ejemplo. */
public final class NumeroAleatorio {

    /** Impide crear instancias: se usa directamente {@code NumeroAleatorio.generar(1, 100)}. */
    private NumeroAleatorio() {
        // Una clase de utilidades no necesita instancias.
    }

    /**
     * Genera un entero comprendido entre los dos límites, ambos incluidos.
     * Por ejemplo, {@code generar(1, 6)} devuelve un valor entre 1 y 6.
     *
     * @param minimo límite inferior
     * @param maximo límite superior
     * @return número generado, incluidos los valores límite
     */
    public static int generar(int minimo, int maximo) {
        return ThreadLocalRandom.current().nextInt(minimo, maximo + 1);
    }
}
