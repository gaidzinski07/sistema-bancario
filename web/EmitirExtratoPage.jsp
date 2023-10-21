<!DOCTYPE html>
<!--
Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Html.html to edit this template
-->
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
    <body class="gradientBackground">
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
            <div class="container-fluid">
                <a class="navbar-brand" href="index.html">
                    <img style="height: 25px" src="https://uploaddeimagens.com.br/images/004/641/545/original/Design_sem_nome.png?1697751630"/>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarScroll"
                        aria-controls="navbarScroll" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarScroll">
                    <ul class="navbar-nav me-auto my-2 my-lg-0 navbar-nav-scroll" style="--bs-scroll-height: 100px;">
                        <li class="nav-item">
                            <a class="nav-link" href="login.jsp">Fazer Login</a>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="navbarScrollingDropdown" role="button"
                               data-bs-toggle="dropdown" aria-expanded="false">
                                Operador/Adminstrador
                            </a>
                            <ul class="dropdown-menu" aria-labelledby="navbarScrollingDropdown">
                                <li><a class="dropdown-item" href="createUserPage.jsp">Criar Usuário</a></li>
                                <li><a class="dropdown-item" href="createBankAccountPage.jsp">Criar Conta Bancária</a></li>
                            </ul>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="navbarScrollingDropdown" role="button"
                               data-bs-toggle="dropdown" aria-expanded="false">
                                Área do Cliente
                            </a>
                            <ul class="dropdown-menu" aria-labelledby="navbarScrollingDropdown">
                                <li><a class="dropdown-item" href="transferencia.jsp">Realizar Transferência</a></li>
                                <li><a class="dropdown-item" href="realizarInvestimento.jsp">Realizar Investimentos</a></li>
                                <li><a class="dropdown-item" href="visualizarInvestimentos.jsp">Meus Investimentos</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
        <section class="vh-100 gradient-custom">
            <div class="container h-100">
                <div class="row d-flex justify-content-center align-items-center h-100">
                    <div class="col-lg-12 col-xl-11">
                        <div class="card text-black" style="border-radius: 25px;">
                            <div class="card-body p-md-5">
                                <div class="row justify-content-center">
                                    <img style="height: 110px; width: auto; padding-bottom: 10px" src="https://uploaddeimagens.com.br/images/004/641/545/original/Design_sem_nome.png?1697751630"/>
                                </div>
                                <form action="EmitirExtratoController" method="get">
                                    <button type="submit" class="btn btn-primary btn-lg" name="emitirExtrato" value="true">Emitir Extrato</button>
                                </form>
                                    <%
                                    HttpSession sessao = request.getSession() ;
                                Boolean showTable = (Boolean) sessao.getAttribute("mostrarTabela") ;
                                    out.print("Flag mostrar tabela <b>" + showTable + "</b> logado.<br>") ;
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
                                        <th scope="row">1</th>
                                        <td>Mark</td>
                                        <td>Otto</td>
                                        <td>@mdo</td>
                                      </tr>
                                      <tr>
                                        <th scope="row">2</th>
                                        <td>Jacob</td>
                                        <td>Thornton</td>
                                        <td>@fat</td>
                                      </tr>
                                      <tr>
                                        <th scope="row">3</th>
                                        <td colspan="2">Larry the Bird</td>
                                        <td>@twitter</td>
                                      </tr>
                                    </tbody>
                                  
                                    </table>
                                </div>
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