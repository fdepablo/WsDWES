package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.servicio.ProductoServicio;
import modelo.entidad.Producto;

import java.io.IOException;
import java.sql.SQLException;

/** Controlador MVC: interpreta la petición, consulta el modelo y selecciona la vista. */
@WebServlet("/productos/*")
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
        try {
            servicio = new ProductoServicio();
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
            String ruta = request.getPathInfo();
            if (ruta == null || "/".equals(ruta)) {
                request.setAttribute("productos", servicio.listar());
                mostrar(request, response, "listado.jsp");
            } else if ("/nuevo".equals(ruta)) {
                request.setAttribute("accion", "nuevo");
                mostrar(request, response, "formulario.jsp");
            } else if ("/editar".equals(ruta)) {
                Long id = leerId(request, response);
                if (id == null) return;
                Producto producto = servicio.buscar(id);
                if (producto == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("idFormulario", producto.getId());
                request.setAttribute("nombreFormulario", producto.getNombre());
                request.setAttribute("precioFormulario", producto.getPrecio().toPlainString());
                request.setAttribute("accion", "editar");
                mostrar(request, response, "formulario.jsp");
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
        request.setCharacterEncoding("UTF-8");
        String ruta = request.getPathInfo();
        try {
            if ("/nuevo".equals(ruta) || "/editar".equals(ruta)) {
                guardar(request, response, ruta);
            } else if ("/eliminar".equals(ruta)) {
                Long id = leerId(request, response);
                if (id == null) return;
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
        boolean edicion = "/editar".equals(ruta);
        Long id = edicion ? leerId(request, response) : null;
        if (edicion && id == null) return;
        String nombre = request.getParameter("nombre");
        String textoPrecio = request.getParameter("precio");
        try {
            boolean guardado;
            if (edicion) {
                guardado = servicio.actualizar(id, nombre, textoPrecio);
            } else {
                servicio.crear(nombre, textoPrecio);
                guardado = true;
            }
            if (!guardado) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", e.getMessage());
            request.setAttribute("nombreFormulario", nombre);
            request.setAttribute("precioFormulario", textoPrecio);
            request.setAttribute("accion", edicion ? "editar" : "nuevo");
            if (edicion) request.setAttribute("idFormulario", id);
            mostrar(request, response, "formulario.jsp");
            return;
        }
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
        return null;
    }

    /** Envía la misma petición a una JSP protegida dentro de WEB-INF. */
    private void mostrar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/" + vista).forward(request, response);
    }
}
