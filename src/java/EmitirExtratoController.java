/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet(urlPatterns = {"/EmitirExtratoController"})
public class EmitirExtratoController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Verifique se o parâmetro "emitirExtrato" está presente na URL
        HttpSession sessao = request.getSession();
        
        String emitirExtrato = request.getParameter("emitirExtrato");

        if (emitirExtrato != null && emitirExtrato.equals("true")) {
            sessao.setAttribute("mostrarTabela", true);
        } else {
            sessao.setAttribute("mostrarTabela", false);
        }
        RequestDispatcher rd = request.getRequestDispatcher("EmitirExtratoPage.jsp") ;
        rd.forward(request, response);

    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    }

    
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
