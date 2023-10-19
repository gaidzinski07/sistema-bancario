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
    </head>
    <body>
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
                    <span class="home-text">Transferência</span>
                  </div>
                </div>
              </div>
              <div class="home-container09">
                <span class="home-text1">De:</span>
              </div>
            </div>
            <div class="home-container10">
              <div class="home-container11">
                <span class="home-text2">Selecione o tipo de conta:</span>
                <select class="home-select">
                  <option value="CC">Conta Corrente</option>
                  <option value="CP">Conta Poupança</option>
                  <option value="CI">Conta Investimento</option>
                </select>
              </div>
              <div class="home-container12"></div>
              <div class="home-container13">
                <span class="home-text3">Saldo : </span>
                <span class="home-text4">R$ 0,00</span>
              </div>
              <ul class="list">
                <li class="list-item"></li>
                <li class="list-item"></li>
              </ul>
              <div class="home-container14"></div>
            </div>
          </div>
          <div class="home-container15">
            <div class="home-container16">
              <span class="home-text5">Digite sua senha</span>
              <input type="text" placeholder="Senha" class="input" />
            </div>
            <button type="button" class="home-button button">Ok</button>
          </div>
        </div>
      </div>
    </div>
    <div class="home-container17">
      <div class="home-container18">
        <div class="home-container19">
          <div class="home-container20">
            <img
              alt="image"
              src="https://i.imgur.com/kBOkecM.png"
              class="home-image"
            />
          </div>
          <div class="home-container21">
            <span class="home-text6">Para:</span>
          </div>
        </div>
        <div class="home-container22">
          <span class="home-text7">Digite o número da Agência: </span>
          <input
            type="text"
            placeholder="1234-5"
            class="home-textinput1 input"
          />
        </div>
      </div>
      <div class="home-container23">
        <span class="home-text8">Digite o número da Conta</span>
        <input type="text" placeholder="6789" class="home-textinput2 input" />
      </div>
    </div>
  </div>
</div>
    </body>
</html>
