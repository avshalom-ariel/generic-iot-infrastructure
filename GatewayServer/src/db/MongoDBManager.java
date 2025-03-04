package db;

import com.google.gson.JsonObject;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBManager implements DBMSHandler {
    private static final String connectionString = "mongodb+srv://avshalomariel:1234@cluster0.z9q8z.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private final MongoClient mongoClient;

    private MongoDBManager() {
        try {
            mongoClient = MongoClients.create(connectionString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class InstanceHolder {
        private static final MongoDBManager INSTANCE = new MongoDBManager();
    }

    public static MongoDBManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public JsonObject registerCompany(JsonObject jsonObject) {
//        String companyID = jsonObject.get("Company_ID").getAsString();
//        JsonObject response = new JsonObject();
//
//        boolean exists = checkDBExistence(companyID);
//        if (exists) {
//            response.addProperty("Status", "Company already exists");
//            return respond(400, response);
//        } else {
//            response.addProperty("Status", "success");
//            return respond(400, response);
//        }
        return null;
    }

    @Override
    public JsonObject registerProduct(JsonObject data) {
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        boolean isInserted = false;
        try {
            String productID = data.get("Product_ID").getAsString();
            String productName = data.get("Product_Name").getAsString();
            String description = data.get("Description").getAsString();

            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> collection = database.getCollection("Products");

            InsertOneResult insertOneResult = collection.insertOne(new Document("_id", productID).append("Product_Name", productName).append("Company_ID", companyID).append("Product_ID", productID).append("Description", description));

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
    public JsonObject getCompany(JsonObject data) {return null;
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

    public JsonObject getIOTDevice(JsonObject data) {return null;}

    public JsonObject getIOTDeviceUpdate(JsonObject data) {
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();

        try {
            String deviceID = data.get("IOT_Device_ID").getAsString();
            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> collection = database.getCollection("IOT_Device_ID_" + deviceID);

            FindIterable<Document> result = collection.find(eq("_id", data.get("IOT_Device_Update_ID").getAsInt()));

            int updateCount = 0;
            for (Document document : result) {
                response.addProperty("IOT_Device_ID_Update_" + data.get("IOT_Device_Update_ID").getAsInt(), document.toString());
                ++updateCount;
            }

            if (0 == updateCount) {
                return respond(400, "No update found");
            }
        } catch(Exception e) {
            return respond(400, "No update found");
        }

        return respond(200, response);
    }

    public JsonObject getIOTDeviceUpdates(JsonObject data) {
        String companyID = data.get("Company_ID").getAsString();
        JsonObject response = new JsonObject();
        try {
            String deviceID = data.get("IOT_Device_ID").getAsString();
            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> collection = database.getCollection("IOT_Device_ID_" + deviceID);

            FindIterable<Document> result = collection.find();

            int updateCount = 0;
            for (Document document : result) {
                response.addProperty("IOT_Device_ID_Update_" + updateCount, document.toString());
                ++updateCount;
            }

            if (0 == updateCount) {
                return respond(400, "No update found");
            }

        } catch(Exception e) {
            return respond(400, "Server failure. please check parameters");
        }

        return respond(200, response);
    }

    @Override
    public JsonObject registerIOTDevice(JsonObject data){
        String companyID = data.get("Company_ID").getAsString();
        String productID = data.get("Product_ID").getAsString();
        JsonObject response = new JsonObject();

//        boolean exists = checkDBExistence(companyID);
//        if (!exists) {
//            response.addProperty("Status", "Company not registered yet, please check company id");
//            return respond(400, response);
//        }
//        boolean exists = checkProductExistence(companyID, productID);
//        if (!exists) {
//            response.addProperty("Status", "Company not registered yet, please check company id");
//            return respond(400, response);
//        }

        boolean isInserted = false;
        try {
            String iotDeviceID = data.get("IOT_Device_ID").getAsString();

            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> deviceCollection = database.getCollection("IOT_Device_ID_" + iotDeviceID);
            InsertOneResult insertOneResult = deviceCollection.insertOne(new Document("_id", iotDeviceID).append("IOT_Device_ID", iotDeviceID).append("Product_ID", productID));

            isInserted = insertOneResult.wasAcknowledged();
            System.out.println("insert status is: " + isInserted);

            response.addProperty("Product_ID", productID);
            response.addProperty("IOT_Device_ID", iotDeviceID);
        } catch(Exception e) {
            return respond(400, "Server failure. please check parameters");
        }

        if (isInserted) {
            return respond(200, response);
        } else {
            return respond(400, response);
        }
    }

    @Override
    public JsonObject registerIOTDeviceUpdate(JsonObject data){
        String companyID = data.get("Company_ID").getAsString();
        String iotDeviceID = data.get("IOT_Device_ID").getAsString();
        JsonObject response = new JsonObject();


//        boolean exists = checkDeviceExistence(companyID, iotDeviceID);
//        if (!exists) {
//            response.addProperty("Status", "Company not registered yet, please check company id");
//            return respond(400, response);
//        }

        boolean isInserted = false;
        try {

            JsonObject iotDeviceUpdate = data.get("IOT_Device_Update").getAsJsonObject();

            int updateId = iotDeviceUpdate.get("IOT_Device_Update_ID").getAsInt();
            iotDeviceUpdate.remove("IOT_Device_Update_ID");
            iotDeviceUpdate.remove("IOT_Device_ID");


            MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
            MongoCollection<Document> deviceCollection = database.getCollection("IOT_Device_ID_" + iotDeviceID);

            InsertOneResult insertOneResult = deviceCollection.insertOne(new Document("_id", updateId).append("IOT_Device_Update", iotDeviceUpdate.toString()));

            isInserted = insertOneResult.wasAcknowledged();

            response.addProperty("IOT_Device_ID", iotDeviceID);
            response.add("IOT_Device_Update", iotDeviceUpdate);
        } catch(Exception e) {
            return respond(400, "Server failure. please check parameters");
        }

        System.out.println("insert status is: " + isInserted);

        if (isInserted) {
            return respond(200, response);
        } else {
            return respond(400, response);
        }
    }

    public static void main(String[] args) {
        MongoDBManager handle = new MongoDBManager();

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

    private boolean checkProductExistence(String companyID, String productID) {
        MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
//        ListCollectionNamesIterable collectionNames = database.listCollectionNames();
//        for (String collectionName : collectionNames) {
//            if (collectionName.equals("Product_ID_" + productID)) {
//                return true;
//            }
//        }
//        return false;
        MongoCollection<Document> productCollection = database.getCollection("Products");
        long count = productCollection.countDocuments(new Document().append("Product_ID", productID));
        if (0 == count) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkDeviceExistence(String companyID, String iotDeviceID) {
        MongoDatabase database = mongoClient.getDatabase("Company_ID_" + companyID);
        ListCollectionNamesIterable collectionNames = database.listCollectionNames();
        for (String collectionName : collectionNames) {
            if (collectionName.equals("IOT_Device_ID_" + iotDeviceID)) {
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

    private JsonObject respond(int statusCode, String info){
        JsonObject respond = new JsonObject();
        respond.addProperty("StatusCode", statusCode);
        respond.addProperty("Info", info);
        return respond;
    }
}
