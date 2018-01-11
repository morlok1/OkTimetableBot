import Bot.BotServer;

import java.io.IOException;

public class MainClass {


    public static void main(String[] args) throws IOException {

        try {
            BotServer server = BotServer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
