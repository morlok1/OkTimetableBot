package Bot.Commands;

import Bot.Bot;
import Data.PropertyManager;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Data.ConstantManager.personalTimetableRequest;

public class CommandActionGetPersonalTimetable implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(CommandActionGetPersonalTimetable.class);

    @Override
    public boolean matches(String message) {
        return FuzzySearch.ratio(message, personalTimetableRequest) > 80;
    }

    @Override
    public String[] execute(String chatId, Bot bot) {

        log.info("Send personal timetable to: " + chatId);

        if (bot.getActiveUsersGroup().containsKey(chatId)) {
            return new String[] {bot.getGroupByHash(bot.getActiveUsersGroup().get(chatId))
                    .getUserById(chatId)
                    .getTimetableString()};

        } else {
            return new String[] {PropertyManager.getProperty("timetableGetAuthorizationError")};
        }
    }
}
