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
        <jsp:include page="HeaderComponent.html"/>
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
