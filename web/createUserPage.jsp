<%-- Document : createUserPage Created on : 19 de out. de 2023, 18:19:17 Author : Lucas Magalhães --%> <%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous" />
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM" crossorigin="anonymous"></script>

        <link href="./formsStyle.css" rel="stylesheet" />
        <title>Criar Conta</title>
    </head>
    <body style="background-color: #0156a5">
        <jsp:include page="HeaderComponent.html"/>
        <div class="container">
            <div class="row align-items-center justify-content-center" style="height: calc(100vh - 57px) !important;">
                <div class="card" style="width: 50%;">
                    <div class="card-body">
                        <h2 class="card-title text-center">Criar Usuário</h2>
                        <form class="mx-1 mx-md-4">
                            <div class="d-flex flex-row align-items-center mb-4">
                                <i class="fas fa-user fa-lg me-3 fa-fw"></i>
                                <div class="form-outline flex-fill mb-0">
                                    <label class="form-label">Nome Completo</label>
                                    <input type="text" id="" class="form-control" placeholder="Insira seu Nome" />
                                </div>
                            </div>
                            <div class="d-flex flex-row align-items-center mb-4">
                                <i class="fas fa-user fa-lg me-3 fa-fw"></i>
                                <div class="form-outline flex-fill mb-0">
                                    <label class="form-label">CPF</label>
                                    <input type="text" id="" class="form-control" placeholder="Digite seu CPF" />
                                </div>
                            </div>
                            <div class="d-flex flex-row align-items-center mb-4">
                                <i class="fas fa-envelope fa-lg me-3 fa-fw"></i>
                                <div class="form-outline flex-fill mb-0">
                                    <label class="form-label">Email</label>
                                    <input type="email" id="" class="form-control" placeholder="Insira seu Email" />
                                </div>
                            </div>
                            <div class="d-flex flex-row align-items-center mb-4">
                                <i class="fas fa-lock fa-lg me-3 fa-fw"></i>
                                <div class="form-outline flex-fill mb-0">
                                    <label class="form-label">Senha</label>
                                    <input type="password" id="" class="form-control" placeholder="Insira sua senha" />
                                </div>
                            </div>
                            <div class="d-flex flex-row align-items-center mb-4">
                                <i class="fas fa-lock fa-lg me-3 fa-fw"></i>
                                <div class="mb-3">
                                    <label class="form-label">Tipo de Usuário</label>
                                    <select class="form-select" id="">
                                        <option value="admin">Administrador</option>
                                        <option value="op">Operador</option>
                                        <option value="client">Cliente</option>
                                    </select>
                                </div>
                            </div>
                            <div class="d-flex justify-content-center mx-4 mb-3 mb-lg-4">
                                <button type="button" class="btn btn-primary btn-lg">Cadastrar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
