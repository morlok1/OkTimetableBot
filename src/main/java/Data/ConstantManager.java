package Data;

/**
 * Интерфейс, хранящий все используемые константы и данные
 */
public interface ConstantManager {

    String token = "tkn1kCxmucCXnyJlpmxVf4LdzFjRcJwIFPnZkK6ykCJh9taviLyBjJhu0tuLKutzH0ipH0:CBAQCFBMEBABABABA";

    String baseUrl = "https://api.ok.ru/";

    String serverURI = "http://sergey.medvedev.space";

    String endpointURI = "/forOkWebhooks";

    String favikonUri = "images/favicon.ico";

    String thanksUri = "html/thanks.html";

    String formUri = "html/timetableForm.html";

    String linkRequest = "Расписание";

    String resultRequest = "Результат";

    String clearRequest = "Новое";

    String[] dayOfWeek = {"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};

    int port = 10001;

}
