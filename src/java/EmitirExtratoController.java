import dto.TransacaoDTO;
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
import model.ContasBancariaDAO;
import model.UsuarioDAO;

@WebServlet(urlPatterns = {"/EmitirExtratoController"})
public class EmitirExtratoController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        UsuarioDAO userDao = new UsuarioDAO();
        int conta = userDao.findContaBancariaByUsuario(usuario);
        if(conta > -1){
            System.out.println(conta);
            ContasBancariaDAO dao = new ContasBancariaDAO();
            List<TransacaoDTO> list = dao.getExtrato(conta);
            request.setAttribute("transacoes", list);
            RequestDispatcher rd = request.getRequestDispatcher("extrato.jsp");
            rd.forward(request, response);
        }else{
            RequestDispatcher rd = request.getRequestDispatcher("index.html");
            rd.forward(request, response);
        }
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
