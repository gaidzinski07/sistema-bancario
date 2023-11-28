<%-- 
    Document   : showMessage
    Created on : Nov 28, 2023, 12:01:05 AM
    Author     : lucas
--%>
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
    <body>
        <div class="container">
            <div class="mt-5">

                <%  String msgOperacaoRealizada = (String) request.getAttribute("msgOperacaoRealizada");
                    String link = (String) request.getAttribute("link");
                    if ((msgOperacaoRealizada != null) && (!msgOperacaoRealizada.isEmpty())) {%>

                <h2><%= msgOperacaoRealizada%></h2>
                <div class="alert alert-success" role="alert">
                    Usuário criado com sucesso faça <a href="http://localhost:8080/sistema-bancario/login.jsp" class="alert-link">login</a> para liberar toda as funcionalidades.
                </div>

                <% }%>

                <a href="<%=link%>" class="btn btn-primary">Ínicio</a>
            </div>
        </div>

        <script src="http://localhost:8080/aplicacaoMVC/views/bootstrap/bootstrap.bundle.min.js"></script>

    </body>
</html>