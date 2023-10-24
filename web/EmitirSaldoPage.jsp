<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<html>

    <head>
        
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <link href="./formsStyle.css" rel="stylesheet" />

        <title>Emitir Saldo</title>
    </head>
    <jsp:include page="HeaderComponent.html" />
    <body class="gradientBackground">
        <section class="vh-100 gradient-custom">
            <div class="container h-100">
                <div class="row d-flex justify-content-center align-items-center h-100">
                    <div class="col-lg-12 col-xl-11">
                        <div class="card text-black" style="border-radius: 25px;">
                            <div class="card-body p-md-5">
                                <div class="row justify-content-center">
                                    <img style="height: 110px; width: auto; padding-bottom: 10px"
                                         src="https://uploaddeimagens.com.br/images/004/641/545/original/Design_sem_nome.png?1697751630" />
                                    <form action="EmitirSaldoController" method="get">
                                        <div class="d-flex justify-content-center"> 
                                            <button type="submit" class="btn btn-primary btn-lg" name="emitirSaldo" value="true">Emitir Saldo</button>
                                        </div>
                                    </form>
                                </div>
                                <% HttpSession sessao = request.getSession(); Boolean showTable = (Boolean) sessao.getAttribute("mostraSaldo"); if (showTable) { %>
                                <section>
                                    <div class="container py-5 h-100">
                                        <div class="row d-flex justify-content-center align-items-center h-100">
                                            <div>
                                                <div class="card mb-3" style="border-radius: .5rem;">
                                                    <div class="row g-0">
                                                        <div class="col-md-4 master-card-style text-center text-white"
                                                             style="border-top-left-radius: .5rem; border-bottom-left-radius: .5rem;">
                                                            <img src="https://upload.wikimedia.org/wikipedia/commons/a/a4/Mastercard_2019_logo.svg"
                                                                 alt="Avatar" class="img-fluid my-5 px-4" />
                                                            <i class="far fa-edit mb-5"></i>
                                                        </div>
                                                        <div class="col-md-8">
                                                            <div class="card-body p-4">
                                                                <h6>Informações da Conta</h6>
                                                                <hr class="mt-0 mb-4">
                                                                <div class="row pt-1">
                                                                    <div class="col-6 mb-3">
                                                                        <h6>Agência</h6>
                                                                        <p>0123</p>
                                                                    </div>
                                                                    <div class="col-6 mb-3">
                                                                        <h6>Número da Conta</h6>
                                                                        <p>1234 5678 9843 1253</p>
                                                                    </div>
                                                                </div>
                                                                <h6>Saldo da Conta</h6>
                                                                <div><b class="text-success">R$85,00</b></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </section>
                                <% } %>
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
