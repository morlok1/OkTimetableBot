package Bot;

import Data.ConstantManager;
import Data.DataManager;
import api.OkApi;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;

import static java.lang.System.exit;

public class BotClient {

    private static BotClient theInstance;

    private OkApi okApi;
    private DataManager dataManager;

    public static BotClient getInstance() {
        if (theInstance == null) {
            theInstance = new BotClient();
        }

        return theInstance;
    }

    public BotClient() {

        System.out.println("Создается");
        try {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(ConstantManager.baseUrl).build();
            okApi = retrofit.create(OkApi.class);

            dataManager = DataManager.getInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Создан");

    }

    public void sendMessage(String message, String chatId) {

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                "{\"recipient\":{\"chat_id\":\"" + chatId + "\"}, \"message\":{ \"text\":\"" + message + "\" } }");
        try {
            okApi.sendMessage(body, ConstantManager.token).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSubscribes() {

        if (!checkSubscribes(ConstantManager.endpoint)) {
            String message = new String();
            try {
                RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), "{" + ConstantManager.endpoint + "}");
                Response<ResponseBody> response = okApi.setSubscribe(body, ConstantManager.token).execute();

                message = response.body().string();


            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message.contains("true")) {
                System.out.println("Subscription is framed");
            } else {
                System.out.println("Subscription is not registered");
                exit(1);
            }
        } else {
            System.out.println("Subscription already registered");
        }
    }

    private boolean checkSubscribes(String endpoint) {
        String message = new String();

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

