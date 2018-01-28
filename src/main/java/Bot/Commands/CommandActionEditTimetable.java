package Bot.Commands;

import Bot.Bot;
import Data.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Data.ConstantManager.DAYS_IN_WEEK;
import static Data.ConstantManager.shortDayOfWeek;

public class CommandActionEditTimetable implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(CommandActionEditTimetable.class);
    private String message;

    @Override
    public boolean matches(String message) {
        message = message.toLowerCase();
        if (message.contains("пн") ||
                message.contains("вт") ||
                message.contains("ср") ||
                message.contains("чт") ||
                message.contains("пн") ||
                message.contains("сб") ||
                message.contains("вс")) {
            this.message = message;
            return true;
        }

        return false;
    }

    @Override
    public String execute(String chatId, Bot bot) {
        if (bot.getActiveUsersGroup().containsKey(chatId)) {
            if (parseTimetableFromOk(message, chatId, bot.getActiveUsersGroup().get(chatId), bot)) {
                return PropertyManager.getProperty("timetableEditCompleted");
            } else {
                return PropertyManager.getProperty("syntaxError");
            }

        } else {
            return PropertyManager.getProperty("authorizationError");
        }
    }

    private boolean parseTimetableFromOk(String message, String chatID, String hash, Bot bot) {
        String state;

        for (int i=0; i<DAYS_IN_WEEK; i++) {
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
}
