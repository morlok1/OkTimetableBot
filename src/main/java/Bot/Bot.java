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

        groups.put(chatId, new GroupOfUser(chatId));

        return ConstantManager.serverURI + ":" + ConstantManager.port  + "/?id=" + chatId;
    }

    public GroupOfUser getGroupByChatID(String chatID) {
        return groups.get(chatID);
    }

    public String getTimetable(String chatId) {
        StringBuilder builder = new StringBuilder();

        UsersTimetable.EmploymentState[] employmentStates = groups.get(chatId).getGeneralTimetable();

        for (int i=0; i<employmentStates.length; i++) {

            builder.append(dayOfWeek[i]);

            switch (employmentStates[i]) {
                case FREE:
                    builder.append(": Свободно ");
                    break;
                case ALMOSTFREE:
                    builder.append(": Свободно, но с затруднениями ");
                    break;
                case BUSY:
                    builder.append(": Занято ");
                    break;
            }
        }

        return builder.toString();
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
}
