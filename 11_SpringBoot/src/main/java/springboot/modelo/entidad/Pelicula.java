package springboot.modelo.entidad;

/**
 * Representa los datos de una película mediante un {@code record} de Java,
 * disponible como característica definitiva desde Java 16.
 * Un record declara sus componentes entre paréntesis. Java genera un constructor
 * con esos componentes, métodos para consultarlos ({@code id()},
 * {@code titulo()} y {@code genero()}), además de {@code equals()},
 * {@code hashCode()} y {@code toString()}. Por eso no escribimos esos métodos
 * ni los getters habituales de una clase JavaBean.
 *
 * <p>Sus componentes no se pueden reasignar después de crear la película.
 * Este ejemplo la usa como dato del catálogo: el repositorio crea cada
 * instancia con {@code new Pelicula(...)}; no es un bean gestionado por Spring.
 *
 * @param id identificador asignado por el repositorio
 * @param titulo título de la película
 * @param genero género de la película
 */
public record Pelicula(int id, String titulo, String genero) {
}
