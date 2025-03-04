package connectionService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import rps.RPS;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ConnectionService {
    private static final int BUFFER_SIZE = 1024*64;
    private final Thread selectorThread = new Thread(this::serverRoutine);
    private volatile boolean isRunning = false;
    private boolean hasStarted = false;
    private final Selector selector;
    private final HTTPService httpService;
    private final RPS rps;

    public ConnectionService(RPS rps, String ip) {
        if (null == rps) {
            throw new NullPointerException("RPS is null");
        }
        this.rps = rps;

        try {
            selector = Selector.open();
            httpService = new HTTPService(InetAddress.getByName(ip), 8000);
        } catch (IOException e) {
            throw new RuntimeException("couldn't open selector");
        }
    }

    /////////////// Server Methods ////////////////

    public void start() {
        if (hasStarted) {
            throw new IllegalStateException("connection service has started, please create a new one");
        }

        hasStarted = true;
        isRunning = true;
        selectorThread.start();
        httpService.start();
    }

    public void stop() throws IOException, InterruptedException {
        isRunning = false;
        selector.wakeup();
        selectorThread.join();
        for (SelectionKey key : selector.keys()) {
            key.channel().close();
        }
        selector.close();
    }

    public void serverRoutine() {
        while (isRunning) {
            try {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (isRunning && iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    ((ChannelHandler) key.attachment()).handle();

                    if (!key.channel().isOpen()) {
                        key.cancel();
                    }
                }

            } catch (IOException e) {

            }
        }
    }

    /////////////// Add Address ////////////////

    public void addTCP(int port, InetAddress ip) throws IOException {
        if (hasStarted) {
            throw new IllegalStateException("connection service has started");
        }
        if (80 == port) {
            throw new IllegalArgumentException("port is saved for http");
        }

        ServerSocketChannel tcpServer = ServerSocketChannel.open();
        tcpServer.bind(new InetSocketAddress(ip, port));
        tcpServer.configureBlocking(false);
        tcpServer.register(selector, SelectionKey.OP_ACCEPT, new TCPAcceptor(tcpServer));
    }

    public void addUDP(int port, InetAddress ip) throws IOException {
        if (hasStarted) {
            throw new IllegalStateException("connection service has started");
        }
        if (80 == port) {
            throw new IllegalArgumentException("port is saved for http");
        }

        DatagramChannel udpServer = DatagramChannel.open();
        InetSocketAddress address = new InetSocketAddress(ip, port);
        udpServer.bind(address);
        udpServer.configureBlocking(false);
        udpServer.register(selector, SelectionKey.OP_READ, new UDPChannel(udpServer));
    }

    /////////////// TCP Acceptor ////////////////


    private class TCPAcceptor implements ChannelHandler {
        private final ServerSocketChannel serverSocketChannel;

        public TCPAcceptor(ServerSocketChannel serverSocketChannel) {
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void handle() throws IOException {
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ, new TCPChannel(client));
            System.out.println("New client connected");
        }
    }

    /////////////// TCP Handler ////////////////

    private class TCPChannel implements ChannelHandler, RespondableChannel {
        private final SocketChannel client;

        public TCPChannel(SocketChannel channel) {
            this.client = channel;
        }

        @Override
        public void handle() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            if (client.read(buffer) == -1) {
                client.close();
                return;
            }
            System.out.println("TCP received");
            buffer.flip();
            rps.handleRequest(this, buffer);
        }

        @Override
        public void respond(ByteBuffer bytes) throws IOException {
            while (bytes.hasRemaining()) {
                client.write(bytes);
            }
        }
    }

    /////////////// UDP Handler ////////////////

    private class UDPChannel implements ChannelHandler, RespondableChannel {
        private final InetSocketAddress clientAddress;
        private final DatagramChannel channel;

        public UDPChannel(DatagramChannel channel) {
            this.channel = channel;
            clientAddress = null;
        }

        public UDPChannel(DatagramChannel channel, InetSocketAddress clientAddress) {
            this.channel = channel;
            this.clientAddress = clientAddress;
        }

        @Override
        public void handle() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);
            if (clientAddress == null) {
                return;
            }
            rps.handleRequest(new UDPChannel(channel, clientAddress), buffer);
            System.out.println("UDP received");

        }

        @Override
        public void respond(ByteBuffer bytes) throws IOException {
            while (bytes.hasRemaining()) {
                channel.send(bytes, clientAddress);
            }
            bytes.clear();
        }
    }

    /////////////// HTTP Handler ////////////////
    private static final int COMPANY_INDEX = 1;
    private static final int COMPANY_ID_INDEX = 2;
    private static final int PRODUCT_INDEX = 3;
    private static final int PRODUCT_ID_INDEX = 4;
    private static final int IOT_DEVICE_INDEX = 5;
    private static final int IOT_DEVICE_ID_INDEX = 6;
    private static final int IOT_DEVICE_UPDATE_INDEX = 7;
    private static final int IOT_DEVICE_UPDATE_ID_INDEX = 8;


    private class HTTPService {
        private final HttpServer server;

        private HTTPService(InetAddress ip, int port) throws IOException {
            server = HttpServer.create(new InetSocketAddress(ip, port), 0);
            addContext("/", new CompanyHandler());
        }

        public void addContext(String path, HttpHandler handler) {
            server.createContext(path, handler);
        }

        public void start() {
            server.setExecutor(null); // Use the default executor
            server.start();
        }
    }

    private class CompanyHandler implements HttpHandler, RespondableChannel {
        private final UrlRootHandler rootHandler = new UrlRootHandler();
        private HttpExchange exchange;

        public CompanyHandler() {
        }

        public CompanyHandler(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("HTTP request received");
            try {
                rootHandler.handle(exchange);
            } catch (IOException e) {
                System.out.println("Error handling request: " + e.getMessage());
                JsonObject response = new JsonObject();
                response.addProperty("StatusCode", 400);
                respond(ByteBuffer.wrap(new Gson().toJson(response).getBytes()));
            }
        }

        @Override
        public void respond(ByteBuffer buffer) throws IOException {
            String jsonString = new String(buffer.array()).trim();
            JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

            String response = "Hello, this is HTTP server response!" + "\n" + jsonObject; //.get("Info").getAsString();
            int statusCode = jsonObject.get("StatusCode").getAsInt();

            exchange.sendResponseHeaders(statusCode, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private interface UrlHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private class UrlRootHandler implements UrlHandler {
        private final UrlHandler urlCompanyGetHandler = new UrlCompanyGetHandler();
        private final UrlHandler urlCompanyPostHandler = new UrlCompanyPostHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                urlCompanyGetHandler.handle(exchange);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                urlCompanyPostHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlCompanyGetHandler implements UrlHandler {
        private final UrlHandler urlProductGetHandler = new UrlProductGetHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if(3 == urlParsed.length && "company".equals(urlParsed[COMPANY_INDEX])) {
                jsonObject.addProperty("Key", "GetCompany");

                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                jsonObject.add("Data", data);

                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if (2 == urlParsed.length && "companies".equals(urlParsed[COMPANY_INDEX])) {
                jsonObject.addProperty("Key", "GetCompanies");

                jsonObject.add("Data", data);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if (3 < urlParsed.length && "company".equals(urlParsed[COMPANY_INDEX])) {
                urlProductGetHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlCompanyPostHandler implements UrlHandler {
        private final UrlHandler urlProductPostHandler = new UrlProductPostHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();

            if (2 == urlParsed.length && "company".equals(urlParsed[COMPANY_INDEX])) {
                jsonObject.addProperty("Key", "RegisterCompany");
                JsonObject requestBody = getRequestBody(exchange);
                jsonObject.add("Data", requestBody);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if (2 < urlParsed.length && "company".equals(urlParsed[COMPANY_INDEX])) {
                urlProductPostHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlProductGetHandler implements UrlHandler {
        UrlHandler urlIOTDeviceGetHandler = new UrlIOTDeviceGetHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if (5 == urlParsed.length && "product".equals(urlParsed[PRODUCT_INDEX])) {

                jsonObject.addProperty("Key", "GetProduct");

//                JsonObject data = getRequestBody(exchange);
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);
                jsonObject.add("Data", data);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));

            } else if (4 == urlParsed.length && "products".equals(urlParsed[PRODUCT_INDEX])) {

                jsonObject.addProperty("Key", "GetProducts");

//                JsonObject data = getRequestBody(exchange);
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                jsonObject.add("Data", data);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));

            } else if (5 < urlParsed.length && "product".equals(urlParsed[PRODUCT_INDEX])) {
                urlIOTDeviceGetHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlProductPostHandler implements UrlHandler {
        UrlHandler urlIOTDevicePostHandler = new UrlIOTDevicePostHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();

            if (4 == urlParsed.length && "product".equals(urlParsed[PRODUCT_INDEX])) {
                jsonObject.addProperty("Key", "RegisterProduct");

                JsonObject data = getRequestBody(exchange);
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                jsonObject.add("Data", data);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));

            } else if (4 < urlParsed.length && "product".equals(urlParsed[PRODUCT_INDEX])) {
                urlIOTDevicePostHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlIOTDeviceGetHandler implements UrlHandler {
        UrlHandler urlIoTDeviceUpdateGetHandler = new UrlIoTDeviceUpdateGetHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if (7 == urlParsed.length && "iot-device".equals(urlParsed[IOT_DEVICE_INDEX])) {
                jsonObject.addProperty("Key", "GetIOTDevice");
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);
                data.addProperty("IOT_Device_ID", urlParsed[IOT_DEVICE_ID_INDEX]);

                jsonObject.add("Data", data);

                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if (7 < urlParsed.length && "iot-devices".equals(urlParsed[IOT_DEVICE_INDEX])) {
                jsonObject.addProperty("Key", "GetIOTDevices");
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);

                jsonObject.add("Data", data);

                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if (6 < urlParsed.length && "iot-device".equals(urlParsed[IOT_DEVICE_INDEX])) {
                urlIoTDeviceUpdateGetHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlIOTDevicePostHandler implements UrlHandler {
        UrlHandler urlIoTDeviceUpdatePostHandler = new UrlIoTDeviceUpdatePostHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if (6 == urlParsed.length && "iot-device".equals(urlParsed[IOT_DEVICE_INDEX])) {
                JsonObject requestBody = getRequestBody(exchange);
                jsonObject.addProperty("Key", "RegisterIOTDevice");
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);
                data.addProperty("IOT_Device_ID", requestBody.get("IOT_Device_ID").getAsString());
                jsonObject.add("Data", data);
                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else if ("iot-device".equals(urlParsed[IOT_DEVICE_INDEX]) && 5 < urlParsed.length) {
                urlIoTDeviceUpdatePostHandler.handle(exchange);
            } else {
                respondError(exchange);
            }
        }
    }

    private class UrlIoTDeviceUpdateGetHandler implements UrlHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();
            if (9 == urlParsed.length && "iot-device-update".equals(urlParsed[IOT_DEVICE_UPDATE_INDEX])) {
                jsonObject.addProperty("Key", "GetIOTDeviceUpdate");
                data.addProperty("IOT_Device_Update_ID", urlParsed[IOT_DEVICE_UPDATE_ID_INDEX]);
            } else if (8 == urlParsed.length && "iot-device-updates".equals(urlParsed[IOT_DEVICE_UPDATE_INDEX])) {
                jsonObject.addProperty("Key", "RegisterIOTDeviceUpdates");
            } else {
                respondError(exchange);
            }

            data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
            data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);
            data.addProperty("IOT_Device_ID", urlParsed[IOT_DEVICE_ID_INDEX]);

            jsonObject.add("Data", data);

            rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
        }
    }

    private class UrlIoTDeviceUpdatePostHandler implements UrlHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] urlParsed = parsePath(exchange.getRequestURI().getPath());
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if (8 == urlParsed.length && "iot-device-update".equals(urlParsed[IOT_DEVICE_UPDATE_INDEX])) {
                jsonObject.addProperty("Key", "RegisterIOTDeviceUpdate");
                data.addProperty("Company_ID", urlParsed[COMPANY_ID_INDEX]);
                data.addProperty("Product_ID", urlParsed[PRODUCT_ID_INDEX]);
                data.addProperty("IOT_Device_ID", urlParsed[IOT_DEVICE_ID_INDEX]);

                JsonObject requestBody = getRequestBody(exchange);

                data.add("IOT_Device_Update", requestBody);
                jsonObject.add("Data", data);

                rps.handleRequest(new CompanyHandler(exchange), ByteBuffer.wrap(new Gson().toJson(jsonObject).getBytes()));
            } else {
                respondError(exchange);
            }
        }
    }

    private JsonObject getRequestBody(HttpExchange exchange) throws IOException {
        InputStream dataInputStream = exchange.getRequestBody();

        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(dataInputStream)).getAsJsonObject();

        return jsonObject;

//        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
//        dataInputStream.read(buffer.array());
//        String requestBodyString = new String(buffer.array()).trim();
//
//        return new Gson().fromJson(requestBodyString, JsonObject.class);
    }

    private void respondError(HttpExchange exchange) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("StatusCode", 400);
        response.addProperty("Info", "Invalid request");

        new CompanyHandler(exchange).respond(ByteBuffer.wrap(new Gson().toJson(response).getBytes()));
    }

    private String[] parsePath(String path) {
        return path.split("/");
    }
}