<%-- 
    Document   : loginJSP
    Created on : 17 de out. de 2023, 23:22:53
    Author     : João
--%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
        <title>JSP Page</title>
    </head>
    <body>
        <div class="container">
            <div class="row align-items-center vh-100">
                <div class="col-6 mx-auto">
                    <div class="card shadow border">
                        <div class="card-body d-flex flex-column align-items-center">
                            <div class="col d-flex justify-content-center">
                                <img class="mb-4" src="https://i.imgur.com/kBOkecM.png" alt="" height="57">
                            </div>
                            <h1>Entrar</h1>
                            <form>
                                <div class="form-group row">
                                    <label for="agencia">Agência</label>
                                    <input type="text" pattern="\d*" maxlength="4" class="form-control" id="agencia" placeholder="">
                                </div>
                                <div class="form-group row">
                                    <label for="conta">Conta Corrente</label>
                                    <input type="text" class="form-control" id="conta" placeholder="">
                                </div>
                                <div class="form-group row">
                                    <label for="senha">Senha</label>
                                    <input type="password" class="form-control" id="senha" placeholder="">
                                </div>

                                <button class="btn btn-primary w-100 py-2" style="margin-top: 10px" type="submit">Sign in</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
