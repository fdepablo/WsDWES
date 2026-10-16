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

/** Consulta todos los productos y muestra la tabla. */
// loadOnStartup = 1, Inicializa el servlet al desplegar la aplicación, sin esperar a la primera petición.
// Los Servlets por definición (al igual que los JSP), tienen carga perezosa o "lazy", por lo que el objeto
// no se inicializa hasta que sea invocado a su URL por primera vez. Con este parámetro indicamos que la
// carga va a ser ansiosa o "eagle".
// El "1" indica que la posición de inicialización en caso de que haya varios Servlets con este parámetro.
@WebServlet(value = {"/productos", "/productos/"}, loadOnStartup = 1)
public class ListadoProductosServlet extends HttpServlet {
    private ProductoDAO productos;

    /** Prepara la base y el DAO una sola vez al crear este servlet. */
    @Override
    public void init() throws ServletException {
        // Las rutas relativas de H2, como ./datos, parten de este directorio.
        System.out.println("07_JDBC - Directorio de trabajo: " + System.getProperty("user.dir"));
        // Para usar MariaDB, cambia aquí la implementación y su import en los cuatro servlets.
        ConexionBD baseDatos = new ConexionBDH2();
        try {
            baseDatos.prepararBaseDatos();
            productos = new ProductoDAO(baseDatos);
        } catch (SQLException e) {
            throw new ServletException("No se pudo preparar la base de datos.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // La lista solo se necesita en esta respuesta: la pasamos en la petición.
            request.setAttribute("productos", productos.listar());
            request.getRequestDispatcher("/WEB-INF/vistas/listado.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("No se pudieron consultar los productos.", e);
        }
    }
}
