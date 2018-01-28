package Data;

/**
 * Интерфейс, хранящий все используемые константы и данные
 */
public interface ConstantManager {

    String token = "tkn1kCxmucCXnyJlpmxVf4LdzFjRcJwIFPnZkK6ykCJh9taviLyBjJhu0tuLKutzH0ipH0:CBAQCFBMEBABABABA";

    String baseUrl = "https://api.ok.ru/";

    String serverURI = "http://api.medvedev.space";

    String okEndpointURI = "/forOkWebhooks";

    String vkEndpointURI = "/forVkWebhooks";

    String linkRequest = "Расписание";

    String resultRequest = "Результат";

    String personalTimetableRequest = "Моё расписание";

    String[] shortDayOfWeek = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};

    int DAYS_IN_WEEK = 7;

    int port = 10000;

    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabvdefghijklmnopqrstuvwxyz0123456789";
    int hashLength = 10;

    static String getDayOfWeekByIndex(int index) {
        String[] dayOfWeek = {"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};
        return dayOfWeek[index];
    }

}
