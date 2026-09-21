package utils;

/**
 * Utilidades mínimas para escribir valores dinámicos dentro de HTML.
 *
 * <p>Ejemplo de uso:</p>
 * <pre>{@code
 * String nombre = "<strong>Ana</strong>";
 * String nombreSeguro = UtilsHTML.escapar(nombre);
 *
 * // Resultado: &lt;strong&gt;Ana&lt;/strong&gt;
 * out.println(nombreSeguro);
 * }</pre>
 */
public final class UtilsHTML {

    private UtilsHTML() {
        // Una clase de utilidades no necesita instancias.
    }

    /**
     * Escapa los caracteres con significado especial en HTML.
     *
     * <p>Los parámetros de una petición proceden del cliente y no deben
     * insertarse directamente en una página.</p>
     *
     * @param valor texto que se mostrará en el HTML
     * @return texto seguro para incluirlo como contenido o atributo HTML
     */
    public static String escapar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
