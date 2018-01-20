package Bot;

import Data.ConstantManager;
import Domain.GroupOfUser;
import Domain.UsersTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static Data.ConstantManager.*;

/**
 * Класс, осуществляющий общую работу бота:
 * -Создание\Пересоздание расписания
 * -Возвращение строки, содержащей результирующее расписание
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static Bot theInstance;
    private HashMap<String, GroupOfUser> groups;        //Хранилище расписаний
    private HashMap<String, String> activeUsersGroup;   //Хранилище соответствий групп и пользователей

    public static Bot getInstance() {
        if (theInstance == null) {
            theInstance = new Bot();
        }

        return theInstance;
    }

    private Bot() {
        groups = new HashMap<>();
        activeUsersGroup = new HashMap<>();
    }

    /**
     * Производит создание новой группы с заданным идентификатором чата
     * В случае существования - оповещение об этом и предложение пересоздать расписание
     * @param chatId - идентификатор
     * @return
     */
    public String generateNewGroup(String chatId) {
        String result = "";
        String hash = generateHash();
        log.info("New timetable for chat: " + chatId + " generated with hash: " + hash);
//        if (findAdmin(chatId) != null) {
//            log.warn("Attempt to wipe the timetable");
//            result = "Вы уже создали расписание.\\nЧтобы начать заново введите 'Новое'";
//        } else {
            groups.put(hash, new GroupOfUser(chatId));
            result = "Форма ввода расписания доступна по ссылке: \\n" + ConstantManager.serverURI + ":" + ConstantManager.port  + "/?id=" + hash;
//        }

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
        if (findAdmin(chatId) == null) {
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
        log.info("User with id: " + chatId + " and hash: " + activeUsersGroup.get(chatId));
        if (activeUsersGroup.containsKey(chatId)) {
            UsersTimetable.EmploymentState[] employmentStates = groups.get(activeUsersGroup.get(chatId)).getGeneralTimetable();

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
     * @param hash - идентификатор
     * @return
     */
    public GroupOfUser getGroupByHash(String hash) {
        return groups.get(hash);
    }


    private String generateHash(){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        for (int i=0; i<hashLength; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        return builder.toString();
    }

    private String findAdmin(String adminID) {

        Set<String> keys = groups.keySet();
        Iterator<String> iterator = keys.iterator();
        String hash;
        while (iterator.hasNext()) {
            hash = iterator.next();
            System.out.println("HASH: " + hash);
            if (groups.get(hash).getAdminID().equals(adminID)) {
                return hash;
            }
        }

        return null;
    }

    public boolean findHash(String message) {
        if (groups.containsKey(message)) {
            return true;
        }

        return false;
    }

    public String findUser(String chatId) {
        Set<String> keys = groups.keySet();
        Iterator<String> iterator = keys.iterator();
        String hash;
        while (iterator.hasNext()) {
            hash = iterator.next();
            if (groups.get(hash).contain(chatId)) {
                return hash;
            }
        }

        return null;
    }

    public HashMap<String, String> getActiveUsersGroup() {
        return activeUsersGroup;
    }
}
