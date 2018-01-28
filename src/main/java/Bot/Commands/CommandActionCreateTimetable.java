package Bot.Commands;

import Bot.Bot;
import Data.PropertyManager;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Data.ConstantManager.linkRequest;

public class CommandActionCreateTimetable implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(CommandActionCreateTimetable.class);

    @Override
    public boolean matches(String message) {
        return FuzzySearch.ratio(message, linkRequest) > 80;
    }

    @Override
    public String execute(String chatId, Bot bot) {
        log.info("New group for " + chatId + " created.");
        return PropertyManager.getProperty("newTimetableCreated") + bot.generateNewGroup(chatId);
    }
}
