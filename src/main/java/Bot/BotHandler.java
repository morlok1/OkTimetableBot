package Bot;

import Domain.UsersTimetable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static Data.ChatMessageManager.*;
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
    public void handle(HttpExchange httpExchange) throws IOException {

        if (httpExchange.getRequestURI().toString().equals("/forOkWebhooks")) {
            //Отвечаем в ОК
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


        if (FuzzySearch.ratio(message, linkRequest) > 80) {

            //Пришел запрос на создание первого расписания
            log.info("New group for " + chatId + " created.");
            server.sendMessage(newTimetableCreated + bot.generateNewGroup(chatId), chatId);

        } else if (FuzzySearch.ratio(message, resultRequest) > 80) {

            //Пришел запрос на выдачу результирующего расписания
            log.info("Result timetable for " + chatId + " generated.");
            server.sendMessage(bot.getTimetable(chatId), chatId);

        } else if (FuzzySearch.ratio(message, personalTimetableRequest) > 80) {

            if (bot.getActiveUsersGroup().containsKey(chatId)) {
                server.sendMessage(bot.getGroupByHash(bot.getActiveUsersGroup().get(chatId))
                        .getUserById(chatId)
                        .getTimetableString(), chatId);

            } else {
                server.sendMessage(timetableGetAuthorizationError, chatId);
            }

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

        } else if (findTimetableInMessage(message)) {

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

    private boolean findTimetableInMessage(String message) {
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

}
