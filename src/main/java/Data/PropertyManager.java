package Data;

import Bot.BotHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class PropertyManager {

    private static final Logger log = LoggerFactory.getLogger(PropertyManager.class);
    private static Properties properties;

    //????????? ??????
    static {
        properties = new Properties();
        try {
            properties.load(BotHandler.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            log.error("Failed to read application properties. Terminating application...");
            System.exit(1);
        }
    }


    public static String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
