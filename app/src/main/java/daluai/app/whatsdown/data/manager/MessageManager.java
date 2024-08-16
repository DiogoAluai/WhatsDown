package daluai.app.whatsdown.data.manager;

import androidx.lifecycle.LiveData;

import java.util.List;

import daluai.app.whatsdown.data.model.Message;

public interface MessageManager {

    List<Message> getMessages(String user);

    LiveData<List<Message>> getMessagesLive(String user);

}
