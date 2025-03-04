package classes;

import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetCompanies")
public class GetCompanies extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("GetCompanies servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("GetCompanies servlet woke up");

        JsonObject data = new JsonObject();
        data.addProperty("DB_type", "mysql");

        JsonObject result = null;
        try {
            request.getRequestDispatcher("/WEB-INF/pages/GetCompanies.jsp").include(request, response);
            result = AdminDBManager.getInstance().getCompanies(data);
        } catch (Exception e) {
            result = new JsonObject();
            result.addProperty("StatusCode", 400);
            result.addProperty("Error", e.getMessage());
            result.addProperty("Info", "Please check your request");
        }

        JsonObject info = null;
        try {
            info = result.get("Info").getAsJsonObject();

            ServletUtils.displayCompanies(response, info);
        } catch(Exception e) {

        }
    }
}
