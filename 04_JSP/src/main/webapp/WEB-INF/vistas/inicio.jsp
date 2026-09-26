<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${titulo}</title>
</head>
<body>
<main>
    <h1>${titulo}</h1>
    <p>Esta página ha sido preparada por un servlet y presentada por una JSP.</p>

    <h2>Datos enviados por el servlet</h2>
    <dl>
        <dt>Fecha y hora del servidor</dt>
        <dd>${fechaServidor}</dd>

        <dt>Número aleatorio</dt>
        <dd>${numeroAleatorio}</dd>

        <dt>Módulo</dt>
        <dd>${curso.nombre}</dd>

        <dt>Ciclo</dt>
        <dd>${curso.ciclo}</dd>
    </dl>

    <p><a href="${pageContext.request.contextPath}/inicio-forward">Generar otros valores</a></p>
    <p><a href="${pageContext.request.contextPath}/">Volver a la página de inicio</a></p>
</main>
</body>
</html>
