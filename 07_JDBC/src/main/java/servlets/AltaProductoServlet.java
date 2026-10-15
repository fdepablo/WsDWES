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

/** Muestra el formulario de alta con GET e inserta el producto con POST. */
@WebServlet("/productos/nuevo")
public class AltaProductoServlet extends HttpServlet {
    // La base ya se ha preparado al pasar primero por el listado.
    private final ConexionBD baseDatos = new ConexionBDH2();
    private final ProductoDAO productos = new ProductoDAO(baseDatos);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Sin atributo producto, la JSP presenta un alta con los campos vacíos.
        request.getRequestDispatcher("/WEB-INF/vistas/alta_editar.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String nombre = request.getParameter("nombre");
        // Convertimos el texto a decimal. Aquí no aplicamos validaciones de negocio.
        Double precio = Double.parseDouble(request.getParameter("precio"));
        try {
            productos.insertar(nombre, precio);
            // POST/Redirect/GET: actualizar el listado no repite el INSERT.
            response.sendRedirect(request.getContextPath() + "/productos");
        } catch (SQLException e) {
            throw new ServletException("No se pudo insertar el producto.", e);
        }
    }
}
