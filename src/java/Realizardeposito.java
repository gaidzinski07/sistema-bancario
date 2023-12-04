/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import entidade.Cliente;
import entidade.ContaBancaria;
import entidade.Deposito;
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
import model.UsuarioDAO;

/**
 *
 * @author fifap
 */
@WebServlet(urlPatterns = {"/Realizardeposito"})
public class Realizardeposito extends HttpServlet {

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
            out.println("<title>Servlet Realizardeposito</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Realizardeposito at " + request.getContextPath() + "</h1>");
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
        
        int tipodeposito = Integer.parseInt(request.getParameter("flexRadioDefault"));
        float valordeposito = Float.parseFloat(request.getParameter("valordep"));
        String senha = (String) (request.getParameter("password"));
        String confirmaveracidade = (String) (request.getParameter("confirmaveracidade"));
        ContaBancaria contabancaria;
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        Cliente cliente;
        ClienteDAO clientedao = new ClienteDAO();
        cliente = clientedao.get(id_logado);
        contabancaria = contaDao.get(cliente.getContaBancaria());
        boolean logado  = senha.equals(String.valueOf(usuariologado.getSenha()));
        
        long millis=System.currentTimeMillis();
        java.sql.Timestamp date = new java.sql.Timestamp(millis); 
        
         if (confirmaveracidade == null){
         confirmaveracidade = "0";}
            RequestDispatcher rd;
        if (confirmaveracidade.equals("0")){
            request.setAttribute("msg", "Erro: Você deve confirmar as condições de veracidade.");
            rd = request.getRequestDispatcher("/realizardeposito.jsp");
            rd.forward(request, response); 
        } else
        {   
            
            if (logado){
       
            Float valor_acrescido_final = contabancaria.getSaldoAtual() + (valordeposito);
            contabancaria.setSaldoAtual(valor_acrescido_final);
            contaDao.update(contabancaria);
            Deposito deposito = new Deposito(1,cliente.getContaBancaria(), date,contabancaria.getIdAgencia(),valordeposito, tipodeposito);
            DepositoDAO depositodao = new DepositoDAO();    
            depositodao.insert(deposito);
            request.setAttribute("msg", "Deposito efetuado com sucesso na Conta Corrente");
            rd = request.getRequestDispatcher("/realizardeposito.jsp");
            rd.forward(request, response);
            }
            else {
            request.setAttribute("msg", "Senha incorreta");
            rd = request.getRequestDispatcher("/realizardeposito.jsp");
            rd.forward(request, response);   
            
            }
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
