package Backend.domain;

public class Notification {
    private String title;
    private String message;
    private String type;
    private String timestamp;

    public Notification() {
    }

    public Notification(String title, String message, String type, String timestamp) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }


    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTimestamp() { return timestamp; }
}