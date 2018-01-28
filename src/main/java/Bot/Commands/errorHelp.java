package Bot.Commands;

import Bot.Bot;
import Data.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class errorHelp implements CommandAction {

    private static final Logger log = LoggerFactory.getLogger(errorHelp.class);

    @Override
    public boolean matches(String message) {
        return true;
    }

    @Override
    public String execute(String chatId, Bot bot) {
        //Отправляем информационное сообщение
        log.info("Information message to " + chatId + " sended.");

        return PropertyManager.getProperty("mainInfo");
    }
}
