package classes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class ServletUtils {
    private static final int BUFFER_SIZE = 1024*64;

    public static void displayCompanies(HttpServletResponse response, JsonObject info) {
        int i = 1;

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.println("<table>");
        out.println("<tr>\n" +
                "        <th>Company_Name</th>\n" +
                "        <th>Company_ID</th>\n" +
                "        <th>Contact_Name</th>\n" +
                "        <th>Contact_ID</th>\n" +
                "        <th>Contact_Number</th>\n" +
                "        <th>Address</th>\n" +
                "    </tr>");

        try{
            while (null != info.get("Contact" + i).getAsJsonObject()) {
                JsonObject contact = info.get("Contact" + i).getAsJsonObject();

                out.println("<tr>");
                out.println("<th>" + contact.get("Company_Name").getAsString() + "</th>");
                out.println("<th>" + contact.get("Company_ID").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_Name").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_ID").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_Number").getAsString() + "</th>");
                out.println("<th>" + contact.get("Address").getAsString() + "</th>");
                out.println("</tr>");

                ++i;
            }
            System.out.println("HEYEYEYEYEY");
        } catch (Exception e) {
            out.println("</table><br><br>");
            out.println("<a href=\"/\" class=\"options\">Go Home</a>");
        }
    }

    public static void displayCompany(HttpServletResponse response, JsonObject info) {
        int i = 1;

        response.setContentType("text/html");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.println("<br><br><h1>Details for company " + info.get("Company_Name").getAsString() + "</h1><br><br><br>");

        out.println("<table>");
        out.println("<tr>\n" +
                "        <th>Company_Name</th>\n" +
                "        <th>Company_ID</th>\n" +
                "        <th>Contact_Name</th>\n" +
                "        <th>Contact_ID</th>\n" +
                "        <th>Contact_Number</th>\n" +
                "        <th>Address</th>\n" +
                "    </tr>");

        try{

            while (null != info.get("Contact" + i).getAsJsonObject()) {
                JsonObject contact = info.get("Contact" + i).getAsJsonObject();

                out.println("<tr>");
                out.println("<th>" + contact.get("Company_Name").getAsString() + "</th>");
                out.println("<th>" + contact.get("Company_ID").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_Name").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_ID").getAsString() + "</th>");
                out.println("<th>" + contact.get("Contact_Number").getAsString() + "</th>");
                out.println("<th>" + contact.get("Address").getAsString() + "</th>");
                out.println("</tr>");

                ++i;
            }

        } catch (Exception e) {
            out.println("</table>");
            out.println("<a href=\"/\" class=\"options\">Go Home</a>");
        }
    }

    public static JsonObject sentTCPRequest(JsonObject request) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName("10.10.0.95"), 60000));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buffer = ByteBuffer.wrap(new Gson().toJson(request).getBytes());

        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buffer.clear();

        ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        responseBuffer.clear();

        int byteRead = 0;
        try {
            byteRead = socketChannel.read(responseBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("byteRead = " + byteRead);

        responseBuffer.flip();

        String response = new String(responseBuffer.array()).trim();

        request = new Gson().fromJson(response, JsonObject.class);

        return request;
    }

    public static void printJsonInTable(JsonObject data, PrintWriter out) {
        out.println("<table>");
        out.println("<tr>\n" +
                "        <th>Key</th>\n" +
                "        <th>Value</th>\n" +
                "    </tr>");

        out.println("<tr>");
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            out.println("<tr>");
            out.println("<th>" + entry.getKey() + "</th>");
            out.println("<th>" + entry.getValue() + "</th>");
            out.println("</tr>");
        }
        out.println("</tr>");
        out.println("</table>");
        out.println("<br>");

    }
}
