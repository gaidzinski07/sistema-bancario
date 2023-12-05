/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import dto.InvestimentoDTO;
import entidade.Usuario;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.InvestimentoDAO;
import model.UsuarioDAO;

/**
 *
 * @author João
 */
@WebServlet(urlPatterns = {"/InvestimentoServlet"})
public class InvestimentoServlet extends HttpServlet {

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
        String acao = (String) request.getParameter("acao");
        acao = acao == null ? "" : acao;
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        UsuarioDAO userDao = new UsuarioDAO();
        int conta = userDao.findContaBancariaByUsuario(usuario);
        if(conta > -1){
            InvestimentoDAO dao = new InvestimentoDAO();
            switch(acao){
                case "RESGATE":
                    int idInvestimento = Integer.parseInt(request.getParameter("idInvestimento"));
                    boolean resgatou = dao.resgatar(idInvestimento);
                    request.setAttribute("mensagem", resgatou? "Resgate realizado com sucesso!" : "Houve um erro no processamento da aplicação");
                    break;
                case "APLICACAO":
                    int idFundo = Integer.parseInt(request.getParameter("idFundo"));
                    String vr = (String) request.getParameter("valorInvestido");
                    float valor = vr == null ? 0 : Float.parseFloat(vr);
                    boolean aplicou = dao.aplicar(idFundo, valor);
                    request.setAttribute("mensagem", aplicou? "Aplicação realizada com sucesso!" : "Houve um erro no processamento da aplicação");
                    break;
            }
            List<InvestimentoDTO> dto = dao.getInvestimentosDoCliente(conta);
            request.setAttribute("investimentos", dto);
            RequestDispatcher rd = request.getRequestDispatcher("visualizarInvestimentos.jsp");
            rd.forward(request, response);
        }else{
            RequestDispatcher rd = request.getRequestDispatcher("index.html");
            rd.forward(request, response);
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
