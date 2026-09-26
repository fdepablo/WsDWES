<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Persona</title></head>
<body>
<main>
    <h1><c:choose><c:when test="${requestScope.accion == 'editar'}">Editar persona</c:when><c:otherwise>Nueva persona</c:otherwise></c:choose></h1>
    <c:if test="${not empty requestScope.error}"><p role="alert"><c:out value="${requestScope.error}" /></p></c:if>
    <form action="${pageContext.request.contextPath}/personas/${requestScope.accion}" method="post">
        <c:if test="${requestScope.accion == 'editar'}"><input type="hidden" name="id" value="${requestScope.idFormulario}"></c:if>
        <p><label for="nombre">Nombre</label><br>
            <input id="nombre" name="nombre" maxlength="80" required value="<c:out value='${requestScope.nombreFormulario}' />"></p>
        <p><label for="edad">Edad</label><br>
            <input id="edad" name="edad" type="number" min="0" max="150" step="1" required value="<c:out value='${requestScope.edadFormulario}' />"></p>
        <p><label for="peso">Peso (kg)</label><br>
            <input id="peso" name="peso" type="number" min="0.01" step="any" required value="<c:out value='${requestScope.pesoFormulario}' />"></p>
        <button type="submit">Guardar</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/personas">Volver al listado</a></p>
</main>
</body>
</html>
