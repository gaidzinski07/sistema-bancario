/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import dto.InvestimentoDTO;
import dto.TransacaoDTO;
import entidade.ContaBancaria;
import entidade.Deposito;
import entidade.Saque;
import entidade.Transferencia;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.ContasBancariaDAO;
import model.DepositoDAO;
import model.InvestimentoDAO;
import model.SaqueDAO;
import model.TransferenciaDAO;

/**
 *
 * @author João
 */
@WebServlet(urlPatterns = {"/TesteDAOs"})
public class TesteDAOs extends HttpServlet {

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
            DepositoDAO depositoDao = new DepositoDAO();
            SaqueDAO saqueDao = new SaqueDAO();
            TransferenciaDAO transfDao = new TransferenciaDAO();
            InvestimentoDAO investDao = new InvestimentoDAO();
            ContasBancariaDAO contaDao = new ContasBancariaDAO();
            
            ArrayList<Deposito> depositos = depositoDao.getAll();
            ArrayList<Saque> saques = saqueDao.getAll();
            ArrayList<Transferencia> transferencias = transfDao.getAll();
            List<InvestimentoDTO> investimentos = investDao.getInvestimentosDoCliente(1);
            ContaBancaria conta = new ContaBancaria();
            conta.setContaCorrente(1);
            List<TransacaoDTO> extrato = contaDao.getExtrato(conta);
            
            
            
            System.out.println("\n\n\n>>> DEPÓSITOS:");
            depositos.stream().forEach(d -> {
                System.out.println("\n>>Depósito Id: " + d.getId());
                System.out.println(">Data deposito: " + d.getDataDeposito().toString());
                System.out.println(">Valor Depósito: " + d.getValor());
            });
            
            
            System.out.println("\n\n\n>>> SAQUES:");
            saques.stream().forEach(s -> {
                System.out.println("\n>>Saque Id: " + s.getId());
                System.out.println(">Data saque: " + s.getData().toString());
                System.out.println(">Valor saquei: "+ s.getValor());
            });
            
            System.out.println("\n\n\n>>> TRANSFERÊNCIAS:");
            transferencias.stream().forEach(t -> {
                System.out.println("\n>>Transferência Id: " + t.getId());
                System.out.println(">Data Transferência: "+t.getData().toString());
                System.out.println(">Valor Transferência: "+t.getValor());
            });
            
            
            System.out.println("\n\n\n>>> INVESTIMENTOS:");
            investimentos.stream().forEach(t -> {
                System.out.println("\n>>Investimento Id: " + t.getInvestimento().getId());
                System.out.println(">Fundo: "+t.getFundo().getNome());
                System.out.println(">Qtd Cotas: "+t.getInvestimento().getQtdCotas());
                System.out.println(">Data: "+t.getInvestimento().getData().toString());
                System.out.println(">Valor Investido: " + t.getInvestimento().getVrInvestido());
                System.out.println(">Valor atual: "+t.getRendimentos() * t.getInvestimento().getVrInvestido());
            });
            
            System.out.println("\n\n\n>>> EXTRATO:");
            extrato.stream().forEach(e -> {
                System.out.println("----------------------");
                System.out.println("\n>>Transacao Id: " + e.getId());
                System.out.println(">Tipo transacao: "+e.getTipo());
                System.out.println(">Data transacao: "+e.getData().toString());
                System.out.println(">Valor: "+e.getValor());
            });
            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet TesteDAOs</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet TesteDAOs at " + request.getContextPath() + "</h1>");
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
        processRequest(request, response);
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
