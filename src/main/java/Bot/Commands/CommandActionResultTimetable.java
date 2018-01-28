package Bot.Commands;

import Bot.Bot;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Data.ConstantManager.resultRequest;

public class CommandActionResultTimetable implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(CommandActionResultTimetable.class);

    @Override
    public boolean matches(String message) {
        return FuzzySearch.ratio(message, resultRequest) > 80;
    }

    @Override
    public String execute(String chatId, Bot bot) {
        log.info("Result timetable for " + chatId + " generated.");
        return bot.getTimetable(chatId);

    }
}
