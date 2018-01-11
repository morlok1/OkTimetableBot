package Bot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class BotHandler implements HttpHandler {

    private BotClient client;

    public BotHandler() {
        client = BotClient.getInstance();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream is = httpExchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        String chatId = "";
        String userId = "";
        String message = "";

        //Вычленяем данные из запроса
        while ((line = reader.readLine()) != null) {
            int index = line.indexOf("user:");
            if (index > 0) {
                userId = line.substring(index, line.indexOf("\"",index));
            }
            index = line.indexOf("chat:");
            if (index > 0) {
                chatId = line.substring(index, line.indexOf("\"",index));
            }
            index = line.indexOf("text");
            if (index > 0) {
                message = line.substring(index + 7, line.indexOf("seq") - 3);
                break;
            }
        }
        System.out.println("user: " + userId + " chat: " + chatId + " message: " + message);

        //Вернуть 200
        byte[] bytes = "OK".getBytes();
        httpExchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();

        //Здесь какая-то работа бота
        client.sendMessage("Ответ на всё - Любовь.", chatId);
    }
}
