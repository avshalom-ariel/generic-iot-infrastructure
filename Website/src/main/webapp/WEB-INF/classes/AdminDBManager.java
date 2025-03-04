package classes;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class AdminDBManager {
    private final HashMap<String, DBMSHandler> dbHandlerMap = new HashMap<>();

    private AdminDBManager(){}

    public static AdminDBManager getInstance(){
        return InstanceHolder.INSTANCE;
    }
    
    private static class InstanceHolder{
    	private static final AdminDBManager INSTANCE = new AdminDBManager();
        static {
            try {
                INSTANCE.addDBHandler("mysql", new MysqlHandler());
                INSTANCE.addDBHandler("mongodb", new MongoDBHandler());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addDBHandler(String key, DBMSHandler handler){
        dbHandlerMap.put(key, handler);
    }

    public JsonObject registerCompany(JsonObject jsonObject){
        String dbType = jsonObject.get("DB_type").getAsString();
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }

        return handler.registerCompany(jsonObject);
    }

    public JsonObject registerProduct(JsonObject jsonObject){
        String dbType = (jsonObject.get("DB_type").getAsString());
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }

        return handler.registerProduct(jsonObject);
    }

    public JsonObject getCompany(JsonObject jsonObject){
        String dbType = (jsonObject.get("DB_type").getAsString());
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }

        return handler.getCompany(jsonObject);
    }

    public JsonObject getCompanies(JsonObject jsonObject){
        String dbType = (jsonObject.get("DB_type").getAsString());
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }

        return handler.getCompanies(jsonObject);
    }

    public JsonObject getProduct(JsonObject jsonObject){

        String dbType = (jsonObject.get("DB_type").getAsString());
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }
        System.out.println(dbType);

        return handler.getProduct(jsonObject);
    }

    public JsonObject getProducts(JsonObject jsonObject){
        String dbType = (jsonObject.get("DB_type").getAsString());
        DBMSHandler handler = dbHandlerMap.get(dbType);
        if(null == handler){
            throw new RuntimeException("Bad DB type");
        }

        return handler.getProducts(jsonObject);
    }
}
