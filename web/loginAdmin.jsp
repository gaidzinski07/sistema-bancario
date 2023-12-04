
<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
        <title>Login UFFBank</title>
    </head>
    <jsp:include page="HeaderComponent.html"/>
    <%
        String msgError = (String) request.getAttribute("msgError");
        if ((msgError != null) && (!msgError.isEmpty())) {%>

    <div style="margin-top: 10px;margin-left: 10px;margin-right: 10px;">
        <div class="alert alert-danger" role="alert">
            <%= msgError%>
        </div>
    </div>
    <% }%>
    
    <body style="background-color: #0156a5">
        <div class="container">
            <div class="row align-items-center vh-100">
                <div class="col-6 mx-auto">
                    <div class="card shadow border">
                        <div class="card-body d-flex flex-column align-items-center">
                            <div class="col d-flex justify-content-center">
                                <img class="mb-4" src="https://uploaddeimagens.com.br/images/004/641/545/original/Design_sem_nome.png?1697751630" alt="Logo do banco" height="57">
                            </div>
                            <h1>Entrar</h1>
                            <form method="post">
                                <div class="form-group row">
                                    <label for="CPF">CPF</label>
                                    <input type="text" class="form-control" name="cpfInput" placeholder="">
                                </div>
                                <div class="form-group row">
                                    <label for="senha">Senha</label>
                                    <input type="password" class="form-control" name="senhaInput" placeholder="">
                                </div>

                                <button class="btn btn-primary w-100 py-2" style="margin-top: 10px" type="submit">Fazer Login</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
