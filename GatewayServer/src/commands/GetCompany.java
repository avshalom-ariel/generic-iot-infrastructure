package commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import connectionService.RespondableChannel;
import db.AdminDBManager;

import java.io.IOException;
import java.nio.ByteBuffer;


public class GetCompany implements Command {
    private final AdminDBManager adminDBManager = AdminDBManager.getInstance();
    private final JsonObject data;

    public GetCompany(JsonObject data) {
        this.data = data;
        data.addProperty("DB_type", "mysql");
    }

    @Override
    public void execute(RespondableChannel respondableChannel) {
        JsonObject respond = adminDBManager.getCompany(data);
        System.out.println("respond is: " + respond);

        try {
            respondableChannel.respond(ByteBuffer.wrap(new Gson().toJson(respond).getBytes()));
        } catch (IOException e) {
            System.out.println("Cannot respond in execute");
            throw new RuntimeException(e);
        }
    }
}
