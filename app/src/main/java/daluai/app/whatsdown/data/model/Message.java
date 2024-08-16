package daluai.app.whatsdown.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {

    @PrimaryKey
    public long messageId;

    @NonNull
    public String user;

    @NonNull
    public String messageText;

    public Message() {
        user = "no user";
        messageText = "no message";
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", user='" + user + '\'' +
                ", messageText='" + messageText + '\'' +
                '}';
    }
}
