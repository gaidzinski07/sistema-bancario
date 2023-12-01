<%-- 
    Document   : visualizarInvestimentos
    Created on : 19 de out. de 2023, 23:41:05
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
        <title>Meus Investimentos</title>
    </head>
    <body style="background-color: #0156a5">
        <jsp:include page="HeaderComponent.html"/>
        <div class="container">
            <div class="row align-items-center vh-100">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Meus Investimentos</h5>
                        <p class="card-text">Visualize e gerencie seus investimentos</p>
                        <table class="table">
                            <thead>
                                <tr>
                                    <th scope="col">Cód.</th>
                                    <th scope="col">Nome</th>
                                    <th scope="col">Data Inicial</th>
                                    <th scope="col">Qtd. Cotas</th>
                                    <th scope="col">Valor Inicial (R$)</th>
                                    <th scope="col">Valor Atual (R$)</th>
                                    <th scope="col"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    java.util.List<dto.InvestimentoDTO> list = (java.util.List<dto.InvestimentoDTO>) request.getAttribute("investimentos");
                                    for(dto.InvestimentoDTO i : list){
                                %>
                                <tr>
                                    <th scope="row"><%= i.getInvestimento().getIdFundo() %></th>
                                    <td><%= i.getFundo().getNome() %></td>
                                    <td><%= i.getInvestimento().getData().toString() %></td>
                                    <td><%= i.getInvestimento().getQtdCotas() %></td>
                                    <td><%= i.getInvestimento().getVrInvestido() %></td>
                                    <td style="color: <%=i.getRendimentos() >= 1 ? "green" : "red"%>"><b><%= i.getInvestimento().getVrInvestido() * i.getRendimentos() %></b></td>
                                    <td>
                                        <div class="btn-group">
                                            <button
                                                type="button"
                                                class="btn btn-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#exampleModalCenter"
                                                onclick="popularModal(<%=i.getInvestimento().getId()%>,<%= "'"+i.getInvestimento().getIdFundo()+" - "+i.getFundo().getNome()+"'"  %>, <%= i.getInvestimento().getVrInvestido() * i.getRendimentos() %>)">
                                                Resgatar
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
                        <h5 class="modal-title" id="exampleModalLongTitle">Resgatar valor de aplicação</h5>
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <form method="POST" action="InvestimentoServlet?acao=RESGATE">
                            <input type="hidden" name="idInvestimento" id="idInvestimento" value="-1">
                            <div class="form-group row">
                                <label for="agencia">Fundo de Investimento</label>
                                <input type="text" readonly="readonly" class="form-control" id="fundoInput" name="fundoInput">
                            </div>
                            <div class="form-group row">
                                <label for="conta">Valor (R$)</label>
                                <input type="number" class="form-control" id="valorInvestido" name="valorInvestido" readonly="readonly">
                            </div>
                            <button class="btn btn-primary w-100 py-2" style="margin-top: 10px" type="submit">Resgatar</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>


        <script>
            function popularModal(idInvestimento, nomeFundo, valorMaximo) {
                var inputNomeFundo = document.getElementById("fundoInput");
                var inputValor = document.getElementById("valorInvestido");
                var inputIdInvestimento = document.getElementById("idInvestimento");
                inputIdInvestimento.value = idInvestimento;
                inputValor.setAttribute("max", valorMaximo);
                inputValor.value = valorMaximo;
                inputNomeFundo.value = nomeFundo;
            }
        </script>
    </body>
</html>
