
import entidade.Agencia;
import entidade.Cliente;
import entidade.ContaBancaria;
import entidade.Fundo;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.AgenciaDAO;
import model.ClienteDAO;
import model.ContasBancariaDAO;
import model.FundoDAO;

/**
 *
 * @author lucas
 */
@WebServlet(urlPatterns = {"/ConexaoTesteBD"})
public class ConexaoTesteBD extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        //Declara Lista Agencias
        AgenciaDAO agenciaDAO = new AgenciaDAO();
        ArrayList<Agencia> agencias = new ArrayList<Agencia>();

        //Declara Lista Conta Bancaria
        ContasBancariaDAO contaDAO = new ContasBancariaDAO();
        ArrayList<ContaBancaria> contas = new ArrayList<ContaBancaria>();

        //Declara Lista Clientes
        ClienteDAO clienteDAO = new ClienteDAO();
        ArrayList<Cliente> clientes = new ArrayList<Cliente>();
        
        //Declara Lista Fundos
        FundoDAO fundoDAO = new FundoDAO();
        ArrayList<Fundo> fundos = new ArrayList<Fundo>();

        try {
            contas = contaDAO.getAll();
            agencias = agenciaDAO.getAll();
            clientes = clienteDAO.getAll();
            fundos = fundoDAO.getAll();

        } catch (Exception ex) {
            Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Teste</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; }");
            out.println("h1 { color: #336699; }");
            out.println("ul { list-style-type: none; padding: 0; }");
            out.println("li { margin-bottom: 10px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Dados de Agência:</h1>");

            if (!agencias.isEmpty()) {
                out.println("<ul>");
                for (Agencia agencia : agencias) {
                    out.println("<li>" + "Id: " + agencia.getId() + " Endereço: " + agencia.getEndereco() + "</li>");
                }
                out.println("</ul>");
            } else {
                out.println("<p>Nenhuma agência encontrada.</p>");
            }
            out.println("<h1>Dados de Conta Bancaria:</h1>");
            if (!contas.isEmpty()) {
                out.println("<ul>");
                for (ContaBancaria conta : contas) {
                    out.println("<li>" + "Conta Corrente: " + conta.getContaCorrente() + " Id Cliente: " + conta.getIdCliente() + "</li>");
                }
                out.println("</ul>");
            } else {
                out.println("<p>Nenhuma Conta encontrada.</p>");
            }

            out.println("<h1>Dados de Cliente:</h1>");
            if (!clientes.isEmpty()) {
                out.println("<ul>");
                for (Cliente cliente : clientes) {
                    out.println("<li>" + "Id Cliente: " + cliente.getId() + " Tipo Cliente: " + cliente.getTipo_Cliente() + " Id Usuario: " + cliente.getId_usuario() + "</li>");
                }
                out.println("</ul>");
            } else {
                out.println("<p>Nenhum Cliente encontrado.</p>");
            }

            out.println("<h1>Dados de Fundo:</h1>");
            if (!fundos.isEmpty()) {
                out.println("<ul>");
                for (Fundo fundo : fundos) {
                    out.println("<li>" + "Id Fundo: " + fundo.getIdFundo() + " Valor Minimo: " + fundo.getValorMinimo() + " Valor Cota: " + fundo.getValorCota()+ "</li>");
                }
                out.println("</ul>");
            } else {
                out.println("<p>Nenhum Fundo encontrado.</p>");
            }

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
