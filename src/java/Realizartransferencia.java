/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.ContasBancariaDAO;
import entidade.ContaBancaria;
import javax.servlet.RequestDispatcher;
import entidade.Usuario;

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
        processRequest(request, response);
        //tipo da conta que vai sair dinheiro do usuariologado
        String tipoconta = (String) request.getParameter("tipoconta");
        
        //usuario que vai receber dinheiro
        String agencia_receber = (String) request.getParameter("agencia_recebedor");
        String conta_receber = (String) request.getParameter("conta_recebedor");
        String cpf_receber = (String) request.getParameter("cpf_recebedor");
        String valor_receber = (String) request.getParameter("valor_recebedor");
        ContaBancaria contarecebedor;
        ContasBancariaDAO contaDaorecebedor = new ContasBancariaDAO();
        contarecebedor = contaDaorecebedor.get(Integer.parseInt(conta_receber));
        Usuario usuariorecebedor;
        
        
         
        
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
