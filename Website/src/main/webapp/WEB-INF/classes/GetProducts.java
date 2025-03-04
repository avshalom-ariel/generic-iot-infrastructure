package classes;

import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/GetProducts")
public class GetProducts extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("GetProducts servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("GetProducts servlet woke up");
        JsonObject result = null;

        if (request.getParameterMap().isEmpty()){
            request.getRequestDispatcher("/WEB-INF/pages/GetProducts.jsp").forward(request, response);
        } else {
            JsonObject data = new JsonObject();
            data.addProperty("DB_type", "mysql");
            data.addProperty("Company_ID", request.getParameter("comp_id"));

            try {
                result = AdminDBManager.getInstance().getProducts(data);
            } catch (Exception e) {
                result = new JsonObject();
                result.addProperty("StatusCode", 400);
                result.addProperty("Error", e.getMessage());
                result.addProperty("Info", "Please check your request");
            }
        }
//        response.setContentType("application/json");

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

            int i = 1;
            JsonObject info = result.get("Info").getAsJsonObject();

            try {
                while (null != info.get("Product" + i).getAsJsonObject()) {
                    JsonObject product = info.get("Product" + i).getAsJsonObject();
                    out.println("<br><h2>Product " + i + "</h2><br>");

                    out.println("<table>");
                    out.println("<tr>\n" +
                            "        <th>Product_ID</th>\n" +
                            "        <th>Product_Name</th>\n" +
                            "        <th>Description</th>\n" +
                            "    </tr>");

                    out.println("<tr>");
                    out.println("<th>" + product.get("Product_ID").getAsString() + "</th>");
                    out.println("<th>" + product.get("Product_Name").getAsString() + "</th>");
                    out.println("<th>" + product.get("Description").getAsString() + "</th>");
                    out.println("</tr>");

                    out.println("</table>");
                    ++i;
                }
                out.println("<a href=\"/\" class=\"options\">Go Home</a>");

            } catch(NullPointerException e) {
                out.println("<a href=\"/\" class=\"options\">Go Home</a>");
            }
        }
    }

    @Override
    public void destroy() {
        // do nothing.
    }

}
