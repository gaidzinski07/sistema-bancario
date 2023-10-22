<%-- 
    Document   : transferência
    Created on : 18 de out. de 2023, 23:17:29
    Author     : fifap
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
        <link href="./formsStyle.css" rel="stylesheet" />
    </head>
    <body>
        <jsp:include page="HeaderComponent.html"/>
<div>
  <link href="./transferencia.css" rel="stylesheet" />
  <div class="home-container">
    <div class="home-container01">
      <div class="home-container02">
        <div class="home-container03">
          <div class="home-container04">
            <div class="home-container05">
              <div class="home-container06">
                <div class="home-container07">
                  <div class="home-container08">
                    <div class="home-container09">
                      <div class="home-container10">
                        <div class="home-container11">
                          <div class="home-container12">
                            <span class="home-text">Transferência</span>
                          </div>
                        </div>
                      </div>
                      <div class="home-container13">
                        <span class="home-text01">De:</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="home-container14">
          <span class="home-text02">Selecione o tipo de conta:</span>
          <select>
            <option value="Option 1">Conta Corrente</option>
            <option value="Option 2">Conta Poupança</option>
            <option value="Option 3">Conta Investimento</option>
          </select>
          <div class="home-container15">
            <span class="home-text03">Saldo : </span>
            <span class="home-text04">R$ 0,00</span>
          </div>
        </div>
      </div>
      <div class="home-container16">
        <span class="home-text05">Digite sua senha</span>
        <input
          type="text"
          placeholder="Senha"
          autofocus
          class="home-textinput input"
        />
        <div class="home-container17">
          <button type="submit" class="home-button button">ok</button>
        </div>
      </div>
    </div>
    <div class="home-container18">
      <div class="home-container19">
        <div class="home-container20">
          <div class="home-container21">
            <div class="home-container22">
              <div class="home-container23">
                <div class="home-container24">
                  <img
                    alt="image"
                    src="https://i.imgur.com/kBOkecM.png"
                    class="home-image"
                  />
                </div>
                <div class="home-container25">
                  <span class="home-text06">Para:</span>
                </div>
              </div>
              <div class="home-container26">
                <span class="home-text07">Digite o número da Agência: </span>
                <input
                  type="text"
                  placeholder="1234-5"
                  class="home-textinput1 input"
                />
              </div>
            </div>
            <div class="home-container27">
              <span class="home-text08">Digite o número da Conta</span>
              <input
                type="text"
                placeholder="6789"
                class="home-textinput2 input"
              />
            </div>
          </div>
          <div class="home-container28">
            <span>Valor a ser creditado R$</span>
            <input
              type="text"
              placeholder="0,00"
              class="home-textinput3 input"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
        crossorigin="anonymous"></script>

    </body>
</html>
