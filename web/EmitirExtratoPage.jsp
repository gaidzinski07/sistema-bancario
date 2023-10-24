<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="Objects.Transacao" %>
<%@ page import="java.util.ArrayList" %>

<!DOCTYPE html>
<html>
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <link href="./formsStyle.css" rel="stylesheet" />

    <title>Emitir Saldo</title>
</head>
<jsp:include page="HeaderComponent.html"/>
<body class="gradientBackground">
    <section class="vh-100 gradient-custom">
        <div class="container h-100">
            <div class="row d-flex justify-content-center align-items-center h-100">
                <div class="col-lg-12 col-xl-11">
                    <div class="card text-black" style="border-radius: 25px;">
                        <div class="card-body p-md-5">
                            <div class="row justify-content-center">
                                <img style="height: 110px; width: auto; padding-bottom: 10px"
                                     src="https://uploaddeimagens.com.br/images/004/641/545/original/Design_sem_nome.png?1697751630"/>
                                <form action="EmitirExtratoController" method="get">
                                    <div class="d-flex justify-content-center">
                                        <button type="submit" class="btn btn-primary btn-lg" name="emitirExtrato" value="true">Emitir Extrato</button>
                                    </div>
                                </form>
                            </div>
                            <%
                                HttpSession sessao = request.getSession();
                                List<Transacao> transacoes = (List<Transacao>) sessao.getAttribute("transacoes");
                                if (transacoes != null && !transacoes.isEmpty()) {
                            %>
                            <div class="extract">
                                <table class="table table-sm">
                                    <thead>
                                        <tr>
                                            <th scope="col">ID Da Transação</th>
                                            <th scope="col">Doador/Favorecido</th>
                                            <th scope="col">Entrada</th>
                                            <th scope="col">Saída</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            for (Transacao transacao : transacoes) {
                                        %>
                                        <tr>
                                            <th scope="row"><%= transacao.getId() %></th>
                                            <td><%= transacao.getDoadorFavorecido() %></td>
                                            <td><%= transacao.getEntrada() %></td>
                                            <td><%= transacao.getSaida() %></td>
                                        </tr>
                                        <%
                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                            <%
                                } else {
                            %>
                            <div class="extract">
                                <table class="table table-sm">
                                    <thead>
                                        <tr>
                                            <th scope="col">ID Da Transação</th>
                                            <th scope="col">Doador/Favorecido</th>
                                            <th scope="col">Entrada</th>
                                            <th scope="col">Saída</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td colspan="4">Nenhuma transação disponível.</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                            <%
                                }
                            %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
            crossorigin="anonymous"></script>
</body>
</html>
