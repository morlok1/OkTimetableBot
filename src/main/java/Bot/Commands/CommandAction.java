package Bot.Commands;

import Bot.Bot;

public interface CommandAction {

    boolean matches(String message);

    String[] execute(String chatId, Bot bot);

}
