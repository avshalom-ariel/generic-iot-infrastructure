package classes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.*;

public class MysqlHandler implements DBMSHandler{
    private static final String mysqlLoginConnectionURL = "jdbc:mysql://localhost:3306/";
    private final String databaseName = "AdminDB";
    private final Connection connection;

    public MysqlHandler() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection loginConnection = DriverManager.getConnection(mysqlLoginConnectionURL, "avshalom", "1234");
            initDB(loginConnection);
            connection = DriverManager.getConnection(mysqlLoginConnectionURL + databaseName, "avshalom", "1234");
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDB(Connection connection) {
        String query = "CREATE DATABASE IF NOT EXISTS AdminDB";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initTables() {
        String companiesQuery = "CREATE TABLE IF NOT EXISTS Companies (Company_ID INT PRIMARY KEY AUTO_INCREMENT, Company_Name VARCHAR(50))";
        String contactsQuery = "CREATE TABLE IF NOT EXISTS Contacts (Contact_ID INT PRIMARY KEY AUTO_INCREMENT, Company_ID INT, Contact_Name VARCHAR(50), Contact_Number VARCHAR(20),Address VARCHAR(50), Credit_Card VARCHAR(20),Expiry_Date DATE, Security_Code CHAR(3), FOREIGN KEY (Company_ID) REFERENCES Companies(Company_ID))";
        String productsQuery = "CREATE TABLE IF NOT EXISTS Products (Product_ID INT PRIMARY KEY AUTO_INCREMENT, Company_ID INT, Product_Name VARCHAR(50), Description VARCHAR(500), FOREIGN KEY (Company_ID) REFERENCES Companies(Company_ID))";

        try {
            PreparedStatement companiesStatement = connection.prepareStatement(companiesQuery);
            PreparedStatement contactsStatement = connection.prepareStatement(contactsQuery);
            PreparedStatement productsStatement = connection.prepareStatement(productsQuery);
            companiesStatement.executeUpdate();
            contactsStatement.executeUpdate();
            productsStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject registerCompany(JsonObject jsonObject) {
        String query = "INSERT INTO Companies(Company_Name) VALUES (?)";
        int companyID = 0;

        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, jsonObject.get("Company_Name").getAsString());

            System.out.println("registerCompany preparedStatement is: " + preparedStatement);
            int rowsEffected = preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                companyID = generatedKeys.getInt(1);
            }

            if(0 == rowsEffected){
                System.out.println("Zero rows   effected!!!");
                preparedStatement.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return respond(400, "Fail register company");
            }

            try{
                InsertContact(jsonObject, companyID);

                connection.commit();
                connection.setAutoCommit(true);

                return respond(200, companyID,"Successfully register company");
            } catch (RuntimeException e) {
                connection.rollback();
                connection.setAutoCommit(true);
                return respond(400, "Fail register company");
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return respond(500, "Server issue");
        }
    }

    private void InsertContact(JsonObject data, long companyID) throws SQLException {
        String query = "INSERT INTO Contacts(Contact_Name, Contact_Number, Company_ID," +
                        " Address, Credit_Card, Expiry_Date, Security_Code) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, data.get("Contact_Name").getAsString());
        preparedStatement.setString(2, data.get("Contact_Number").getAsString());
        preparedStatement.setLong(3, companyID);
        preparedStatement.setString(4, data.get("Address").getAsString());
        preparedStatement.setString(5, data.get("Credit_Card").getAsString());
        preparedStatement.setDate(6, Date.valueOf(data.get("Expiry_Date").getAsString()));
        preparedStatement.setString(7, data.get("Security_Code").getAsString());

        System.out.println("preparedStatement in contacts is: " + preparedStatement);

        int rowsEffected = preparedStatement.executeUpdate();

        if(0 == rowsEffected){
            System.out.println("0 rowsEffected");
            throw new RuntimeException("Failed to insert contact");
        }

        preparedStatement.close();
    }

    @Override
    public JsonObject registerProduct(JsonObject jsonObject) {
        String query = "INSERT INTO Products(Company_ID, Product_Name, Description) VALUES (?, ?, ?);";

        try {
            String productName = jsonObject.get("Product_Name").getAsString();
            String description = jsonObject.get("Description").getAsString();
            int companyID = jsonObject.get("Company_ID").getAsInt();

            if(null == productName || null == description){
                return respond(400, "Data not valid");
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, companyID);
            preparedStatement.setString(2, productName);
            preparedStatement.setString(3, description);

            int rowEffected = preparedStatement.executeUpdate();
            if(0 == rowEffected){
                return respond(500, "Fail register product");
            }

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            long generatedId = 0;

            if (generatedKeys.next()) {
                generatedId = generatedKeys.getLong(1);
                System.out.println("Inserted row with generated ID: " + generatedId);
            }

            return respond(200, generatedId,"Successfully register product");
        }catch (Exception e) {
            return respond(400, "Please check Company ID");
        }
    }

    @Override
    public JsonObject getCompany(JsonObject jsonObject) {
        Integer companyID = jsonObject.get("Company_ID").getAsInt();
        String findCompanyIDQuery = "SELECT Company_Name FROM Companies WHERE Company_ID = ?;";

        if (200 != companyCheck(companyID.toString()).get("StatusCode").getAsInt()) {
            return companyCheck(companyID.toString());
        }

        ResultSet resultSet = null;
        try {
            resultSet= getResult(findCompanyIDQuery, companyID.toString());
            if(!resultSet.next()){
                resultSet.close();
                return respond(400, "Company not exist");
            }
            String companyName = resultSet.getString("Company_Name");

            String getCompanyQuery = "SELECT * FROM Companies c JOIN Contacts con ON c.Company_ID=con.Company_ID WHERE c.Company_ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(getCompanyQuery);
            preparedStatement.setInt(1, companyID);
            resultSet = preparedStatement.executeQuery();

            JsonObject respond = new JsonObject();
            respond.addProperty("Company_Name", companyName);

            int i = 1;
            while(resultSet.next()){
                JsonObject row = new JsonObject();
                row.addProperty("Contact_Name", resultSet.getString("Contact_Name"));
                row.addProperty("Contact_Number", resultSet.getInt("Contact_Number"));
                row.addProperty("Address", resultSet.getString("Address"));
                row.addProperty("Contact_ID", resultSet.getString("Contact_ID"));
                row.addProperty("Company_ID", companyID);
                row.addProperty("Company_Name", companyName);
                respond.add("Contact" + i, row);

                ++i;
            }

            return respond(200, respond);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JsonObject getCompanies(JsonObject jsonObject) {
        String getCompanyQuery = "SELECT * FROM Companies c JOIN Contacts con ON c.Company_ID=con.Company_ID";
        try (PreparedStatement preparedStatement = connection.prepareStatement(getCompanyQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            JsonObject respond = new JsonObject();
            int i = 1;
            while(resultSet.next()){
                JsonObject row = new JsonObject();
                row.addProperty("Company_Name", resultSet.getString("Company_Name"));
                row.addProperty("Company_ID", resultSet.getInt("Company_ID"));
                row.addProperty("Contact_Name", resultSet.getString("Contact_Name"));
                row.addProperty("Contact_Number", resultSet.getInt("Contact_Number"));
                row.addProperty("Address", resultSet.getString("Address"));
                row.addProperty("Contact_ID", resultSet.getString("Contact_ID"));
                respond.add("Contact" + i, row);
                ++i;
            }

            return respond(200, respond);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject getProduct(JsonObject jsonObject) {
        System.out.println("HERE");
        Integer productID = jsonObject.get("Product_ID").getAsInt();
        Integer companyID = jsonObject.get("Company_ID").getAsInt();
        JsonObject respond = new JsonObject();

        if (200 != companyCheck(companyID.toString()).get("StatusCode").getAsInt()) {
            return companyCheck(companyID.toString());
        }
        try {
            String getProductQuery = "SELECT Product_Name, Description FROM Products WHERE Product_ID=? AND Company_ID=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(getProductQuery);
            preparedStatement.setString(1, productID.toString());
            preparedStatement.setString(2, companyID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                respond.addProperty("Product_Name", resultSet.getString("Product_Name"));
                respond.addProperty("Description", resultSet.getString("Description"));
                return respond(200, respond);
            } else {
                return respond(400, "Product dosn't exists");
            }
        } catch (SQLException e) {
            return respond(400, "Bad request");
        }
    }

    @Override
    public JsonObject getProducts(JsonObject data) {
        Integer companyID = data.get("Company_ID").getAsInt();

        if (200 != companyCheck(companyID.toString()).get("StatusCode").getAsInt()) {
            return companyCheck(companyID.toString());
        }
        try {
            String getProductsQuery = "SELECT Product_Name, Description, Product_ID FROM Products WHERE Company_ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(getProductsQuery);
            preparedStatement.setString(1, companyID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            JsonObject respond = new JsonObject();
            int i = 1;
            while(resultSet.next()){
                JsonObject product = new JsonObject();
                product.addProperty("Product_Name", resultSet.getString("Product_Name"));
                product.addProperty("Product_ID", resultSet.getInt("Product_ID"));
                product.addProperty("Description", resultSet.getString("Description"));
                respond.add("Product" + i, product);
                ++i;
            }

            if (1 == i) {
                return respond(400, "No products found");
            }

            return respond(200, respond);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject registerIOTDevice(JsonObject data) {
        System.out.println("In registerIOTDevice, data: " + data);

        return null;
    }

    private JsonObject companyCheck(String companyId) {
        String query = "SELECT Company_Name FROM Companies WHERE Company_ID=?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, companyId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                return respond(400, "Company not exists");
            } else {
                return respond(200, "Company exists");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet getResult(String query, String val) throws SQLException {
        PreparedStatement prepStm = connection.prepareStatement(query);
        prepStm.setString(1, val);
        return prepStm.executeQuery();
    }

    private JsonObject respond(int statusCode, String info){
        JsonObject respond = new JsonObject();
        respond.addProperty("StatusCode", statusCode);
        respond.addProperty("Info", info);
        return respond;
    }

    private JsonObject respond(int statusCode, JsonObject info){
        JsonObject respond = new JsonObject();
        respond.addProperty("StatusCode", statusCode);
        respond.add("Info", info);
        return respond;
    }

    private JsonObject respond(int statusCode, long id, String info){
        JsonObject respond = new JsonObject();
        respond.addProperty("StatusCode", statusCode);
        respond.addProperty("Info", info);
        respond.addProperty("ID", id);
        return respond;
    }

    @Override
    public JsonObject registerIOTDeviceUpdate(JsonObject data){
        return null;
    }

//    public static void main(String args[]) {
//        String s = "Uriah haze seharot bahaze";
//        Stack<Character> stack = new Stack<>();
//        String[] strArr =  s.split(" ");
//
//        for (int i = 0; i < strArr.length; i++) {
//            for (int j = 0; j < strArr[i].length(); j++) {
//                stack.push(strArr[i].charAt(j));
//            }
//            while (!stack.isEmpty()) {
//                System.out.print(stack.pop());
//            }
//            System.out.print(" ");
//        }
//    }
}
