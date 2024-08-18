package daluai.app.whatsdown.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

import daluai.app.whatsdown.MessageNetworkPacket;

@Entity(tableName = "messages")
public class Message {

    @PrimaryKey(autoGenerate = true)
    public long messageId;

    @NonNull
    public String user;

    @NonNull
    public String messageText;

    public boolean isMine;

    public Message() {
        user = "no user";
        messageText = "no message";
        isMine = false;
    }

    @Ignore
    public Message(@NonNull String user, @NonNull String messageText, boolean isMine) {
        this.user = user;
        this.messageText = messageText;
        this.isMine = isMine;
    }

    public static Message fromSendingPacket(MessageNetworkPacket message) {
        return new Message(
                message.getUserTarget(),
                message.getMessage(),
                true
        );
    }

    public static Message fromReceivingPacket(MessageNetworkPacket message) {
        return new Message(
                message.getUserOrigin(),
                message.getMessage(),
                false
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageId == message.messageId && isMine == message.isMine
                && Objects.equals(user, message.user)
                && Objects.equals(messageText, message.messageText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, user, messageText, isMine);
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", user='" + user + '\'' +
                ", messageText='" + messageText + '\'' +
                ", isMine='" + isMine + '\'' +
                '}';
    }
}
