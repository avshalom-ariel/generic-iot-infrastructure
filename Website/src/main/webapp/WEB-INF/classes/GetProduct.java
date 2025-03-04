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

@WebServlet("/GetProduct")
public class GetProduct extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("GetProduct servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("GetProduct servlet woke up");
        JsonObject result = null;

        if (request.getParameterMap().isEmpty()){
            request.getRequestDispatcher("/WEB-INF/pages/GetProduct.jsp").forward(request, response);
        } else {
            JsonObject data = new JsonObject();
            data.addProperty("DB_type", "mysql");
            data.addProperty("Company_ID", request.getParameter("comp_id"));
            data.addProperty("Product_ID", request.getParameter("prod_id"));

            result = AdminDBManager.getInstance().getProduct(data);
        }

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(result.get("StatusCode").getAsInt() != 200){
            request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
            out.println("<h1>" + "Failed! <br><br>" + result.get("Info").getAsString() + "</h1>");
        } else {
            request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);

            JsonObject info = result.get("Info").getAsJsonObject();

            out.println("<br><br><h1>Product description</h1><br><br>");
            out.println("<table>");
            out.println("<tr>\n" +
                    "        <th>Product_Name</th>\n" +
                    "        <th>Description</th>\n" +
                    "    </tr>");
            out.println("<tr>\n" +
                    "        <th>" + info.get("Product_Name").getAsString() + "</th>\n" +
                    "        <th>" + info.get("Description").getAsString() + "</th>\n" +
                    "    </tr>");
        }

        out.println("</table><br><br>");
        out.println("<a href=\"/\" class=\"options\">Go Home</a>");
    }

    @Override
    public void destroy() {
        // do nothing.
    }

}
