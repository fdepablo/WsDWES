package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.servicio.ProductoServicio;
import modelo.persistencia.ConexionBDH2;
import modelo.persistencia.interfaces.ConexionBD;
import modelo.entidad.Producto;

import java.io.IOException;
import java.sql.SQLException;

/** Controlador MVC: interpreta la petición, consulta el modelo y selecciona la vista. */
// Inicializa el servlet al desplegar la aplicación, sin esperar a la primera petición.
@WebServlet(value = "/productos/*", loadOnStartup = 1)
public class ProductoServlet extends HttpServlet {
    private ProductoServicio servicio;

    /**
     * Se ejecuta una vez al crear el servlet. Delega en el servicio la
     * preparación del modelo y la conexión con la persistencia.
     *
     * @throws ServletException si no se puede abrir o preparar la base de datos
     */
    @Override
    public void init() throws ServletException {
        // Las rutas relativas de H2, como ./datos, parten de este directorio.
        System.out.println("08_MVC - Directorio de trabajo: " + System.getProperty("user.dir"));
        try {
            // Para usar MariaDB, cambia el objeto por new ConexionBDMariaDB() y su import.
            ConexionBD baseDatos = new ConexionBDH2();
            // El servicio recibe la conexión; el controlador no utiliza el DAO ni ejecuta SQL.
            servicio = new ProductoServicio(baseDatos);
        } catch (SQLException e) {
            throw new ServletException("No se pudo preparar el catálogo de productos.", e);
        }
    }

    /**
     * Muestra el listado o el formulario solicitado. Para editar, busca antes
     * el producto y devuelve 404 si el ID ya no existe.
     *
     * @param request petición con la ruta y, al editar, el parámetro id
     * @param response respuesta que mostrará la JSP o un error HTTP
     * @throws ServletException si falla la consulta o el forward
     * @throws IOException si falla la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // /productos/* permite atender varias rutas: aquí obtenemos /nuevo, /editar... o null.
            String ruta = request.getPathInfo();
            if (ruta == null || "/".equals(ruta)) {
                // La lista llega a la JSP como atributo de esta petición, no como dato de sesión.
                request.setAttribute("productos", servicio.listar());
                mostrar(request, response, "listado.jsp");
            } else if ("/nuevo".equals(ruta)) {
                // "accion" es un atributo para configurar la JSP compartida, no un parámetro recibido.
                request.setAttribute("accion", "nuevo");
                mostrar(request, response, "alta_editar.jsp");
            } else if ("/editar".equals(ruta)) {
                Long id = leerId(request, response);
                // leerId ya ha enviado el error 400 si devuelve null: no continuamos.
                if (id == null) return;
                Producto producto = servicio.buscar(id);
                if (producto == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                // Usamos atributos de formulario que también podremos rellenar tras un error de validación.
                request.setAttribute("idFormulario", producto.getId());
                request.setAttribute("nombreFormulario", producto.getNombre());
                request.setAttribute("precioFormulario", producto.getPrecio());
                request.setAttribute("accion", "editar");
                mostrar(request, response, "alta_editar.jsp");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Error al consultar los productos.", e);
        }
    }

    /**
     * Recibe las operaciones que modifican datos. Establece UTF-8 antes de
     * leer el formulario y redirige al listado tras una operación correcta.
     *
     * @param request petición de alta, edición o borrado
     * @param response respuesta o redirección al listado
     * @throws ServletException si falla el acceso a la base o una vista
     * @throws IOException si falla la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // La codificación se establece antes de leer cualquier parámetro del formulario.
        request.setCharacterEncoding("UTF-8");
        String ruta = request.getPathInfo();
        try {
            if ("/nuevo".equals(ruta) || "/editar".equals(ruta)) {
                // Alta y edición comparten la recogida de datos y el tratamiento de errores.
                guardar(request, response, ruta);
            } else if ("/eliminar".equals(ruta)) {
                Long id = leerId(request, response);
                if (id == null) return;
                // false significa que no existía una fila con ese ID, no que fallara JDBC.
                if (!servicio.eliminar(id)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/productos");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Error al guardar los productos.", e);
        }
    }

    /**
     * Entrega los datos al servicio. Si este rechaza los valores, conserva
     * lo escrito en atributos de petición para volver a mostrar el formulario.
     *
     * @param ruta distingue el alta de la edición
     * @throws SQLException si falla la consulta o la escritura SQL
     */
    private void guardar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException, SQLException {
        // La ruta decide la operación; el ID solo es necesario cuando modificamos una fila existente.
        boolean edicion = "/editar".equals(ruta);
        // Si edición es true, leemos el ID porque venimos de editar, null en caso contrario, ya que venimos de alta.
        Long id = edicion ? leerId(request, response) : null;
        if (edicion && id == null) return;

        String nombre = request.getParameter("nombre");
        // Conservamos el texto original: el servicio lo convierte y lo valida, y la JSP puede volver a mostrarlo.
        String textoPrecio = request.getParameter("precio");
        try {
            boolean guardado;
            if (edicion) {
                guardado = servicio.actualizar(id, nombre, textoPrecio);
            } else {//Si no es edición, es alta
                servicio.crear(nombre, textoPrecio);
                // crear no devuelve boolean: si termina sin excepción, la inserción se ha realizado.
                guardado = true;
            }
            if (!guardado) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (IllegalArgumentException e) {
            // El servicio detecta el error de negocio; el controlador lo convierte en una respuesta HTTP 400.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", e.getMessage());
            // Devolvemos lo escrito, aunque no sea válido, sin guardarlo ni obligar a rellenarlo de nuevo.
            request.setAttribute("nombreFormulario", nombre);
            request.setAttribute("precioFormulario", textoPrecio);
            request.setAttribute("accion", edicion ? "editar" : "nuevo");
            if (edicion) request.setAttribute("idFormulario", id);
            // forward conserva estos atributos; redirect los perdería al crear otra petición.
            mostrar(request, response, "alta_editar.jsp");
            return;
        }
        // POST/Redirect/GET: actualizar el listado no vuelve a enviar el alta o la edición.
        response.sendRedirect(request.getContextPath() + "/productos");
    }

    /**
     * Convierte el parámetro id; un valor ausente o inválido produce 400.
     *
     * @return ID positivo o null cuando ya se ha enviado el error
     */
    private Long leerId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            long id = Long.parseLong(request.getParameter("id"));
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {
            // Una URL o un formulario manipulado puede enviar un ID no numérico.
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El ID debe ser un entero positivo.");
        // Long admite null: lo usamos para indicar al llamador que debe terminar sin seguir operando.
        return null;
    }

    /** Envía la misma petición a una JSP protegida dentro de WEB-INF. */
    private void mostrar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/" + vista).forward(request, response);
    }
}
