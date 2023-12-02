<%-- 
    Document   : extrato
    Created on : 1 de dez. de 2023, 12:55:08
    Author     : João
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <title>JSP Page</title>
    </head>
    <body style="background-color: #0156a5">
        <jsp:include page="HeaderComponent.html"/>
        <div class="container">
            <div class="row align-items-center" style="height: calc(100vh - 57px) !important;">
                <div class="col-6 mx-auto">
                    <div class="card shadow border">
                        <div class="card-body d-flex flex-column align-items-center">
                            <table class="table table-sm">
                                <thead>
                                    <tr>
                                        <th scope="col">Id</th>
                                        <th scope="col">Tipo</th>
                                        <th scope ="col">Nome</th>
                                        <th scope="col">Valor (R$)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        java.util.List<dto.TransacaoDTO> list = (java.util.List<dto.TransacaoDTO>) request.getAttribute("transacoes");
                                        for (dto.TransacaoDTO t : list) {
                                    %>
                                    <tr>
                                        <th scope="row"><%= t.getId()%></th>
                                        <td><%= t.getTipo()%></td>
                                        <td><%=t.getNome()%></td>
                                        <td><%= t.getValor()%></td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
