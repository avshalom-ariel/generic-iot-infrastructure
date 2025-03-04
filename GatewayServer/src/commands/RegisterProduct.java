package commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import connectionService.RespondableChannel;
import db.MongoDBManager;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RegisterProduct implements Command {
    private final MongoDBManager mongoDBManager = MongoDBManager.getInstance();
    private final JsonObject data;

    public RegisterProduct(JsonObject data) {
        this.data = data;
        data.addProperty("DB_type", "mysql");

    }

    @Override
    public void execute(RespondableChannel respondableChannel) {
        JsonObject respond = mongoDBManager.registerProduct(data);
        System.out.println("respond is: " + respond);

        try {
            respondableChannel.respond(ByteBuffer.wrap(new Gson().toJson(respond).getBytes()));
        } catch (IOException e) {
            System.out.println("Cannot respond in execute");
            throw new RuntimeException(e);
        }
    }
}
