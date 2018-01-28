package Bot;

import Data.ConstantManager;
import api.OkApi;
import com.sun.net.httpserver.HttpServer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.InetSocketAddress;

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

        try {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(ConstantManager.baseUrl).build();
            okApi = retrofit.create(OkApi.class);

            log.info("OK worker started.");

        } catch (Exception e) {
            log.error("OK worker is not started.");
        }


        log.info("Server started. Listen " + ConstantManager.port + " port.");

        setSubscribes();


    }

    /**
     * Отправляет заданное текстовое сообщение в заданный чат в ОК
     * @param message - сообщение
     * @param chatId - идентификатор чата ОК
     */
    public void sendMessage(String message, String chatId) {

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                "{\"recipient\":{\"chat_id\":\"" + chatId + "\"}, \"message\":{\"text\":\"" + message + "\" } }");
        try {
            Response<ResponseBody> response = okApi.sendMessage(body, ConstantManager.token).execute();

        } catch (IOException e) {
            log.error("Error sending message.");
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

}
