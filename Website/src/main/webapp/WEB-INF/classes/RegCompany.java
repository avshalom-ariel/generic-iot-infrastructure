package classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Map;

@WebServlet("/RegCompany")
public class RegCompany extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("RegCompany servlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("RegCompany servlet woke up");

        JsonObject data = new JsonObject();
        data.addProperty("DB_type", "mysql");

        request.getRequestDispatcher("/WEB-INF/pages/RegCompany.jsp").include(request, response);

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

        String dateWithDefaultDay = request.getParameter("exp_date") + "/01";

        DateTimeFormatter inputFormatterWithDay = DateTimeFormatter.ofPattern("MM/yy/dd");
        LocalDate date = LocalDate.parse(dateWithDefaultDay, inputFormatterWithDay);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = date.format(outputFormatter);

        JsonObject data = new JsonObject();
        data.addProperty("DB_type", "mysql");
        data.addProperty("Company_Name", request.getParameter("comp_name"));
        data.addProperty("Contact_Name", request.getParameter("cont_name"));
        data.addProperty("Contact_Number", request.getParameter("cont_num"));
        data.addProperty("Address", request.getParameter("add"));
        data.addProperty("Credit_Card", request.getParameter("crd_card"));
        data.addProperty("Expiry_Date", formattedDate);
        data.addProperty("Security_Code", request.getParameter("sec_code"));
        System.out.println(data);

        JsonObject result = AdminDBManager.getInstance().registerCompany(data);

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");

        if(result.get("StatusCode").getAsInt() != 200){
            request.getRequestDispatcher("/WEB-INF/pages/Error.jsp").include(request, response);
            out.println("<h1>" + "Failed! <br><br>" + result + "</h1><br><br>");
        } else {
            request.getRequestDispatcher("/WEB-INF/pages/Success.jsp").include(request, response);
            out.println("<h1>" + "Success! <br><br>" + "Company ID: " + result.get("ID").getAsString() + "</h1><br><br>");
        }
        out.println("<a href=\"/\" class=\"options\">Go Home</a>");
    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
