package classes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/GetUpdates")
public class GetUpdates extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("GetUpdate servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("GetUpdates servlet woke up");
        JsonObject httpRespond = null;

        if (request.getParameterMap().isEmpty()){
            request.getRequestDispatcher("/WEB-INF/pages/GetUpdates.jsp").forward(request, response);
        } else {
            JsonObject data = new JsonObject();

            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                data.addProperty("DB_type", "mongodb");
                data.addProperty("Company_ID", request.getParameter("comp_id"));
                data.addProperty("Product_ID", request.getParameter("prod_id"));
                data.addProperty("IOT_Device_ID", request.getParameter("device_id"));
            } catch(Exception e) {
                out.println("<h1> Failed! <br><br> Please enter valid values</h1>");

                out.println("<a href=\"/\" class=\"options\">Go Home</a>");
            }
//            data.addProperty("DB_type", "mongodb");
//            data.addProperty("Company_ID", request.getParameter("comp_id"));
//            data.addProperty("Product_ID", request.getParameter("prod_id"));
//            data.addProperty("IOT_Device_ID", request.getParameter("device_id"));
//            data.addProperty("IOT_Device_Update_ID", request.getParameter("update_id"));

            JsonObject requestToServer = new JsonObject();
            requestToServer.addProperty("Key", "GetIOTDeviceUpdates");
            requestToServer.add("Data", data);

            response.setContentType("text/html");

            response.setContentType("text/html");

            try {
                httpRespond = ServletUtils.sentTCPRequest(requestToServer);
                System.out.println("DATA = " + httpRespond);
                if(httpRespond.get("StatusCode").getAsInt() != 200){
                    request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
                    out.println("<h1> Failed! <br><br>" + httpRespond.get("Info").getAsString() + "</h1>");
                } else {
                    request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);

                    if (httpRespond.get("StatusCode").getAsInt() != 200) {
                        throw new RuntimeException(httpRespond.get("Info").getAsString());
                    }

                    JsonObject info = httpRespond.get("Info").getAsJsonObject();
                    int i = 1;

                    String unparseUpdate = info.get("IOT_Device_ID_Update_" + i).getAsString();
                    JsonObject update = null;

                    while (null != unparseUpdate) {
                        unparseUpdate = info.get("IOT_Device_ID_Update_" + i).getAsString();
                        String parseUpdate = unparseUpdate.replace("\\", "").replace("=", ":").replace("Document", "");
                        System.out.println("parseUpdate IS:  " + parseUpdate);
                        JsonObject fullUpdate = new Gson().fromJson(parseUpdate.substring(1, parseUpdate.length() - 1), JsonObject.class);
                        System.out.println("fullUpdate is: " + fullUpdate);
                        update = fullUpdate.get("IOT_Device_Update").getAsJsonObject();
                        System.out.println("update is: " + update);
                        out.println("<br><h2>Update " + i + "</h2><br>");
                        Thread.sleep(2000);
                        ServletUtils.printJsonInTable(update, out);
                        ++i;
                    }


                }
            } catch(Exception e) {
                out.println("<h1> Failed! <br><br>" + httpRespond.get("Info").getAsString() + "</h1>");

                out.println("<a href=\"/\" class=\"options\">Go Home</a>");

//                request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
//                out.println("<h1>" + "Failed! <br><br> Please check your request ID's</h1>");
            }
        }
    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
