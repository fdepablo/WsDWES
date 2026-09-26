<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resultado de la redirección</title>
</head>
<body>
<main>
    <h1>Resultado de la redirección</h1>
    <p>Esta JSP está fuera de WEB-INF para que el navegador pueda solicitarla mediante la nueva URL.</p>
    <p>El servlet guardó el atributo de petición "mensaje" antes de redirigir.</p>
    <p>Valor disponible aquí: ${empty requestScope.mensaje ? 'Ninguno: esta es otra petición.' : requestScope.mensaje}</p>
    <p><a href="${pageContext.request.contextPath}/">Volver a la página de inicio</a></p>
</main>
</body>
</html>
