package classes;

import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/RegProduct")
public class RegProduct extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("RegProduct servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("RegProduct servlet woke up");

        JsonObject data = new JsonObject();
        data.addProperty("DB_type", "mysql");

        request.getRequestDispatcher("/WEB-INF/pages/RegProduct.jsp").include(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                                            throws ServletException, IOException {
        Map<String, String[]> map = request.getParameterMap();

        System.out.println("Inside doPost");

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] value = entry.getValue();
            System.out.println(entry.getKey() + ": " + value[0]);
        }

        JsonObject data = new JsonObject();
        data.addProperty("DB_type", "mysql");
        data.addProperty("Company_ID", request.getParameter("comp_id"));
        data.addProperty("Product_Name", request.getParameter("prod_name"));
        data.addProperty("Product_Name", request.getParameter("prod_name"));
        data.addProperty("Description", request.getParameter("desc"));

        JsonObject result = AdminDBManager.getInstance().registerProduct(data);

        data.addProperty("Product_ID", result.get("ID").getAsString());
        data.addProperty("DB_type", "mongodb");

        JsonObject requestToServer = new JsonObject();
        requestToServer.addProperty("Key", "RegisterProduct");
        requestToServer.add("Data", data);

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");

        try {
            if(result.get("StatusCode").getAsInt() != 200){
                request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
                out.println("<h1> Failed! <br><br>" + result.get("Info").getAsString() + "</h1>");
            } else {
                request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);

                data.addProperty("Product_ID", result.get("ID").getAsString());

                JsonObject httpRespond = ServletUtils.sentTCPRequest(requestToServer);

                if (httpRespond.get("StatusCode").getAsInt() != 200) {
                    throw new RuntimeException(httpRespond.get("Info").getAsString());
                }

                out.println("<h1>" + "Success! <br><br> Product ID: " + result.get("ID").getAsString() + "</h1><br><br>");
            }
            out.println("<a href=\"/\" class=\"options\">Go Home</a>");

        } catch(Exception e) {
            request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
            out.println("<h1>" + "Failed! <br><br> Please check the company ID</h1>");
            out.println("<a href=\"/\" class=\"options\">Go Home</a>");

        }
    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
