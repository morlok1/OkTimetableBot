package Bot.Commands;

import java.util.ArrayList;

public class Strategy {

    private static ArrayList<CommandAction> commands = new ArrayList<>();

    static {
        commands.add(new CommandActionCreateTimetable());
        commands.add(new CommandActionResultTimetable());
        commands.add(new CommandActionGetPersonalTimetable());
        commands.add(new CommandActionAuthorizationByHash());
        commands.add(new CommandActionEditTimetable());
        commands.add(new errorHelp());
    }

    public static CommandAction getCommandByIndex(int index) {
        return commands.get(index);
    }

    public static int countOfCommand() {
        return commands.size();
    }


}
