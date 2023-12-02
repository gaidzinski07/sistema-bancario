<%-- 
    Document   : transferência
    Created on : 18 de out. de 2023, 23:17:29
    Author     : fifap
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" import="entidade.ContaBancaria, java.text.*, java.util.*, java.io.*" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>transferencia</title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <link href="./formsStyle.css" rel="stylesheet" />
    </head>
    <body style="background-color:#0156a5">
        <jsp:include page="HeaderComponent.html"/>
        <% String mostra_saldo = String.valueOf(request.getAttribute("saldo")); %>
        <% String mostra_conta = String.valueOf(request.getAttribute("tipoconta")); %>
        <% String mostra_msg = String.valueOf(request.getAttribute("msg")); %>
        <% String mostra_senhaerrada = String.valueOf(request.getAttribute("msgsenha")); %>
            <h1>Realizar transferência entre contas</h1>
            <form name="myTrans" action="Realizartransferencia" id="myTrans">
                <section class="vh-10">
                    <div class="container h-10">
                        <div class="card text-black" style="border-radius: 5px;">
                            <div class="row">
                                <span class="home-text02">Selecione o tipo de conta a consultar:</span>
                                <select name="tipoconta">
                                    <option selected="selected" value="Option 1">Conta Corrente</option>
                                    <option value="Option 2">Conta Poupança</option>
                                    <option value="Option 3">Conta Investimento
                                    </option>
                                 </select>
                                <p></p>
                                <p></p>
                                    <div class="row">
                                        <div class="home-container15">
                                        <span class="home-text03">Tipo : </span>
                                        <span class="home-text04" id="saldo"><%=mostra_conta.toString()%></span>
                                        <p></p>
                                        <span class="home-text03">Saldo : R$</span>
                                        <span class="home-text04" id="saldo"><%=mostra_saldo.toString()%></span>
                                  
                                        </div>
                                    </div>
            <button type="submit" class="btn btn-secondary">Consultar</button>
                            </div>
                        </div>
                    </div>
            </section>
                
            </form>
                                        <p></p>
            <form name="myTrans" action="Realizartransferencia" id="myTrans" method="POST">
                <section class="vh-100">
            <div class="container h-100">
            <div class="container text-left">
                <div class="card text-black" style="border-radius: 0px;">
                      
                <div class="row justify-content-center">                    
                   
                    <div class="col-md-10 col-lg-6 col-xl-5 order-2 order-lg-1">
                        
                        <span class="home-text01">De:</span>
                        <div class="home-container14">
                                                            
                                <span class="home-text02">Selecione o tipo de conta:</span>
                                <select name="tipoconta">
                                    <option selected="selected" value="Option 1">Conta Corrente</option>
                                    <option value="Option 2">Conta Poupança</option>
                                    <option value="Option 3">Conta Investimento
                                    </option>
                                 </select>
                                    <div class="row">
                                        <div class="home-container15">
                                        <%--<span class="home-text03">Saldo : </span>
                                        <span class="home-text04" id="saldo"><%=mostra_saldo.toString()%></span> --%>
                                  
                                        </div>
                                    </div>

                                    
                        </div>
                            <div class="mb-5">
                                    <label for="digitesenha" class="form-label">Digite sua senha</label>
                                    <input type="text" class="form-control" id="inputsenha" name="inputsenha" aria-describedby="valorhelp">
                                    <div id="valorhelp" class="form-text">Usuário deve digitar sua senha</div>
                                </div>
                            <div class="row container justify-content-center">
                            <button type="submit" class="btn btn-primary">Enviar</button>
                            <span class="home-text03">Mensagem : <%=mostra_msg.toString()%> </span>
                            <span class="home-text03">Você digitou : <%=mostra_senhaerrada.toString()%> </span>
                            
                            </div>
                        </div>
                
                    <div class="col">
                        <span class="home-text06">Para:</span>
                        
                                   
                        <div class="home-container26">
                            <span class="home-text07">Digite o número da Agência: </span>
                            <input
                                type="text"
                                placeholder="1234-5"
                                class="home-textinput1 input"
                                name="agencia_recebedor"
                            />
                        </div>
                                    
                        <div class="home-container27">
                             <span class="home-text08">Digite o número da Conta:</span>
                             <input
                                type="text"
                                placeholder="6789"
                                class="home-textinput2 input"
                                name="conta_recebedor"
                            />
                         </div>
                        <div class="home-container26">
                            <span class="home-text07">Digite o CPF: </span>
                            <input
                                type="text"
                                placeholder="987.654.321-01"
                                class="home-textinput1 input"
                                name="cpf_recebedor"
                            />
                        </div>
                        
                        <div class="home-container28">
                            <span>Valor a ser creditado R$</span>
                            <input
                                type="text"
                                placeholder="0,00"
                                class="home-textinput3 input"
                                name="valor_recebedor"
                                />
                
                        </div>
                                               
                    </div>
                        
                </div>
            
            
        
            </section>
                                     <script type="text/javascript">

                                     
                                    //    window.onload=function(){ 
                                    //            window.setTimeout(document.myTrans.submit.bind(document.myTrans), 2500);
                                    //    };

                                   </script>
            </form>
            
        
        
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
        crossorigin="anonymous"></script>
        <script type="text/javascript">

 //window.onload=function(){ 
       // window.setTimeout(document.myTrans.submit(), 5000000);
   // };


</script>

    </body>
</html>
