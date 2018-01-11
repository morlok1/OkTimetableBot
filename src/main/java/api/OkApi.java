package api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface OkApi {

    @GET("/graph/me/messages")
    Call<ResponseBody> getMessages(@Query("access_token") String token, @Query("chat_id") String chatId);

    @POST("/graph/me/messages")
    Call<ResponseBody> sendMessage(@Body RequestBody body, @Query("access_token") String token);

    @GET("/graph/me/subscriptions")
    Call<ResponseBody> getSubscriptions(@Query("access_token") String token);

    @POST("/graph/me/subscribe")
    Call<ResponseBody> setSubscribe(@Body RequestBody body, @Query("access_token") String token);
}
