package Bot.Commands;

import Bot.Bot;
import Data.PropertyManager;
import Domain.UsersTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static Data.ConstantManager.hashLength;
import static Domain.UsersTimetable.EmploymentState.FREE;

public class CommandActionAuthorizationByHash implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(CommandActionAuthorizationByHash.class);
    private Bot bot = Bot.getInstance();
    private String hash;

    @Override
    public boolean matches(String message) {
        if (message.length() == hashLength && bot.findHash(message)) {
            hash = message;
            return true;
        }

        return false;
    }

    @Override
    public String execute(String chatId, Bot bot) {
        //Если пользователя в этом чате ещё нет - добавляем его туда
        if (bot.getGroupByHash(hash).getUserById(chatId) == null) {
            UsersTimetable.EmploymentState[] employmentStates = new UsersTimetable.EmploymentState[7];
            Arrays.fill(employmentStates, FREE);

            bot.getGroupByHash(hash).addUser(employmentStates, chatId);
            log.info("Added user " + chatId + " for group " + hash);
        }
        //Обновляем активное расписание для пользователя
        bot.getActiveUsersGroup().put(chatId, hash);
        return PropertyManager.getProperty("authorizationComplete");
    }
}
