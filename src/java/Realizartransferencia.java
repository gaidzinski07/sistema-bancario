/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.ContasBancariaDAO;
import entidade.ContaBancaria;
import javax.servlet.RequestDispatcher;
import entidade.Usuario;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.UsuarioDAO;

/**
 *
 * @author fifap
 */
@WebServlet(urlPatterns = {"/Realizartransferencia"})
public class Realizartransferencia extends HttpServlet {

    //private Object ContaBancariaDao;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String tipoconta = (String) request.getParameter("tipoconta");
        //Float saldo;
        //String acao = (String) request.getParameter("acao");
        ContaBancaria contabancaria;
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        contabancaria = contaDao.get(1);
        RequestDispatcher rd;
        
            if (tipoconta.equals("Option 1")){
                
                request.setAttribute("saldo", contabancaria.getSaldoAtual());
                request.setAttribute("tipoconta", "Conta Corrente");
                rd = request.getRequestDispatcher("/transferencia.jsp");
                rd.forward(request, response);
            }
            else if (tipoconta.equals("Option 2")){
                
                request.setAttribute("saldo", contabancaria.getSaldoAtualPP());
                request.setAttribute("tipoconta", "Conta Poupança");
                rd = request.getRequestDispatcher("/transferencia.jsp");
                rd.forward(request, response);
            }
            else if(tipoconta.equals("Option 3")){
            
            }        
               
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        //tipo da conta que vai sair dinheiro do usuariologado
        String tipoconta = (String) request.getParameter("tipoconta");
        Usuario usuariologado = new Usuario();
        UsuarioDAO usuarioDAOlogado = new UsuarioDAO();
        int id_logado = 1;
        //usuariologado.setSenha((usuarioDAOlogado.getUsuario(1)).getSenha());
        try {
            usuariologado = usuarioDAOlogado.getUsuario(id_logado);
        } catch (Exception ex) {
            Logger.getLogger(Realizartransferencia.class.getName()).log(Level.SEVERE, "Não encontrado", ex);
        }
        
        
        
        //usuario que vai receber dinheiro
        int agencia_receber =(Integer.parseInt (request.getParameter("agencia_recebedor")));
        int conta_receber = (Integer.parseInt (request.getParameter("conta_recebedor")));
        String cpf_receber = (request.getParameter("cpf_recebedor"));
        float valor_receber = (Float.parseFloat (request.getParameter("valor_recebedor")));
        String senha = (String) request.getParameter("inputsenha");
        ContaBancaria contarecebedor;
        ContasBancariaDAO contaDaorecebedor = new ContasBancariaDAO();
         contarecebedor = contaDaorecebedor.get((conta_receber));
        Usuario usuariorecebedor;
        UsuarioDAO usuarioDaorecebedor = new UsuarioDAO();
        
        
        try {
            usuariorecebedor = usuarioDaorecebedor.getUsuarioCPF(cpf_receber);
        } catch (Exception ex) {
            Logger.getLogger(Realizartransferencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         //Usuario que vai ceder dinheiro
        ContaBancaria contabancaria;
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        contabancaria = contaDao.get(1);
         
        boolean logado  = senha.equals(String.valueOf(usuariologado.getSenha()));
        
        RequestDispatcher rd;
        if (logado){
            if (tipoconta.equals("Option 1")){
                
                Float valor_retirado_final = contabancaria.getSaldoAtual() - (valor_receber);
                contabancaria.setSaldoAtual(valor_retirado_final);
                contaDao.update(contabancaria);
                Float valor_acrescido_final = contarecebedor.getSaldoAtual() + (valor_receber);
                contarecebedor.setSaldoAtual(valor_acrescido_final);
                contaDaorecebedor.update(contarecebedor);
                request.setAttribute("msg", "Transferência efetivada");
                request.setAttribute("msgsenha", "Senha correta");
                request.setAttribute("saldo", contabancaria.getSaldoAtual());
                request.setAttribute("tipoconta", "Conta Corrente");
                rd = request.getRequestDispatcher("/transferencia.jsp");
                rd.forward(request, response);
            }
            else if (tipoconta.equals("Option 2")){
                
                Float valor_retirado_final = contabancaria.getSaldoAtualPP() - (valor_receber);
                contabancaria.setSaldoAtualPP(valor_retirado_final);
                contaDao.update(contabancaria);
                Float valor_acrescido_final = contarecebedor.getSaldoAtualPP() + (valor_receber);
                contarecebedor.setSaldoAtualPP(valor_acrescido_final);
                contaDaorecebedor.update(contarecebedor);
                request.setAttribute("msg", "Transferência efetivada");
                request.setAttribute("msgsenha", "Senha correta");
                request.setAttribute("saldo", contabancaria.getSaldoAtualPP());
                request.setAttribute("tipoconta", "Conta Poupança");
                rd = request.getRequestDispatcher("/transferencia.jsp");
                rd.forward(request, response);
            }
            else if(tipoconta.equals("Option 3")){
            
            }
        }
        else {request.setAttribute("msg", "Senha incorreta");
                request.setAttribute("saldo", contabancaria.getSaldoAtual());
                request.setAttribute("tipoconta", "Conta Corrente");
            try {
                request.setAttribute("msgsenha", senha);
            } catch (Exception ex) {
                Logger.getLogger(Realizartransferencia.class.getName()).log(Level.SEVERE, null, ex);
            }
                rd = request.getRequestDispatcher("/transferencia.jsp");
                rd.forward(request, response);
        }
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
