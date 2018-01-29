package Bot;

import Data.ConstantManager;
import api.OkApi;
import com.sun.net.httpserver.HttpServer;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Random;

import static Data.ConstantManager.PROPERTIES_FILE;
import static java.lang.System.exit;


/**
 * Класс сервера, осуществляющий общение бота с сетью.
 * Работает с api ОК.
 * Отдает html-страницы ввода расписания и благодарности
 */
public class BotServer {

    private static final Logger log = LoggerFactory.getLogger(BotServer.class);
    private static BotServer theInstance;
    private final HttpServer server;
    private OkApi okApi;

    private GroupActor actorVk;
    private VkApiClient apiClient;
    private final Random random = new Random();


    public static BotServer getInstance() throws IOException {
        if (theInstance == null) {
            theInstance = new BotServer();
        }

        return theInstance;
    }

    private BotServer() throws IOException {

        server = HttpServer.create();
        server.bind(new InetSocketAddress(ConstantManager.port),10);
        server.createContext("/", new BotHandler(this));
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stop the server");
            server.stop(0);
        }));


        initOk();
        initVk();


        log.info("Server started. Listen " + ConstantManager.port + " port.");

        setSubscribes();


    }

    /**
     * Отправляет заданное текстовое сообщение в заданный чат в ОК
     * @param message - сообщение
     * @param chatId - идентификатор чата ОК
     */
    public void sendOkMessage(String message, String chatId) {

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                "{\"recipient\":{\"chat_id\":\"" + chatId + "\"}, \"message\":{\"text\":\"" + message.replaceAll("\n","\\\\n") + "\" } }");
        try {
            Response<ResponseBody> response = okApi.sendMessage(body, ConstantManager.token).execute();

        } catch (IOException e) {
            log.error("Error sending message.");
        }
    }

    /**
     * Отправляет заданное текстовое сообщение в заданный чат в ВК
     * @param message - сообщение
     * @param chatId - идентификатор чата ВК
     */
    public void sendVkMessage(String message, String chatId) {
        try {
            apiClient.messages().send(actorVk).message(message).userId(Integer.valueOf(chatId)).randomId(random.nextInt()).execute();
            log.error("VK worker was started.");
        } catch (ApiException e) {
            log.error("INVALID REQUEST", e);
            log.error("VK worker was not started.");
        } catch (ClientException e) {
            log.error("NETWORK ERROR", e);
            log.error("VK worker was not started.");
        }
    }

    /**
     * Осуществляет подписку на webhook`и от ОК
     * Предварительно проверяет её наличие
     */
    private void setSubscribes() {
        String endpoint = ConstantManager.serverURI + ConstantManager.okEndpointURI;
        if (!checkSubscribes(endpoint)) {
            String message = "";
            try {
                RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                        "{\"url\":\"" + endpoint + "\"}");
                Response<ResponseBody> response = okApi.setSubscribe(body, ConstantManager.token).execute();

                message = response.body().string();


            } catch (IOException e) {
                e.printStackTrace();

                log.error("An error occurred while trying to send a subscription request");
            }
            if (message.contains("true")) {
                log.info("Subscription is registered");
            } else {
                log.error("Subscription is not registered");
                exit(1);
            }
        } else {
            log.info("Subscription already registered");
        }
    }

    /**
     * Осуществляет проверку подписки на webhook`и от ОК по указанному endpoint`у
     * @param endpoint
     * @return -    true - есть подписка
     *              false - нет подписки
     */
    private boolean checkSubscribes(String endpoint) {
        String message = "";

        try {
            Response<ResponseBody> response = okApi.getSubscriptions(ConstantManager.token).execute();

            message = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (message.contains(endpoint)) {
            return true;
        }

        return false;
    }


    private void initOk() {
        try {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(ConstantManager.baseUrl).build();
            okApi = retrofit.create(OkApi.class);
            log.error("OK worker was started.");
        } catch (Exception e) {
            log.error("OK worker was not started.");
        }
    }

    private void initVk() {

        HttpTransportClient client = new HttpTransportClient();
        apiClient = new VkApiClient(client);

        try {
            actorVk = initVkApi(apiClient, readProperties());
        } catch (FileNotFoundException e) {
            log.error("Cannot read VK properties");
        }
    }

    private static GroupActor initVkApi(VkApiClient apiClient, Properties properties) {
        int groupId = Integer.parseInt(properties.getProperty("groupId"));
        String token = properties.getProperty("token");
        int serverId = Integer.parseInt(properties.getProperty("serverId"));
        if (groupId == 0 || token == null || serverId == 0) throw new RuntimeException("Params are not set");
        GroupActor actor = new GroupActor(groupId, token);

        try {
            apiClient.groups().setCallbackSettings(actor, serverId).messageNew(true).execute();
        } catch (ApiException e) {
            throw new RuntimeException("Api error during init", e);
        } catch (ClientException e) {
            throw new RuntimeException("Client error during init", e);
        }

        return actor;
    }

    private static Properties readProperties() throws FileNotFoundException {
        InputStream inputStream = BotServer.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (inputStream == null)
            throw new FileNotFoundException("property file '" + PROPERTIES_FILE + "' not found in the classpath");

        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Incorrect properties file");
        }
    }

    public void sendOkMessages(String[] messages, String chatId) {
        for (String message : messages) {
                sendOkMessage(message, chatId);
            }
    }

    public void sendVkMessages(String[] messages, String chatId) {
        for (String message : messages) {
            sendVkMessage(message, chatId);
        }
    }
}
