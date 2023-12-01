<%-- 
    Document   : realizarInvestimento
    Created on : 19 de out. de 2023, 21:07:54
    Author     : João
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
        <link href="./formsStyle.css" rel="stylesheet" />
        <title>Realizar Investimento</title>
    </head>
    <body style="background-color: #0156a5">
        <jsp:include page="HeaderComponent.html"/>
        <div class="container">
            <div class="row align-items-center vh-100">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Fundos de Investimento</h5>
                        <p class="card-text">Visualize fundos de investimento disponíveis no UFFBank</p>
                        <table class="table">
                            <thead>
                                <tr>
                                    <th scope="col">Cód.</th>
                                    <th scope="col">Nome</th>
                                    <th scope="col">Valor da Cota (R$)</th>
                                    <th scope="col">Valor Mín. (R$)</th>
                                    <th scope="col">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                java.util.List<entidade.Fundo> list = (java.util.List<entidade.Fundo>) request.getAttribute("fundos");
                                for(entidade.Fundo f : list){
                                %>
                                <tr>
                                    <th scope="row"><%=f.getId()%></th>
                                    <td><%=f.getNome()%></td>
                                    <td><%=f.getValorCota()%></td>
                                    <td><%=f.getValorMinimo()%></td>
                                    <td>
                                        <div class="btn-group">
                                            <button
                                                type="button"
                                                class="btn btn-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#exampleModalCenter"
                                                onclick="popularModal(<%=f.getId()%>,<%="'"+f.getNome()+"'"%>,<%=f.getValorMinimo()%>)">
                                                Aplicar
                                            </button>
                                        </div>
                                    </td>
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

        <!-- Modal -->
        <div class="modal fade" id="exampleModalCenter" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLongTitle">Realizar Aplicação</h5>
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <form method="POST" action="InvestimentoServlet?acao=APLICACAO">
                            <input type="hidden" id="idFundo" name="idFundo">
                            <div class="form-group row">
                                <label for="agencia">Fundo de Investimento</label>
                                <input type="text" disabled="disabled" class="form-control" id="fundoInput" placeholder="" value="Fundo Padrão">
                            </div>
                            <div class="form-group row">
                                <label for="conta">Valor (R$)</label>
                                <input type="number" class="form-control" id="valorInvestido" name="valorInvestido" step="0.01">
                            </div>
                            <button class="btn btn-primary w-100 py-2" style="margin-top: 10px" type="submit">Aplicar</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <div class="alert alert-success" role="alert" id="abc">
            This is a success alert—check it out!
        </div>

        <script>
            function popularModal(idFundo, nomeFundo, valorMinimo) {
                var inputNomeFundo = document.getElementById("fundoInput");
                var inputValor = document.getElementById("valorInvestido");
                var inputIdFundo = document.getElementById("idFundo");
                inputIdFundo.value = idFundo;
                inputValor.setAttribute("min", valorMinimo);
                inputValor.value = 0;
                inputNomeFundo.value = nomeFundo;
            }
            function teste() {
                document.getElementById("abc").alert();
            }

        </script>

    </body>
</html>
