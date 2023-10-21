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
                                <li><a class="dropdown-item" href="realizardeposito.jsp">Realizar Depósito</a></li>
                                <li><a class="dropdown-item" href="realizarInvestimento.jsp">Realizar Investimentos</a></li>
                                <li><a class="dropdown-item" href="visualizarInvestimentos.jsp">Meus Investimentos</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
        <h1>Fundos de Investimento</h1>
        <div class="container">
            
            
            <div class="row align-items-center vh-100">
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
                        <tr>
                            <th scope="row">0001</th>
                            <td>Lojas Americanas</td>
                            <td>1,99</td>
                            <td>10,00</td>
                            <td>
                                <div class="btn-group">
                                    <button
                                        type="button"
                                        class="btn btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#exampleModalCenter"
                                        onclick="popularModal('0001 - Lojas Americanas', 10)">
                                        Aplicar
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th scope="row">0002</th>
                            <td>777 Partners Investimentos Desportivos</td>
                            <td>7,77</td>
                            <td>77</td>
                            <td>
                                <div class="btn-group">
                                    <button
                                        type="button"
                                        class="btn btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#exampleModalCenter"
                                        onclick="popularModal('0002 - 777 Partners Investimentos Desportivos', 77)">
                                        Aplicar
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th scope="row">0003</th>
                            <td>LG Tecnologia</td>
                            <td>88,99</td>
                            <td>180</td>
                            <td>
                                <div class="btn-group">
                                    <button
                                        type="button"
                                        class="btn btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#exampleModalCenter"
                                        onclick="popularModal('0003 - LG Tecnologia', 180)">
                                        Aplicar
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
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
                        <form>
                            <div class="form-group row">
                                <label for="agencia">Fundo de Investimento</label>
                                <input type="text" disabled="disabled" class="form-control" id="fundoInput" placeholder="" value="Fundo Padrão">
                            </div>
                            <div class="form-group row">
                                <label for="conta">Valor (R$)</label>
                                <input type="number" class="form-control" id="valorInvestido">
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
            function popularModal(nomeFundo, valorMinimo) {
                var inputNomeFundo = document.getElementById("fundoInput");
                var inputValor = document.getElementById("valorInvestido");
                inputValor.setAttribute("min", valorMinimo);
                inputValor.value = 0;
                inputNomeFundo.value = nomeFundo;
            }
            function teste(){
                document.getElementById("abc").alert();
            }
            
        </script>

    </body>
</html>
