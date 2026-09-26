package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.entidad.Persona;
import modelo.servicio.PersonaServicio;

import java.io.IOException;

/** Traduce peticiones HTTP en llamadas al servicio y selecciona la JSP. */
@WebServlet("/personas/*")
public class PersonaServlet extends HttpServlet {
    private PersonaServicio servicio;

    @Override
    public void init() throws ServletException {
        try {
            servicio = new PersonaServicio();
        } catch (RuntimeException e) {
            throw new ServletException("No se pudo iniciar JPA.", e);
        }
    }

    @Override
    public void destroy() {
        if (servicio != null) servicio.close();
    }

    /** GET solo consulta y muestra; no modifica la base de datos. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ruta = request.getPathInfo();
        if (ruta == null || "/".equals(ruta)) {
            request.setAttribute("personas", servicio.listar());
            mostrar(request, response, "listado.jsp");
        } else if ("/nuevo".equals(ruta)) {
            request.setAttribute("accion", "nuevo");
            mostrar(request, response, "formulario.jsp");
        } else if ("/editar".equals(ruta)) {
            Long id = leerId(request, response);
            if (id == null) return;
            Persona persona = servicio.buscar(id);
            if (persona == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("accion", "editar");
            request.setAttribute("idFormulario", persona.getId());
            request.setAttribute("nombreFormulario", persona.getNombre());
            request.setAttribute("edadFormulario", persona.getEdad());
            request.setAttribute("pesoFormulario", persona.getPeso());
            mostrar(request, response, "formulario.jsp");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /** POST escribe y redirige para evitar repetir la operación al recargar. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String ruta = request.getPathInfo();
        if ("/nuevo".equals(ruta) || "/editar".equals(ruta)) {
            guardar(request, response, ruta);
        } else if ("/eliminar".equals(ruta)) {
            Long id = leerId(request, response);
            if (id == null) return;
            if (!servicio.eliminar(id)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/personas");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        boolean edicion = "/editar".equals(ruta);
        Long id = edicion ? leerId(request, response) : null;
        if (edicion && id == null) return;
        String nombre = request.getParameter("nombre");
        String textoEdad = request.getParameter("edad");
        String textoPeso = request.getParameter("peso");
        try {
            int edad = Integer.parseInt(textoEdad);
            double peso = Double.parseDouble(textoPeso);
            if (edicion) {
                if (!servicio.actualizar(id, nombre, edad, peso)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                servicio.crear(nombre, edad, peso);
            }
            response.sendRedirect(request.getContextPath() + "/personas");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", "Revisa el nombre, la edad y el peso.");
            request.setAttribute("accion", edicion ? "editar" : "nuevo");
            request.setAttribute("idFormulario", id);
            request.setAttribute("nombreFormulario", nombre);
            request.setAttribute("edadFormulario", textoEdad);
            request.setAttribute("pesoFormulario", textoPeso);
            mostrar(request, response, "formulario.jsp");
        }
    }

    private Long leerId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            long id = Long.parseLong(request.getParameter("id"));
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {
            // Un formulario manipulado puede enviar un ID inválido.
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El ID debe ser un entero positivo.");
        return null;
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/" + vista).forward(request, response);
    }
}
