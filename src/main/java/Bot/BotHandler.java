package Bot;

import Data.ConstantManager;
import Domain.UsersTimetable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

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

        if (httpExchange.getRequestURI().toString().equals("/favicon.ico")) {
            //Отправим фавикон, что ли
            log.info("Request to send a favicon");
        } else if (httpExchange.getRequestURI().toString().equals("/forOkWebhooks")) {
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
        byte[] page = "Well Done! You can go away!".getBytes();
        OutputStream os = httpExchange.getResponseBody();
        try {
            httpExchange.sendResponseHeaders(200, page.length);
            os.write(page);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String uri = httpExchange.getRequestURI().toString();
        log.info("Parse web-form answer: " + uri);
        ///?user-name-value=Fs&monday=2&tuesday=2&wednesday=2&thursday=2&friday=3&saturday=2&sunday=2&id=chat%3AC3e11972edc00

        UsersTimetable.EmploymentState[] employmentStates = new UsersTimetable.EmploymentState[7];

        employmentStates[0] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("monday") + 7,uri.indexOf("monday") + 8)));
        employmentStates[1] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("tuesday") + 8,uri.indexOf("tuesday") + 9)));
        employmentStates[2] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("wednesday") + 10,uri.indexOf("wednesday") + 11)));
        employmentStates[3] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("thursday") + 9,uri.indexOf("thursday") + 10)));
        employmentStates[4] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("friday") + 7,uri.indexOf("friday") + 8)));
        employmentStates[5] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("saturday") + 9,uri.indexOf("saturday") + 10)));
        employmentStates[6] = getEmploementStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("sunday") + 7,uri.indexOf("sunday") + 8)));

        String chatID = uri.substring(uri.indexOf("&id=") + 4, uri.length()).replace("%3A",":");

        log.info("Parse web-form answer complete. Result: chatID:" + chatID + " timetable: " + Arrays.toString(employmentStates));

        bot.getGroupByChatID(chatID).addUser(employmentStates);
        //UsersTimetable user = new UsersTimetable();

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
            if (line.equals("<body onLoad=\"javascript:init()\">")) {
                builder.append("<form method=\"GET\" action=\"" + ConstantManager.serverURI + ":" + ConstantManager.port + "/\" name=\"web-form\">");
            }
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
            //Пришел запрос на создание первого расписания
            log.info("First group for " + chatId + " created.");
            server.sendMessage(bot.generateNewGroup(chatId), chatId);

        } else if (FuzzySearch.ratio(message, ConstantManager.resultRequest) > 80) {
            //Пришел запрос на выдачу результирующего расписания
            log.info("Result timetable for " + chatId + " generated.");
            server.sendMessage(bot.getTimetable(chatId), chatId);
        } else if (FuzzySearch.ratio(message, ConstantManager.clearRequest) > 90) {
            //Пришел запрос на создание нового расписания
            log.info("New group for " + chatId + "created");
            server.sendMessage(bot.clearAndGenerateGroup(chatId), chatId);
        } else {
                //Отправляем информационное сообщение
                log.info("Information message to " + chatId + " sended.");
                server.sendMessage("Отправьте 'Расписание', чтобы начать составлять новое расписание.\\n Отправьте 'Результаты' чтобы получить результаты.", chatId);
        }
    }

    private UsersTimetable.EmploymentState getEmploementStateByIndex(int index) {
        switch (index) {
            case 1:
                return UsersTimetable.EmploymentState.FREE;
            case 2:
                return UsersTimetable.EmploymentState.ALMOSTFREE;
            case 3:
                return UsersTimetable.EmploymentState.BUSY;
        }
        return UsersTimetable.EmploymentState.FREE;
    }
}
