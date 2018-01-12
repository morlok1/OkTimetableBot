package Data;

public interface ConstantManager {

    String token = "tkn1kCxmucCXnyJlpmxVf4LdzFjRcJwIFPnZkK6ykCJh9taviLyBjJhu0tuLKutzH0ipH0:CBAQCFBMEBABABABA";

    String baseUrl = "https://api.ok.ru/";

    String okHost = "api.ok.ru";

    String serverURI = "http://sergey.medvedev.space";

    String endpointURI = "/forOkWebhooks";

    String linkRequest = "Расписание";

    String resultRequest = "Результат";

    String[] dayOfWeek = {"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};

    char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    int port = 10000;

    int hashLength = 7;
}
