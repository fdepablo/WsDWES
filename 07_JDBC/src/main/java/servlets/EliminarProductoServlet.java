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

/** Elimina por POST para que una simple consulta GET no borre datos. */
@WebServlet("/productos/eliminar")
public class EliminarProductoServlet extends HttpServlet {
    // La base ya se ha preparado al pasar primero por el listado.
    private final ConexionBD baseDatos = new ConexionBDH2();
    private final ProductoDAO productos = new ProductoDAO(baseDatos);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = Long.parseLong(request.getParameter("id"));
        try {
            if (!productos.eliminar(id)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "El producto no existe.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/productos");
        } catch (SQLException e) {
            throw new ServletException("No se pudo eliminar el producto.", e);
        }
    }
}
