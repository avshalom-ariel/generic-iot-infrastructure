package classes;

import com.google.gson.JsonObject;

public interface DBMSHandler {
    public JsonObject registerCompany(JsonObject jsonObject);
    public JsonObject registerProduct(JsonObject jsonObject);
    public JsonObject registerIOTDevice(JsonObject jsonObject);
    public JsonObject registerIOTDeviceUpdate(JsonObject jsonObject);
    public JsonObject getCompany(JsonObject jsonObject);
    public JsonObject getCompanies(JsonObject jsonObject);
    public JsonObject getProduct(JsonObject jsonObject);
    public JsonObject getProducts(JsonObject jsonObject);
}
