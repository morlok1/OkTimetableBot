package Bot;

import Data.ConstantManager;
import Domain.GroupOfUser;
import Domain.UsersTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;

import static Data.ConstantManager.*;

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

    public String clearAndGenerateGroup(String chatId) {
        groups.remove(chatId);

        return generateNewGroup(chatId);
    }

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


    public String generateHash() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        for (int i=0; i< hashLength; i++) {
            builder.append(
                    alphabet[
                            random.nextInt(alphabet.length - 1)]);
        }

        return builder.toString();
    }

    public GroupOfUser getGroupByChatID(String chatID) {
        log.info("Getter of chat " + chatID);
        return groups.get(chatID);
    }

}
