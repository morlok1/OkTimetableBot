import Bot.BotServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainClass {

    private static final Logger log = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) throws IOException {

        log.info("Starting...");
        BotServer server = BotServer.getInstance();

    }

}
