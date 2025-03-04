package classes;


import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/GetCompany")
public class GetCompany extends HttpServlet {
    @Override
    public void init() throws ServletException {
        System.out.println("GetCompany servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("GetCompany servlet woke up");
        JsonObject result = null;

        if (request.getParameterMap().isEmpty()){
            request.getRequestDispatcher("/WEB-INF/pages/GetCompany.jsp").forward(request, response);
        } else {
            JsonObject data = new JsonObject();
            data.addProperty("DB_type", "mysql");
            data.addProperty("Company_ID", request.getParameter("comp_id"));

            result = AdminDBManager.getInstance().getCompany(data);
        }

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            if(result.get("StatusCode").getAsInt() != 200){
                request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
                out = response.getWriter();
                out.println("<h1>" + "Failed! <br><br>" + result.get("Info").getAsString() + "</h1>");
            } else {
                request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);

                JsonObject info = result.get("Info").getAsJsonObject();

                System.out.println(info);

                ServletUtils.displayCompany(response, info);
            }

            out.println("<a href=\"/\" class=\"options\">Go Home</a>");

        } catch (IOException e) {
            out.println("<h1>" + "Failed! <br><br>" + result.get("Info").getAsString() + "</h1>");
            out.println("<a href=\"/\" class=\"options\">Go Home</a>");
        }
    }

    @Override
    public void destroy() {
        // do nothing.
    }

}
