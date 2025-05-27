/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import dto.Estudiante;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Quichiz
 */
@WebServlet(name = "estudiante", urlPatterns = {"/estudiante"})
public class estudiante extends HttpServlet {

    // FORMATO DE FECHA PARA CONVERSIÓN
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // CONFIGURACIÓN DE PERSISTENCIA JPA
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_TDP-IIpractica_war_1.0-SNAPSHOTPU");

    // OBTENER ENTITY MANAGER
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // MÉTODO GET - LISTAR TODOS LOS ESTUDIANTES
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            List<Estudiante> estudiantes = em.createQuery("SELECT e FROM Estudiante e ORDER BY e.codiEstdWeb DESC", Estudiante.class).getResultList();
            JSONArray jsonArray = new JSONArray();

            for (Estudiante e : estudiantes) {
                JSONObject obj = new JSONObject();
                obj.put("codiEstdWeb", e.getCodiEstdWeb());
                obj.put("ndniEstdWeb", e.getNdniEstdWeb());
                obj.put("appaEstdWeb", e.getAppaEstdWeb());
                obj.put("apmaEstdWeb", e.getApmaEstdWeb());
                obj.put("nombEstdWeb", e.getNombEstdWeb());

                // FORMATEAR FECHA
                if (e.getFechNaciEstdWeb() != null) {
                    obj.put("fechNaciEstdWeb", dateFormat.format(e.getFechNaciEstdWeb()));
                } else {
                    obj.put("fechNaciEstdWeb", JSONObject.NULL);
                }

                // VALIDACIÓN SIMPLE PARA CAMPOS STRING
                obj.put("logiEstd", e.getLogiEstd() != null ? e.getLogiEstd() : "");
                obj.put("passEstd", e.getPassEstd() != null ? e.getPassEstd() : "");

                jsonArray.put(obj);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("data", jsonArray);
            jsonResponse.put("message", "Estudiantes cargados correctamente");

        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al cargar estudiantes: " + e.getMessage());
        } finally {
            em.close();
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // MÉTODO POST - CREAR NUEVO ESTUDIANTE
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = getEntityManager();
        JSONObject jsonResponse = new JSONObject();

        try {
            // LEER DATOS JSON DEL REQUEST
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            // CREAR NUEVO ESTUDIANTE
            Estudiante estudiante = new Estudiante();
            estudiante.setNdniEstdWeb(json.getString("ndniEstdWeb"));
            estudiante.setAppaEstdWeb(json.getString("appaEstdWeb").toUpperCase());
            estudiante.setApmaEstdWeb(json.getString("apmaEstdWeb").toUpperCase());
            estudiante.setNombEstdWeb(json.getString("nombEstdWeb").toUpperCase());

            // CONVERTIR FECHA DE STRING A DATE
            String fechaStr = json.getString("fechNaciEstdWeb");
            Date fecha = dateFormat.parse(fechaStr);
            estudiante.setFechNaciEstdWeb(fecha);

            estudiante.setLogiEstd(json.getString("logiEstd"));
            estudiante.setPassEstd(json.getString("passEstd")); // SIN CIFRADO COMO SOLICITASTE

            // PERSISTIR EN BASE DE DATOS
            em.getTransaction().begin();
            em.persist(estudiante);
            em.getTransaction().commit();

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Estudiante creado correctamente");
            response.setStatus(HttpServletResponse.SC_CREATED);

        } catch (Exception e) {
            // ROLLBACK EN CASO DE ERROR
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al crear estudiante: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            em.close();
        }

        // ENVIAR RESPUESTA
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

}
