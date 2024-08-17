package daluai.app.whatsdown.ui.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import daluai.app.whatsdown.data.manager.MessageManager;
import daluai.app.whatsdown.data.model.Message;

public class MessageViewModel extends ViewModel {

    private final LiveData<List<Message>> messages;

    public MessageViewModel(MessageManager messageManager, String user) {
        this.messages = messageManager.getMessagesLive(user);
    }

    public LiveData<List<Message>> getMessagesLive() {
        return messages;
    }
}
