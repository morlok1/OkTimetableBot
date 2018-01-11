package Bot;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BotServer {

    private static BotServer theInstance;

    private final HttpServer server;
    private final BotClient botClient;

    public static BotServer getInstance() throws IOException {
        if (theInstance == null) {
            theInstance = new BotServer();
        }

        return theInstance;
    }


    private BotServer() throws IOException {

        server = HttpServer.create();
        server.bind(new InetSocketAddress(10001),10);
        server.createContext("/", new BotHandler());
        server.start();

        System.out.println("Server started.");

        botClient = BotClient.getInstance(); //Встретилось два одиночества
        botClient.setSubscribes();


    }

    public HttpServer getServer() {
        return server;
    }
}
