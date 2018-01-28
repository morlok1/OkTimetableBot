package Bot;

import Bot.Commands.Strategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static Data.ConstantManager.*;


/**
 * Класс, осуществляющий обработку входящих на сервер запросов:
 * -webhook`и от ОК
 * -запрос на передачу фавикона (но это не точно)
 * -запрос на передачу html-страницы с формой ввода расписания на неделю
 * -запрос на обработку переданой формы
 * -
 */
public class BotHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(BotHandler.class);
    private BotServer server;
    private Bot bot;


    public BotHandler(BotServer server) {
        this.server = server;
        bot = Bot.getInstance();
    }

    /**
     * Определяет тип входящего запроса и передает управление соответствующему методу
     * Типы:
     * -запрос фавикона
     * -webhook от ОК
     * -запрос на передачу html-страницы
     * -запрос на обработку формы
     * -неизвестный запрос - ничего не передаём и удивляемся в логи
     * @param httpExchange
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange httpExchange) {


        if (httpExchange.getRequestURI().toString().equals(okEndpointURI)) {
            //Отвечаем в ОК
            log.info("Come message from OK");
            parseMessageFromOk(httpExchange);
        } else {
            //Это какой-то странный запрос - пока просто игнорим и светим в лог
            log.warn("Unexpected request (probably, the Chinese)");
            log.warn("URI: " + httpExchange.getRequestURI().toString());
        }

    }

    /**
     * Производит разбор сообщения от ОК, определяя цель сообщения:
     * -Создание расписание
     * -Затирание старого расписания и создания нового
     * -Вывод результирующего расписания
     * -Неизвестное сообщение - вывод информации о синтаксисе запросов бота
     * @param httpExchange
     * @throws IOException
     */
    private void parseMessageFromOk(HttpExchange httpExchange) {

        String body = "";

        try (InputStream is = httpExchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            body = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Вернуть 200
        byte[] bytes = "OK".getBytes();

        try {
            httpExchange.sendResponseHeaders(200, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(bytes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


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


        for (int i=0; i<Strategy.countOfCommand(); i++) {
            if (Strategy.getCommandByIndex(i).matches(message)) {
                server.sendMessage(Strategy.getCommandByIndex(i).execute(chatId, bot), chatId);
                break;
            }
        }

/*        if (Strategy.getCommandByIndex(0).matches(message)) {
            server.sendMessage(Strategy.getCommandByIndex(0).execute(chatId, bot), chatId);

        } else if (Strategy.getCommandByIndex(1).matches(message)) {
            server.sendMessage(Strategy.getCommandByIndex(1).execute(chatId, bot), chatId);

        } else if (Strategy.getCommandByIndex(2).matches(message)) {
            server.sendMessage(Strategy.getCommandByIndex(2).execute(chatId, bot), chatId);

        } else if (Strategy.getCommandByIndex(3).matches(message)) {
            server.sendMessage(Strategy.getCommandByIndex(3).execute(chatId, bot), chatId);

        } else if (Strategy.getCommandByIndex(4).matches(message)) {
            server.sendMessage(Strategy.getCommandByIndex(4).execute(chatId, bot), chatId);

        } else if (Strategy.getCommandByIndex(5).matches(message)){
            server.sendMessage(Strategy.getCommandByIndex(5).execute(chatId, bot), chatId);
        }*/
    }



}
