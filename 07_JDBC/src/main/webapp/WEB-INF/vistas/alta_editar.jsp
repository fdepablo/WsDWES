<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Producto</title></head>
<body>
<main>
    <%-- Sin producto estamos dando de alta; con producto estamos editando. --%>
    <h1>${empty requestScope.producto ? 'Nuevo producto' : 'Editar producto'}</h1>
    <%-- La misma condición elige el servlet que recibirá el POST. --%>
    <form action="${pageContext.request.contextPath}/productos/${empty requestScope.producto ? 'nuevo' : 'editar'}" method="post">
        <c:if test="${not empty requestScope.producto}">
            <%-- Solo la edición necesita enviar el ID de la fila que se actualizará. --%>
            <input type="hidden" name="id" value="${requestScope.producto.id}">
        </c:if>
        <p><label for="nombre">Nombre</label><br>
            <input id="nombre" name="nombre" maxlength="80" required value="<c:out value='${requestScope.producto.nombre}' />"></p>
        <p><label for="precio">Precio (€)</label><br>
            <input id="precio" name="precio" type="number" min="0" max="99999999.99" step="0.01" required value="<c:out value='${requestScope.precioFormulario}' />"></p>
        <button type="submit">Guardar</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/productos">Volver al listado</a></p>
</main>
</body>
</html>
