package servlet;

import dao.EstudianteJpaController;
import dto.Estudiante;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "login", urlPatterns = {"/login"})
public class login extends HttpServlet {

    //LLAMAR AL CONTROLADOR - ACCESO A BD
    private EstudianteJpaController loginService;

    //LLAMA A LA PERSISTENCIA
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Preg1TDS_war_1.0-SNAPSHOTPU");

    @Override
    public void init() throws ServletException {
        super.init();
        //INICIALIZAR CONTROLADOR
        loginService = new EstudianteJpaController(emf);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // AGREGAR HEADERS CORS PARA PERMITIR PETICIONES DESDE DIFERENTES DOMINIOS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        PrintWriter out = response.getWriter();

        try {
            // Leer el cuerpo del request como texto(lee la solicitud)
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // VALIDAR QUE EL CUERPO NO ESTE VACIO
            String requestBody = sb.toString();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("status", "fail");
                errorJson.put("message", "Cuerpo de la petición vacío");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            JSONObject body = new JSONObject(requestBody);

            // VALIDAR QUE EXISTAN LOS CAMPOS REQUERIDOS
            if (!body.has("dni") || !body.has("clave")) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("status", "fail");
                errorJson.put("message", "DNI y clave son requeridos");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            String dni = body.getString("dni");
            String clave = body.getString("clave");

            // VALIDAR QUE NO ESTEN VACIOS
            if (dni == null || dni.trim().isEmpty() || clave == null || clave.trim().isEmpty()) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("status", "fail");
                errorJson.put("message", "DNI y clave no pueden estar vacíos");
                out.print(errorJson.toString());
                out.flush();
                return;
            }

            Estudiante user = loginService.validarEstudianteDni(dni.trim(), clave.trim());

            JSONObject json = new JSONObject();
            if (user != null) {
                request.getSession().setAttribute("estudiante", user);
                json.put("status", "ok");
                json.put("redirect", "principal.html");
                // AGREGAR INFORMACIÓN DEL USUARIO PARA DEBUG
                json.put("usuario", user.getLogiEstd());
            } else {
                json.put("status", "fail");
                json.put("message", "Usuario o clave incorrecta");
            }

            //Muestra la respuesta al cliente(HTML)
            out.print(json.toString());
            out.flush();

        } catch (Exception e) {
            // MANEJAR ERRORES DE PARSING JSON O OTROS
            JSONObject errorJson = new JSONObject();
            errorJson.put("status", "fail");
            errorJson.put("message", "Error interno del servidor: " + e.getMessage());
            out.print(errorJson.toString());
            out.flush();
            // DEBUG
            e.printStackTrace();
        }
    }

    // AGREGAR SOPORTE PARA OPTIONS (CORS PREFLIGHT)
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
