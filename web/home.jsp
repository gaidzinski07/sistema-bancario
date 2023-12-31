<%-- 
    Document   : home
    Created on : Nov 15, 2023, 11:22:59 PM
    Author     : lucas
--%>

<%@page import="entidade.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <link href="./formsStyle.css" rel="stylesheet" />

        <title>Início</title>
    </head>
    <%
        String showToast = request.getParameter("showToast");
        if ("true".equals(showToast)) {
    %>
    <div style="margin-top: 10px;margin-left: 10px;margin-right: 10px;">
        <div class="alert alert-danger" role="alert">
            É preciso fazer <a href="/sistema-bancario/login.jsp" class="alert-link">Login</a> para acessar esta função
        </div> 
    </div>

    <%
        }
    %>

    <%
            String msgSucess = (String) request.getAttribute("userLogged");
            if ((msgSucess != null) && (!msgSucess.isEmpty())) {%>

    <div style="margin-top: 10px;margin-left: 10px;margin-right: 10px;">
        <div class="alert alert-success" role="alert">
            <%= msgSucess%>
        </div>
    </div>
    <% }%>
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
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="navbarScrollingDropdown" role="button"
                               data-bs-toggle="dropdown" aria-expanded="false">
                                Login
                            </a>
                            <ul class="dropdown-menu" aria-labelledby="navbarScrollingDropdown">
                                <li><a class="dropdown-item" href="LoginController">Logar Adminstrador</a></li>
                                <li><a class="dropdown-item" href="ClienteController">Logar Cliente</a></li>
                            </ul>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="navbarScrollingDropdown" role="button"
                               data-bs-toggle="dropdown" aria-expanded="false">
                                Administrador
                            </a>
                            <ul class="dropdown-menu" aria-labelledby="navbarScrollingDropdown">
                                <li><a class="dropdown-item" href="CreateUser">Criar Usuário</a></li>
                                <li><a class="dropdown-item" href="admin/createBankAccountPage.jsp">Criar Conta Bancária</a></li>
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
                                <li><a class="dropdown-item" href="realizarsaque.jsp">Realizar Saque</a></li>
                                <li><a class="dropdown-item" href="FundoServlet">Realizar Investimentos</a></li>
                                <li><a class="dropdown-item" href="InvestimentoServlet">Meus Investimentos</a></li>
                                <li><a class="dropdown-item" href="EmitirExtratoController">Emitir Extrato</a></li>
                                <li><a class="dropdown-item" href="EmitirSaldoController">Emitir Saldo</a></li>
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="login.jsp">Fazer Logout</a>
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
                                    <div class="accordion" id="accordionExample">
                                        <div class="accordion-item">
                                            <h2 class="accordion-header" id="headingOne">
                                                <button class="accordion-button" type="button" data-bs-toggle="collapse"
                                                        data-bs-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                                                    Sobre nós
                                                </button>
                                            </h2>
                                            <div id="collapseOne" class="accordion-collapse collapse show" aria-labelledby="headingOne"
                                                 data-bs-parent="#accordionExample">
                                                <div class="accordion-body">
                                                    <p> Olá e bem-vindo ao UFFBank! É um prazer tê-lo aqui. Obrigado por visitar nossa página
                                                        inicial. Estamos muito animados em compartilhar com vocês os detalhes do nosso banco virtual.
                                                        O UFFBank é um banco virtual moderno e inovador que visa fornecer serviços bancários de alta
                                                        qualidade para nossos clientes. Nós nos esforçamos para fornecer uma experiência bancária
                                                        fácil e sem complicações para nossos clientes. Nossa equipe de especialistas em serviços
                                                        bancários está sempre pronta para ajudá-lo com suas necessidades bancárias. Sinta-se à vontade
                                                        para explorar nosso site e descobrir mais sobre nossos serviços bancários. Se você tiver
                                                        alguma dúvida, não hesite em entrar em contato conosco. Estamos ansiosos para ouvir de você!
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="accordion-item">
                                            <h2 class="accordion-header" id="headingTwo">
                                                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                                        data-bs-target="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                                                    Links Úteis
                                                </button>
                                            </h2>
                                            <div id="collapseTwo" class="accordion-collapse collapse" aria-labelledby="headingTwo"
                                                 data-bs-parent="#accordionExample">
                                                <div class="accordion-body">
                                                    <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                                                       href="https://github.com/gaidzinski07/sistema-bancario">
                                                        Repositorio GitHub
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="accordion-item">
                                            <h2 class="accordion-header" id="headingThree">
                                                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                                        data-bs-target="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                                                    Desenvolvedores Responsáveis
                                                </button>
                                            </h2>
                                            <div id="collapseThree" class="accordion-collapse collapse" aria-labelledby="headingThree"
                                                 data-bs-parent="#accordionExample">
                                                <div class="accordion-body">
                                                    <ul>
                                                        <li> 
                                                            <a
                                                                class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                                                                href="https://www.linkedin.com/in/lucas-magalhaes-2103/">
                                                                Lucas Magalhães
                                                            </a>
                                                        </li>
                                                        <li> 
                                                            <a
                                                                class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                                                                href="">
                                                                João Gaidzinski
                                                            </a>
                                                        </li>
                                                        <li> 
                                                            <a
                                                                class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                                                                href="">
                                                                Luiz Gustavo
                                                            </a>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
                crossorigin="anonymous">

        </script>
</body>
</html>