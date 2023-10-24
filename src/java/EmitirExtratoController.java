import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import Objects.Transacao;

@WebServlet(urlPatterns = {"/EmitirExtratoController"})
public class EmitirExtratoController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessao = request.getSession();

        // Verifique se o parâmetro "emitirExtrato" está presente na URL
        String emitirExtrato = request.getParameter("emitirExtrato");

        if (emitirExtrato != null && emitirExtrato.equals("true")) {
            List<Transacao> transacoes = new ArrayList<Transacao>();
            transacoes.add(new Transacao("000001", "Lucas Magalhães", "100.00", "0.00"));
            transacoes.add(new Transacao("000002", "Jacob", "50.00", "0.00"));
            transacoes.add(new Transacao("000003", "Larry the Bird", "0.00", "25.00"));
            sessao.setAttribute("transacoes", transacoes);
            sessao.setAttribute("mostrarTabela", true);
            System.out.println("Valor de transacoes: " + transacoes);
        } else {
            sessao.removeAttribute("transacoes"); // Remove as transações da sessão
            sessao.setAttribute("mostrarTabela", false);
        }

        RequestDispatcher rd = request.getRequestDispatcher("EmitirExtratoPage.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lógica para processar requisições POST, se necessário
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
