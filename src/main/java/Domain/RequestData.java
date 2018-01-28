package Domain;

public class RequestData {

    private String message;
    private String chatId;

    public RequestData(String message, String chatId) {
        this.message = message;
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public String getChatId() {
        return chatId;
    }
}
