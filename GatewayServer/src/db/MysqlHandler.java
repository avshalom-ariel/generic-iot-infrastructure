package db;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.*;

public class MysqlHandler implements DBMSHandler{
    private static final String mysqlConnectionURL = "jdbc:mysql://localhost:3306/AdminDB";
    private final Connection connection;

    public MysqlHandler() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(mysqlConnectionURL, "avshalom", "1234");
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

            System.out.println("preparedStatement is: " + preparedStatement);
            int rowsEffected = preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                companyID = generatedKeys.getInt(1);
            }

            if(0 == rowsEffected){
                System.out.println("Zero rows effected!!!");
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

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, data.get("Contact_Name").getAsString());
        preparedStatement.setString(2, data.get("Contact_Number").getAsString());
        preparedStatement.setLong(3, companyID);
        preparedStatement.setString(4, data.get("Address").getAsString());
        preparedStatement.setString(5, data.get("Credit_Card").getAsString());
        preparedStatement.setDate(6, Date.valueOf(data.get("Expiry_Date").getAsString()));
        preparedStatement.setString(7, data.get("Security_Code").getAsString());

        System.out.println("preparedStatement in contacts is: " + preparedStatement);

        int rowsEffected = preparedStatement.executeUpdate();

        preparedStatement.close();
        if(0 == rowsEffected){
            System.out.println("0 rowsEffected");
            throw new RuntimeException("Failed to insert contact");
        }
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
            return respond(400, "Duplicate Product");
        }
    }

    @Override
    public JsonObject getCompany(JsonObject jsonObject) {
        Integer companyID = jsonObject.get("Company_ID").getAsInt();
        String findCompanyIDQuery = "SELECT Company_Name FROM Companies WHERE Company_ID = ?;";

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
                row.addProperty("Company_Name", resultSet.getString("Contact_Name"));
                row.addProperty("Contact_Name", resultSet.getString("Contact_Name"));
                row.addProperty("Contact_Number", resultSet.getInt("Contact_Number"));
                row.addProperty("Address", resultSet.getString("Address"));
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
        Integer productID = jsonObject.get("Product_ID").getAsInt();
//        Integer companyID = jsonObject.get("Company_ID").getAsInt();
        JsonObject respond = new JsonObject();

        try {
            String getProductQuery = "SELECT Product_Name, Description FROM Products WHERE Product_ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(getProductQuery);
            preparedStatement.setString(1, productID.toString());
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
        try {
            String getProductsQuery = "SELECT Product_Name, Description FROM Products WHERE Company_ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(getProductsQuery);
            preparedStatement.setString(1, companyID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            JsonObject respond = new JsonObject();
            int i = 1;
            while(resultSet.next()){
                System.out.println("result set not empty!");
                JsonObject product = new JsonObject();
                product.addProperty("Product_Name", resultSet.getString("Product_Name"));
                product.addProperty("Description", resultSet.getString("Description"));
                respond.add("Product" + i, product);
                ++i;
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

    private ResultSet getResult(String query, String val) throws SQLException {
        PreparedStatement prepStm = connection.prepareStatement(query);
        prepStm.setString(1, val);
        return prepStm.executeQuery();
    }

    private JsonObject respond(int statusCode, String info){
        return new Gson().fromJson("{'StatusCode': " + statusCode + ", " +
                "'Info': '" + info + "'}", JsonObject.class);
    }

    private JsonObject respond(int statusCode, JsonObject info){
        return new Gson().fromJson("{'StatusCode': " + statusCode + ", " +
                "'Info': '" + info + "'}", JsonObject.class);
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
