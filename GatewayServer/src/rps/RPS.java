package rps;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import commands.Command;
import connectionService.RespondableChannel;
import factory.Factory;
import parser.Parser;
import plugAndPlay.DirMonitor;
import plugAndPlay.DynamicJarLoader;
import threadPool.ThreadPool;

public class RPS {
    private final ThreadPool threadPool = new ThreadPool();
    private final Parser requestParser;
    private final Factory<String, Command, JsonObject> commandFactory = new Factory<>();
    private final DynamicJarLoader dynamicJarLoader;
    private final DirMonitor dirMonitor;
    private final String dirToWatch;

    public RPS(Parser requestParser, String dirToWatch) {

        this.requestParser = requestParser;
        dirMonitor = new DirMonitor(dirToWatch, this);
        dynamicJarLoader = new DynamicJarLoader("Command");
        this.dirToWatch = dirToWatch;
        startPlugAndPlayService();
    }

    public void handleRequest(RespondableChannel respondableChannel, ByteBuffer buffer) {
        threadPool.submit(new Request(respondableChannel, buffer));
    }

    public void shutdown() {
        threadPool.shutdown();
    }

    public void startPlugAndPlayService() {
        loadInitialCommands();

        threadPool.submit(()->{
            try {
                dirMonitor.watch();
        } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadInitialCommands() {
        File[] dir = new File(dirToWatch).listFiles();

        assert dir != null;

        for (File f : dir) {
            if (f.getName().endsWith(".jar")) {
                load(f.getName());
            }
        }
    }

    public void load(String jarAdded) {
        List<Class<?>> classes = dynamicJarLoader.load(dirToWatch + "/" + jarAdded);

        for (Class<?> clazz : classes) {
            System.out.println("Class loaded name:" + clazz.getSimpleName());

            Function<JsonObject, Command> createCommand = (JsonObject jsonObject)->{
                try {
                    return (Command) clazz.getConstructor(JsonObject.class).newInstance(jsonObject);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
            commandFactory.add(clazz.getSimpleName(), createCommand);
        }
    }

    private class Request implements Runnable {
        private final RespondableChannel respondableChannel;
        private final ByteBuffer buffer;

        public Request(RespondableChannel respondableChannel, ByteBuffer buffer) {
            this.respondableChannel = respondableChannel;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                JsonObject jsonObject = requestParser.parse(buffer);
                System.out.println(jsonObject);
                System.out.println(jsonObject.get("Data").getAsJsonObject());
                Command command = commandFactory.create(jsonObject.get("Key").getAsString(), jsonObject.get("Data").getAsJsonObject());
                command.execute(respondableChannel);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                JsonObject data = new JsonObject();
                data.addProperty("StatusCode", 400);
                data.addProperty("Info", "Invalid Request");
                String jsonString = new Gson().toJson(data);
                try {
                    respondableChannel.respond(ByteBuffer.wrap(jsonString.getBytes()));
                } catch (IOException ex) {
                    System.out.println("Cannot respond");
                }
            }
        }
    }
}
