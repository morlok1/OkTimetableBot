package Bot;

import Data.ConstantManager;
import Data.PropertyManager;
import Domain.GroupOfUser;
import Domain.UsersTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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
        String hash = generateHash();
        log.info("New timetable for chat: " + chatId + " generated with hash: " + hash);

        groups.put(hash, new GroupOfUser(chatId));

        return hash;
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
                builder.append(ConstantManager.getDayOfWeekByIndex(i));

                switch (employmentStates[i]) {
                    case FREE:
                        builder.append(": Свободно \n");
                        break;
                    case ALMOSTFREE:
                        builder.append(": Опоздают \n");
                        break;
                    case BUSY:
                        builder.append(": Занято \n");
                        break;
                }
            }
            log.info(builder.toString());
            result = builder.toString();
        } else {
            log.warn("Access to the schedule that was not created");
            result = PropertyManager.getProperty("timetableIsNotExist");
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

        for (Map.Entry<String, GroupOfUser> entry : groups.entrySet()) {
            String hash = entry.getKey();
            if (groups.get(hash).getAdminID().equals(adminID)) {
                return hash;
            }
        }

        return null;
    }

    public boolean findHash(String message) {
        return groups.containsKey(message);
    }

    public String findUser(String chatId) {
        for (Map.Entry<String, GroupOfUser> entry : groups.entrySet()) {
            String hash = entry.getKey();
            if (groups.get(hash).contains(chatId)) {
                return hash;
            }
        }

        return null;
    }

    public HashMap<String, String> getActiveUsersGroup() {
        return activeUsersGroup;
    }
}
