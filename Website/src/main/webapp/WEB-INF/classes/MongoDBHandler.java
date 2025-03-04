package classes;

import com.mongodb.*;


import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import com. mongodb. client. MongoIterable;

public class MongoDBHandler implements DBMSHandler{
    private static final String connectionString = "mongodb+srv://avshalomariel:1234@cluster0.z9q8z.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private final MongoClient mongoClient;

    public MongoDBHandler() {
        try {
            mongoClient = MongoClients.create(connectionString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject registerCompany(JsonObject jsonObject) {
        String companyID = jsonObject.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        boolean exists = checkDBExistence(companyID);
        if (exists) {
            response.addProperty("Status", "Company already exists");
            return respond(400, response);
        } else {
            response.addProperty("Status", "success");
            return respond(400, response);
        }
    }

    @Override
    public JsonObject registerProduct(JsonObject data) {
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        boolean exists = checkDBExistence(companyID);
        if (exists) {
            response.addProperty("Status", "Company not registered yet, please check company id");
            return respond(400, response);
        }

        boolean isInserted = false;
        try {
            String productID = data.get("Product_ID").getAsString();
            String productName = data.get("Product_Name").getAsString();
            String description = data.get("Description").getAsString();

            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> collection = database.getCollection("Products");
            InsertOneResult insertOneResult = collection.insertOne(new Document().append("Product_Name", productName).append("Company_ID", companyID).append("Product_ID", productID).append("Description", description));

            isInserted = insertOneResult.wasAcknowledged();
            System.out.println("insert status is: " + isInserted);

            response.addProperty("Product_ID", productID);
            response.addProperty("Product_Name", productName);
            response.addProperty("Description", description);
        } catch(Exception e) {
            response.addProperty("Exception", e.getMessage());
        }

        if (isInserted) {
            return respond(200, response);
        } else {
            return respond(400, response);
        }
    }

    @Override
    public JsonObject getCompany(JsonObject data) {
        return null;
    }

    @Override
    public JsonObject getCompanies(JsonObject data) {
        return null;
    }

    @Override
    public JsonObject getProduct(JsonObject data) {
        return null;
    }

    @Override
    public JsonObject getProducts(JsonObject data) {
        return null;
    }

    @Override
    public JsonObject registerIOTDevice(JsonObject data){
        MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        boolean exists = checkDBExistence(companyID);
        if (exists) {
            response.addProperty("Status", "Company not registered yet, please check company id");
            return respond(400, response);
        }

        boolean isInserted = false;
        try {
            String productID = data.get("Product_ID").getAsString();
            String iotDeviceID = data.get("IOT_Device_ID").getAsString();

            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> deviceCollection = database.getCollection("IOT_Device_ID_" + iotDeviceID);
            InsertOneResult insertOneResult = deviceCollection.insertOne(new Document().append("IOT_Device_ID", iotDeviceID).append("Product_ID", productID));

            isInserted = insertOneResult.wasAcknowledged();
            System.out.println("insert status is: " + isInserted);

            response.addProperty("Product_ID", productID);
            response.addProperty("IOT_Device_ID", iotDeviceID);
        } catch(Exception e) {
            response.addProperty("Exception", e.getMessage());
        }

        if (isInserted) {
            return respond(200, response);
        } else {
            return respond(400, response);
        }
    }

    @Override
    public JsonObject registerIOTDeviceUpdate(JsonObject data){
        MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        boolean exists = checkDBExistence(companyID);
        if (exists) {
            response.addProperty("Status", "Company not registered yet, please check company id");
            return respond(400, response);
        }

        boolean isInserted = false;
        try {
            String iotDeviceID = data.get("IOT_Device_ID").getAsString();
            String iotDeviceUpdate = data.get("IOT_Device_Update").getAsString();

            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> deviceCollection = database.getCollection("IOT_Device_ID_" + iotDeviceID);
            InsertOneResult insertOneResult = deviceCollection.insertOne(new Document().append("IOT_Device_ID", iotDeviceID).append("IOT_Device_Update", iotDeviceUpdate));

            isInserted = insertOneResult.wasAcknowledged();

            response.addProperty("IOT_Device_ID", iotDeviceID);
            response.addProperty("IOT_Device_Update", iotDeviceUpdate);
        } catch(Exception e) {
            response.addProperty("Exception", e.getMessage());
        }

        System.out.println("insert status is: " + isInserted);

        if (isInserted) {
            return respond(200, response);
        } else {
            return respond(400, response);
        }
    }

    public static void main(String[] args) {
        MongoDBHandler handle = new MongoDBHandler();

        JsonObject productJson = new JsonObject();
        productJson.addProperty("Company_ID", "2");
        productJson.addProperty("Product_ID", "1");
        productJson.addProperty("Product_Name", "Idodos pans");
        productJson.addProperty("Description", "white pans");

        JsonObject iotDeviceJson = new JsonObject();
        iotDeviceJson.addProperty("Company_ID", "2");
        iotDeviceJson.addProperty("Product_ID", "1");
        iotDeviceJson.addProperty("IOT_Device_ID", "1");

        JsonObject iotDeviceUpdateJson = new JsonObject();
        iotDeviceUpdateJson.addProperty("Company_ID", "2");
        iotDeviceUpdateJson.addProperty("IOT_Device_ID", "1");
        iotDeviceUpdateJson.addProperty("IOT_Device_Update", "update1");

        handle.registerProduct(productJson);
        handle.registerIOTDevice(iotDeviceJson);
        handle.registerIOTDeviceUpdate(iotDeviceUpdateJson);

    }

    private boolean checkDBExistence(String companyID) {
        MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
        for (String dbName : databaseNames) {
            if (dbName.equals("Company_ID_" + companyID)) {
                return true;
            }
        }
        return false;
    }

    private JsonObject respond(int statusCode, JsonObject info){
        JsonObject respond = new JsonObject();
        respond.addProperty("StatusCode", statusCode);
        respond.add("Info", info);
        return respond;
    }
}
