
import entidade.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.UsuarioDAO;

@WebServlet(urlPatterns = {"/LoginController"})
public class LoginController extends HttpServlet {

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
        RequestDispatcher rd;
        // Recuperar os dados do formulário
        Usuario newUser = new Usuario();
        Usuario usuarioLogado = new Usuario();
        UsuarioDAO userDao = new UsuarioDAO();

        try {

            newUser.setCpf(request.getParameter("cpfInput"));
            newUser.setSenha(request.getParameter("senhaInput"));

            usuarioLogado = userDao.logar(newUser);
            System.out.println("CPF: " + usuarioLogado.getCpf());
        } catch (Exception ex) {
            Logger.getLogger(GenerateUserController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (usuarioLogado.getId() != 0) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuarioLogado);
            
            request.setAttribute("userLogged", "Usário " + usuarioLogado.getNome() + " " + "logado com sucesso!");
            rd = request.getRequestDispatcher("/home.jsp");
            rd.forward(request, response);

        } else {
            request.setAttribute("msgError", "Usuário e/ou senha incorreto");
            rd = request.getRequestDispatcher("/loginAdmin.jsp");
            rd.forward(request, response);

        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirecionar para o JSP desejado
        request.getRequestDispatcher("/loginAdmin.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
