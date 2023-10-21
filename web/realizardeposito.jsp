<%-- 
    Document   : fazerpix
    Created on : 21 de out. de 2023, 12:11:23
    Author     : fifap
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Depósito</title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <link href="./formsStyle.css" rel="stylesheet" />
    </head>
    <body>
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
        
   
        <form>
            <section class="vh-100 gradient-custom">
                 <h1>Depósito no caixa eletrônico</h1>
            <div class="container h-100">
                <div class="row d-flex justify-content-center align-items-center h-100">
                    <div class="col-lg-12 col-xl-11">
                        
                        <div class="card text-black" style="border-radius: 0px;">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault1">
                                <label class="form-check-label" for="flexRadioDefault1">
                                    Depósito em cheque
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault2" checked>
                                <label class="form-check-label" for="flexRadioDefault2">
                                    Depósito em dinheiro
                                </label>
                            </div>
                            <h6>Não é possível depositar Cheque + Dinheiro em espécie. Favor realizar depósitos separados.</h6>
                            <div class="mb-5">
                                <label for="valordeposito" class="form-label">Valor sendo depositado</label>
                                    <input type="valor" class="form-control" id="inputvalor" aria-describedby="valorhelp">
                                        <div id="valorhelp" class="form-text">Sujeito a a verificação dos valores no envelope.</div>
                                        </div>
                            <div class="mb-5">
                                <label for="senha" class="form-label">Digite sua Senha</label>
                                <input type="password" class="form-control" id="InputPassword">
                            </div>
                            <div class="mb-3 form-check">
                                <input type="checkbox" class="form-check-input" id="exampleCheck1">
                                <label class="form-check-label" for="exampleCheck1">Confirmo que as informações prestadas são verdadeiras e que se houver valores distintos o depósito não será realizado.</label>
                            </div>
                                <button type="submit" class="btn btn-primary">Enviar</button>
        </form>
</div>
                    </div>
                    </div>
                </div>
    </body>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
        crossorigin="anonymous"></script>
</html>
