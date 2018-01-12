package Bot;

import Data.ConstantManager;
import Domain.GroupOfUser;
import Domain.UsersTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;

import static Data.ConstantManager.*;

/**
 * Класс, осуществляющий общую работу бота:
 * -Создание\Пересоздание расписания
 * -Возвращение строки, содержащей результирующее расписание
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static Bot theInstance;
    private HashMap<String, GroupOfUser> groups;


    public static Bot getInstance() {
        if (theInstance == null) {
            theInstance = new Bot();
        }

        return theInstance;
    }

    private Bot() {
        groups = new HashMap<>();
    }

    /**
     * Производит создание новой группы с заданным идентификатором чата
     * В случае существования - оповещение об этом и предложение пересоздать расписание
     * @param chatId - идентификатор
     * @return
     */
    public String generateNewGroup(String chatId) {
        String result = "";
        if (groups.containsKey(chatId)) {
            log.warn("Attempt to wipe the timetable");
            result = "Вы уже создали расписание.\\nЧтобы начать заново введите 'Новое'";
        } else {
            groups.put(chatId, new GroupOfUser(chatId));
            result = "Форма ввода расписания доступна по ссылке: \\n" + ConstantManager.serverURI + ":" + ConstantManager.port  + "/?id=" + chatId;
        }

        return result;
    }

    /**
     * Удаляет расписание по переданному идентификатору и создает новое
     * В случае, если расписания не существует - передаёт оповещение
     * @param chatId - идентификатор
     * @return
     */
    public String clearAndGenerateGroup(String chatId) {
        String result;
        if (groups.containsKey(chatId)) {
            log.warn("Attempt to wipe the timetable");
            result = "Вы ещё не создали расписания.\\\\nЧтобы создать расписание отправьте 'Расписание'\"";
        } else {
            groups.remove(chatId);
            result = generateNewGroup(chatId);
        }

        return result;
    }

    /**
     * Генерирует сообщение, содержащее результирующее расписание
     * по переданному идентификатору сообщения
     * @param chatId - идентификатор
     * @return
     */
    public String getTimetable(String chatId) {
        log.info("Start generate timetable");
        String result = "";
        StringBuilder builder = new StringBuilder();

        if (groups.containsKey(chatId)) {
            UsersTimetable.EmploymentState[] employmentStates = groups.get(chatId).getGeneralTimetable();

            for (int i = 0; i < employmentStates.length; i++) {
                builder.append(dayOfWeek[i]);

                switch (employmentStates[i]) {
                    case FREE:
                        builder.append(": Свободно \\n");
                        break;
                    case ALMOSTFREE:
                        builder.append(": Свободно, но с затруднениями \\n");
                        break;
                    case BUSY:
                        builder.append(": Занято \\n");
                        break;
                }
            }
            log.info(builder.toString());
            result = builder.toString();
        } else {
            log.warn("Access to the schedule that was not created");
            result = "Вы ещё не создали расписания.\\nЧтобы создать расписание отправьте 'Расписание'";
        }
        return result;
    }

    /**
     *  Возвращает расписание по переданному идентификатору
     * @param chatID - идентификатор
     * @return
     */
    public GroupOfUser getGroupByChatID(String chatID) {
        return groups.get(chatID);
    }

}
