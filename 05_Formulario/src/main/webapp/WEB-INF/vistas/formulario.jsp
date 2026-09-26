<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulario de registro</title>
</head>
<body>
<main>
    <h1>Registro de usuario</h1>
    <p>Completa los datos. Los campos marcados con * son obligatorios.</p>

    <form action="${pageContext.request.contextPath}/registro" method="post">
        <p>
            <label for="nombre">Nombre *</label><br>
            <input id="nombre" name="nombre" type="text" maxlength="60" required>
        </p>

        <p>
            <label for="email">Correo electrónico *</label><br>
            <input id="email" name="email" type="email" maxlength="100" required>
        </p>

        <p>
            <label for="password">Contraseña *</label><br>
            <input id="password" name="password" type="password" minlength="8" maxlength="72" required>
        </p>

        <fieldset>
            <legend>Intereses (puedes marcar varios)</legend>
            <label><input type="checkbox" name="intereses" value="programacion"> Programación</label><br>
            <label><input type="checkbox" name="intereses" value="bases-datos"> Bases de datos</label><br>
            <label><input type="checkbox" name="intereses" value="diseno-web"> Diseño web</label>
        </fieldset>

        <fieldset>
            <legend>Tipo de cuenta *</legend>
            <label><input type="radio" name="tipoCuenta" value="estudiante" required> Estudiante</label><br>
            <label><input type="radio" name="tipoCuenta" value="docente"> Docente</label>
        </fieldset>

        <p><button type="submit">Crear cuenta</button></p>
    </form>
</main>
</body>
</html>
