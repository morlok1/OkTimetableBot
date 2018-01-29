package Bot;

import Bot.Commands.Strategy;
import Domain.RequestData;
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

        String body = processRequest(httpExchange);

        if (httpExchange.getRequestURI().toString().equals(okEndpointURI)) {
            //Отвечаем в ОК
            log.info("Come message from OK");
            RequestData requestData = parseMessageFromOk(body);
            server.sendOkMessages(botWork(requestData), requestData.getChatId());

        } else if (httpExchange.getRequestURI().toString().equals(vkEndpointURI)) {
            log.info("Come message from VK");
            RequestData requestData = parseMessageFromVk(body);
            server.sendVkMessages(botWork(requestData), requestData.getChatId());
        } else {
            //Это какой-то странный запрос - пока просто игнорим и светим в лог
            log.warn("Unexpected request (probably, the Chinese)");
            log.warn("URI: " + httpExchange.getRequestURI().toString());
            return;
        }



    }

    private RequestData parseMessageFromVk(String body) {

        String chatId = "";
        String message = "";

        int index = body.indexOf("user_id") + 9;
        if (index > 0) {
            chatId = body.substring(index, body.indexOf(",",index));
        }
        index = body.indexOf("body") + 7;
        if (index > 0) {
            message = body.substring(index, body.indexOf("\"},"));
        }

        log.info("[VK] New message from " + chatId + ". message: " + message);

        return new RequestData(message, chatId);
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
    private RequestData parseMessageFromOk(String body) {

        String chatId = "";
        String message = "";

        //Вычленяем данные из запроса
        int index = body.indexOf("chat:");
        if (index > 0) {
            chatId = body.substring(index, body.indexOf("\"",index));
        }
        index = body.indexOf("text") + 7;
        if (index > 0) {
            message = body.substring(index, body.indexOf("seq") - 3);
        }

        log.info("[OK] New message from " + chatId + ". message: " + message);

        return new RequestData(message, chatId);
    }

    /**
     * Отвечаем на полученный callback кодом 200
     * Извлекаем и возвращаем тело запроса
     * @param httpExchange
     * @return - тело запрсоа
     */
    private String processRequest(HttpExchange httpExchange) {
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
        return body;
    }

    /**
     * Обработка команды ботом
     * @param requestData
     * @return
     */
    private String[] botWork(RequestData requestData) {
        String result[] = {"Unknown request format"};
        for (int i=0; i<Strategy.countOfCommand(); i++) {
            if (Strategy.getCommandByIndex(i).matches(requestData.getMessage())) {
                result = Strategy.getCommandByIndex(i).execute(requestData.getChatId(), bot);
                break;
            }
        }

        return result;
    }


}
