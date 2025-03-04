package classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/GetUpdate")
public class GetUpdate extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("GetUpdate servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
                                        throws ServletException, IOException {

        System.out.println("GetUpdate servlet woke up");
        JsonObject httpRespond = null;
        JsonObject mysqlCheck = new JsonObject();

        if (request.getParameterMap().isEmpty()){
            request.getRequestDispatcher("/WEB-INF/pages/GetUpdate.jsp").forward(request, response);
        } else {
            JsonObject data = new JsonObject();
            data.addProperty("DB_type", "mongodb");
            data.addProperty("Company_ID", request.getParameter("comp_id"));
            data.addProperty("Product_ID", request.getParameter("prod_id"));
            data.addProperty("IOT_Device_ID", request.getParameter("device_id"));
            data.addProperty("IOT_Device_Update_ID", request.getParameter("update_id"));

            mysqlCheck.addProperty("DB_type", "mysql");
            mysqlCheck.addProperty("Company_ID", request.getParameter("comp_id"));
            mysqlCheck.addProperty("Product_ID", request.getParameter("prod_id"));

            JsonObject requestToServer = new JsonObject();
            requestToServer.addProperty("Key", "GetIOTDeviceUpdate");
            requestToServer.add("Data", data);

            response.setContentType("text/html");

            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (200 != AdminDBManager.getInstance().getProduct(mysqlCheck).get("StatusCode").getAsInt()) {
                request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
                out.println("<h1> Failed! <br><br></h1>");
                out.println("<h1>" + AdminDBManager.getInstance().getProduct(mysqlCheck).get("Info").getAsString() + "</h1>");
                out.println("<a href=\"/\" class=\"options\">Go Home</a>");
                System.out.println("In mysql check");
                return;
            }

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
                    String unparseUpdate = info.get("IOT_Device_ID_Update_" + request.getParameter("update_id")).getAsString();
                    String parseUpdate = unparseUpdate.replace("\\", "").replace("=", ":").replace("Document", "");
                    System.out.println("parseUpdate IS:  " + parseUpdate);
                    JsonObject fullUpdate = new Gson().fromJson(parseUpdate.substring(1, parseUpdate.length() - 1), JsonObject.class);
                    System.out.println("fullUpdate is: " + fullUpdate);
                    JsonObject update = fullUpdate.get("IOT_Device_Update").getAsJsonObject();
                    System.out.println("update is: " + update);

                    out.println("<h1> Update is: <br><br> </h1>");
                    ServletUtils.printJsonInTable(update, out);

                }

                out.println("<a href=\"/\" class=\"options\">Go Home</a>");

            } catch(Exception e) {
                request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
                out.println("<h1>" + "Failed! <br><br> </h1>");
                out.println("<a href=\"/\" class=\"options\">Go Home</a>");

            }
        }

//        response.setContentType("text/html");
//
//        PrintWriter out = null;
//        try {
//            out = response.getWriter();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if(httpRespond.get("StatusCode").getAsInt() != 200){
//            request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
//            out.println("<h1>" + "Failed! <br><br>" + httpRespond.get("Info").getAsJsonObject() + "</h1>");
//        } else {
//            request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);
//            out.println("<h1>" + "Update: <br><br>" + httpRespond.get("Info").getAsJsonObject() + "</h1>");
//        }
    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
