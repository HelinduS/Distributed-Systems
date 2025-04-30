import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private final String sender;      // The sender of the message
    private final String recipient;   // The recipient of the message
    private final String content;     // The content of the message
    private final Date timestamp;     // The timestamp when the message was created

    // Constructor to initialize a message
    public Message(String sender, String recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = new Date(); // Set the current timestamp
    }

    // Getter method for sender
    public String getSender() {
        return sender;
    }

    // Getter method for recipient
    public String getRecipient() {
        return recipient;
    }

    // Getter method for content
    public String getContent() {
        return content;
    }

    // Getter method for timestamp
    public Date getTimestamp() {
        return timestamp;
    }

    // Overriding toString() to represent the message in a human-readable format
    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender + " to " + recipient + ": " + content;
    }
}