/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import entidade.Cliente;
import entidade.ContaBancaria;
import entidade.Deposito;
import entidade.Saque;
import entidade.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.ClienteDAO;
import model.ContasBancariaDAO;
import model.DepositoDAO;
import model.SaqueDAO;
import model.UsuarioDAO;

/**
 *
 * @author fifap
 */
@WebServlet(urlPatterns = {"/Realizarsaque"})
public class Realizarsaque extends HttpServlet {

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
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Realizarsaque</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Realizarsaque at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
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
        Usuario usuariologado = new Usuario();
        UsuarioDAO usuarioDAOlogado = new UsuarioDAO();
        int id_logado = 1;
        try {
            usuariologado = usuarioDAOlogado.getUsuario(id_logado);
        } catch (Exception ex) {
            Logger.getLogger(Realizardeposito.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int tipoconta = Integer.parseInt(request.getParameter("flexRadioDefault"));
        float valorsaque = Float.parseFloat(request.getParameter("valorsaq"));
        String senha = (String) (request.getParameter("password"));
        //String confirmaveracidade = (String) (request.getParameter("confirmaveracidade"));
        ContaBancaria contabancaria;
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        Cliente cliente;
        ClienteDAO clientedao = new ClienteDAO();
        cliente = clientedao.get(id_logado);
        contabancaria = contaDao.get(cliente.getContaBancaria());
        boolean logado  = senha.equals(String.valueOf(usuariologado.getSenha()));
        
        long millis=System.currentTimeMillis();
        java.sql.Timestamp date = new java.sql.Timestamp(millis); 
        RequestDispatcher rd;
         
            
            if (logado){
                if (tipoconta == 1){
                    Float valor_descrescido_final = contabancaria.getSaldoAtual() - (valorsaque);
                    contabancaria.setSaldoAtual(valor_descrescido_final);
                    contaDao.update(contabancaria); 
                    request.setAttribute("msg", "Saque efetuado com sucesso na Conta Corrente");
                }
                else if (tipoconta == 2){
                    Float valor_descrescido_final = contabancaria.getSaldoAtualPP() - (valorsaque);
                    contabancaria.setSaldoAtualPP(valor_descrescido_final);
                    contaDao.update(contabancaria);
                    request.setAttribute("msg", "Saque efetuado com sucesso na Conta Poupança");
                }
            Saque saque = new Saque(1,cliente.getContaBancaria(), contabancaria.getIdAgencia(),date,valorsaque);
            SaqueDAO saquedao = new SaqueDAO();    
            saquedao.insert(saque);
            
            rd = request.getRequestDispatcher("/realizarsaque.jsp");
            rd.forward(request, response);
            }
            else {
            request.setAttribute("msg", "Senha incorreta");
            rd = request.getRequestDispatcher("/realizarsaque.jsp");
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
