package servlets;

import bbdd.ConexionBDH2;
import bbdd.ProductoDAO;
import bbdd.interfaces.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import modelo.Producto;

/** GET carga un producto en el formulario; POST guarda los cambios de esa fila. */
@WebServlet("/productos/editar")
public class EditarProductoServlet extends HttpServlet {
    // La base ya se ha preparado al pasar primero por el listado.
    private final ConexionBD baseDatos = new ConexionBDH2();
    private final ProductoDAO productos = new ProductoDAO(baseDatos);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El GET recibe el ID enviado por el botón Editar; todavía no modifica nada.
        long id = Long.parseLong(request.getParameter("id"));
        try {
            Producto producto = productos.buscar(id);
            if (producto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "El producto no existe.");
                return;
            }
            // La presencia del producto indica a la JSP compartida que estamos editando.
            request.setAttribute("producto", producto);
            // El input numérico necesita el punto decimal, no un precio con formato monetario.
            request.setAttribute("precioFormulario", producto.getPrecio());
            request.getRequestDispatcher("/WEB-INF/vistas/alta_editar.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("No se pudo consultar el producto.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // El ID oculto identifica la fila; nombre y precio contienen los cambios.
        long id = Long.parseLong(request.getParameter("id"));
        String nombre = request.getParameter("nombre");
        Double precio = Double.parseDouble(request.getParameter("precio"));
        try {
            // Cero filas afectadas significa que el producto ya no existe.
            if (!productos.actualizar(id, nombre, precio)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "El producto no existe.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/productos");
        } catch (SQLException e) {
            throw new ServletException("No se pudo actualizar el producto.", e);
        }
    }
}
