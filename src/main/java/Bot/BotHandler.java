package Bot;

import Data.ConstantManager;
import Domain.UsersTimetable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static Data.ChatMessageManager.*;
import static Data.ConstantManager.hashLength;
import static Data.ConstantManager.shortDayOfWeek;


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


    public BotHandler(BotServer server) throws IOException {
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
    public void handle(HttpExchange httpExchange) throws IOException {

        if (httpExchange.getRequestURI().toString().equals("/favicon.ico")) {
            //Отправим фавикон, что ли
            log.info("Request to send a favicon");
            sendFavicon(httpExchange);
        } else if (httpExchange.getRequestURI().toString().equals("/forOkWebhooks")) {
            //Отвечаем в ОК
            parseMessageFromOk(httpExchange);
        } else if (httpExchange.getRequestURI().toString().contains("/?id=")) {
            //Отдаем html-страницу
            sendWebForm(httpExchange);

        } else if (httpExchange.getRequestURI().toString().contains("monday")) {
            //Записываем результаты для заданной группы
            parseTimetableFromWebForm(httpExchange);
        } else {
            //Это какой-то странный запрос - пока просто игнорим и светим в лог
            log.warn("Unexpected request (probably, the Chinese)");
            log.warn("URI: " + httpExchange.getRequestURI().toString());
        }

    }

    /**
     * Отправляет фавикон (но это не точно)
     * @param httpExchange
     * @throws IOException
     */
    private void sendFavicon(HttpExchange httpExchange) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(ConstantManager.favikonUri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        byte[] bytes = builder.toString().getBytes();
        httpExchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Отправляет страницу благодарности за заполненную форму
     * @param httpExchange
     * @throws IOException
     */
    private void sendThanksPage(HttpExchange httpExchange) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(ConstantManager.thanksUri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        OutputStream os = httpExchange.getResponseBody();
        String line;
        StringBuilder builder = new StringBuilder();

        log.info("Sending a web page with form");
        while ((line = reader.readLine()) != null) {

            builder.append(line);

        }
        byte[] page = builder.toString().getBytes();
        httpExchange.sendResponseHeaders(200, page.length);
        os.write(page);
        os.close();
    }

    /**
     * Производит сборку и отправку страницы с формой
     * Сборка:
     * -вставка идентификатор чата, закрепленного за данной ссылкой
     * -вставка актуального адреса сервера обработки формы
     * @param httpExchange
     * @throws IOException
     */
    private void sendWebForm(HttpExchange httpExchange) throws IOException {

        String hash = httpExchange.getRequestURI().toString();
        hash = hash.substring(hash.indexOf("?id=") + 4);

        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(ConstantManager.formUri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        OutputStream os = httpExchange.getResponseBody();
        String line;
        StringBuilder builder = new StringBuilder();

        log.info("Sending a web page with form");
        while ((line = reader.readLine()) != null) {
            if (line.equals("</form>")) {
                builder.append("<input type=\"input\" value=\"" + hash + "\" name=\"hash\" hidden>");
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

    /**
     * Производит разбор сообщения от ОК, определяя цель сообщения:
     * -Создание расписание
     * -Затирание старого расписания и создания нового
     * -Вывод результирующего расписания
     * -Неизвестное сообщение - вывод информации о синтаксисе запросов бота
     * @param httpExchange
     * @throws IOException
     */
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

        String hash;

        if (FuzzySearch.ratio(message, ConstantManager.linkRequest) > 80) {
            //Пришел запрос на создание первого расписания
            log.info("New group for " + chatId + " created.");

            server.sendMessage(newTimetableCreated + bot.generateNewGroup(chatId), chatId);

        } else if (FuzzySearch.ratio(message, ConstantManager.resultRequest) > 80) {
            //Пришел запрос на выдачу результирующего расписания

            log.info("Result timetable for " + chatId + " generated.");

            server.sendMessage(bot.getTimetable(chatId), chatId);

        } else if (message.length() == hashLength && bot.findHash(message)) {
            //Если пользователя в этом чате ещё нет - добавляем его туда
            if (bot.getGroupByHash(message).getUserById(chatId) == null) {
                UsersTimetable.EmploymentState[] employmentStates = new UsersTimetable.EmploymentState[7];
                for (int i = 0; i < 7; i++) {
                    employmentStates[i] = UsersTimetable.EmploymentState.FREE;
                }
                bot.getGroupByHash(message).addUser(employmentStates, chatId);
                log.info("Added user " + chatId + " for group " + message);
            }
            //Обновляем активное расписание для пользователя
            bot.getActiveUsersGroup().put(chatId, message);

            server.sendMessage(authorizationComplete ,chatId);

        } else if (findTimetable(message)) {
            if (bot.getActiveUsersGroup().containsKey(chatId)) {
                if (parseTimetableFromOk(message, chatId, bot.getActiveUsersGroup().get(chatId))) {

                    server.sendMessage(timetableEditCompleted, chatId);

                } else {

                    server.sendMessage(syntaxError, chatId);

                }

            } else {

                server.sendMessage(authorizationError, chatId);

            }

        } else {
                //Отправляем информационное сообщение
                log.info("Information message to " + chatId + " sended.");

                server.sendMessage(mainInfo, chatId);
        }
    }

    private boolean parseTimetableFromOk(String message, String chatID, String hash) {
        String state;

        for (int i=0; i<7; i++) {
            if (message.contains(shortDayOfWeek[i])) {
                try {
                    state = message.substring(message.indexOf(shortDayOfWeek[i]) + 2, message.indexOf(shortDayOfWeek[i]) + 3);
                    bot.getGroupByHash(hash).getUserById(chatID).setStateByDayIndex(i, Integer.parseInt(state));
                } catch (Exception E) {
                    log.error("Cannot parse timetable message: " + message);
                    return false;
                }
            }

        }

        return true;
    }

    private boolean findTimetable(String message) {
        if (message.contains("пн") ||
                message.contains("вт") ||
                message.contains("ср") ||
                message.contains("чт") ||
                message.contains("пн") ||
                message.contains("сб") ||
                message.contains("вс")) {
            return true;
        }

        return false;
    }

    /**
     * Производит разбор GET-запроса от формы с заполненным расписанием на неделю и его добавление
     * @param httpExchange
     * @throws IOException
     */
    private void parseTimetableFromWebForm(HttpExchange httpExchange) throws IOException {
        /*sendThanksPage(httpExchange);

        String uri = httpExchange.getRequestURI().toString();
        log.info("Parse web-form answer: " + uri);
        ///?user-name-value=Fs&monday=2&tuesday=2&wednesday=2&thursday=2&friday=3&saturday=2&sunday=2&id=chat%3AC3e11972edc00

        UsersTimetable.EmploymentState[] employmentStates = new UsersTimetable.EmploymentState[7];

        employmentStates[0] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("monday") + 7,uri.indexOf("monday") + 8)));
        employmentStates[1] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("tuesday") + 8,uri.indexOf("tuesday") + 9)));
        employmentStates[2] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("wednesday") + 10,uri.indexOf("wednesday") + 11)));
        employmentStates[3] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("thursday") + 9,uri.indexOf("thursday") + 10)));
        employmentStates[4] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("friday") + 7,uri.indexOf("friday") + 8)));
        employmentStates[5] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("saturday") + 9,uri.indexOf("saturday") + 10)));
        employmentStates[6] = getEmploymentStateByIndex(Integer.parseInt(uri.substring(uri.indexOf("sunday") + 7,uri.indexOf("sunday") + 8)));

        String hash = uri.substring(uri.indexOf("&hash=") + 6, uri.indexOf("&hash=") + 6 + hashLength).replace("%3A",":");

        log.info("Parse web-form answer complete. Result: hash:" + hash + " timetable: " + Arrays.toString(employmentStates));

        bot.getGroupByHash(hash).addUser(employmentStates, ); //TODO: Добавить какой-то идентификатор в форме
        */

    }

    /**
     * Возвращает сущность EmploymentState по цифровому индексу
     * @param index
     * @return
     */
    private UsersTimetable.EmploymentState getEmploymentStateByIndex(int index) {
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
