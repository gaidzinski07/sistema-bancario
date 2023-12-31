import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
/**
 * @author lucas
 */

@WebServlet(urlPatterns = {"/EmitirSaldoController"})
public class EmitirSaldoController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Verifique se o parâmetro "emitirSaldo" está presente na URL

        HttpSession sessao = request.getSession();
        String emitirSaldo = request.getParameter("emitirSaldo");

        if (emitirSaldo != null && emitirSaldo.equals("true")) {
            sessao.setAttribute("mostraSaldo", true);
        } else {
            sessao.setAttribute("mostraSaldo", false);
        }
        RequestDispatcher rd = request.getRequestDispatcher("EmitirSaldoPage.jsp") ;
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
