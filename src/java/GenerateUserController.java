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
import model.UsuarioDAO;

@WebServlet(urlPatterns = {"/CreateUser"})
public class GenerateUserController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            // Renderizar o formulário
            renderForm(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Recuperar os dados do formulário
        Usuario newUser = new Usuario();
        newUser.setNome(request.getParameter("nomeInput"));
        newUser.setCpf(request.getParameter("cpfInput"));
        newUser.setSenha(request.getParameter("senhaInput"));
        
        UsuarioDAO userDao = new UsuarioDAO();
        try {
            
            // Mostrar os dados no console
            System.out.println("getCpf: " + newUser.getCpf());
            System.out.println("getNome: " + newUser.getNome());
            System.out.println("getSenha: " + newUser.getSenha());
            userDao.Inserir(newUser);
        } catch (Exception ex) {
            Logger.getLogger(GenerateUserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        RequestDispatcher rd;
        request.setAttribute("msgOperacaoRealizada", "Usuário criado com Sucesso!");
        request.setAttribute("link", "http://localhost:8080/sistema-bancario/index.html");
        rd = request.getRequestDispatcher("showMessage.jsp");
        rd.forward(request, response);
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirecionar para o JSP desejado
        request.getRequestDispatcher("/admin/createUserPage.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
