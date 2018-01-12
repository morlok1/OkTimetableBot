package Bot;

import Data.ConstantManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class BotHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(BotHandler.class);

    private BotServer server;
    private Bot bot;

    public BotHandler(BotServer server) throws IOException {
        this.server = server;
        bot = Bot.getInstance();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        if (httpExchange.getRequestURI().toString().equals("/forOkWebhooks")) {
            //Отвечаем в ОК
            parseMessageFromOk(httpExchange);
        } else if (httpExchange.getRequestURI().toString().contains("/?id=")) {
            //Отдаем html-страницу
            sendWebForm(httpExchange);

        } else if (httpExchange.getRequestURI().toString().contains("monday")) {
            //Записываем результаты для заданной группы
            parseMessageFromWebForm(httpExchange);
        } else {
            //Это какой-то странный запрос - пока просто игнорим и светим в лог
            log.warn("Unexpected request (probably, the Chinese)");
            log.warn("URI: " + httpExchange.getRequestURI().toString());
        }

    }

    private void parseMessageFromWebForm(HttpExchange httpExchange) {
        String uri = httpExchange.getRequestURI().toString();
        log.info("Parse web-form answer: " + uri);

        //Парсим чиселки

    }

    private void sendWebForm(HttpExchange httpExchange) throws IOException {

        String chatId = httpExchange.getRequestURI().toString();
        chatId = chatId.substring(chatId.indexOf("chat"));

        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream("html/index.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        OutputStream os = httpExchange.getResponseBody();
        String line;
        StringBuilder builder = new StringBuilder();

        log.info("Sending a web page with form");
        while ((line = reader.readLine()) != null) {
            if (line.equals("</form>")) {
                builder.append("<input type=\"input\" value=\"" + chatId + "\" name=\"id\" hidden>");
            }
            builder.append(line);
        }
        byte[] page = builder.toString().getBytes();
        httpExchange.sendResponseHeaders(200, page.length);
        os.write(page);
        os.close();

    }

    private void parseMessageFromOk(HttpExchange httpExchange) throws IOException {
        InputStream is = httpExchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String body = reader.readLine();


        String chatId = "";
        String userId = "";
        String message = "";

        //Вычленяем данные из запроса
        int index = body.indexOf("user:");
        if (index > 0) {
            userId = body.substring(index, body.indexOf("\"",index));
        }
        index = body.indexOf("chat:");
        if (index > 0) {
            chatId = body.substring(index, body.indexOf("\"",index));
        }
        index = body.indexOf("text");
        if (index > 0) {
            message = body.substring(index + 7, body.indexOf("seq") - 3);
        }

        log.info("New message in " + chatId + " from " + userId + ". message: " + message);

        //Вернуть 200
        byte[] bytes = "OK".getBytes();
        httpExchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();

        if (FuzzySearch.ratio(message, ConstantManager.linkRequest) > 80) {
            //Пришел запрос на создание нового расписания
            log.info("New group for " + chatId + " created.");
            server.sendMessage(bot.generateNewGroup(chatId), chatId);

        } else if (FuzzySearch.ratio(message, ConstantManager.resultRequest) > 80) {
            //Пришел запрос на выдачу результирующего расписания
            log.info("Result timetable for " + chatId + " generated.");
            server.sendMessage(bot.getTimetable(chatId), chatId);
        } else {
            //Отправляем информационное сообщение
            log.info("Information message to " + chatId + " sended.");
            server.sendMessage("Отправьте 'Расписание', чтобы начать составлять новое расписание, или 'Результаты' чтобы получить результаты.", chatId);
        }
    }


}
