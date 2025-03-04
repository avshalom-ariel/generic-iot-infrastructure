package parser;

import java.nio.ByteBuffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JsonParser implements Parser {

    @Override
    public JsonObject parse(ByteBuffer request) {
        String jsonString = new String(request.array()).trim();
        return new Gson().fromJson(jsonString, JsonObject.class);
    }

    public static ByteBuffer jsonToByteBuffer(JsonObject json) {
        return ByteBuffer.wrap(new Gson().toJson(json).getBytes());
    }

    public static String jsonToString(JsonObject json) {
        return new Gson().toJson(json);
    }

}