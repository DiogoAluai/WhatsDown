package daluai.app.whatsdown.data.manager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import daluai.app.whatsdown.data.dao.MessageDao;
import daluai.app.whatsdown.data.model.Message;

public class MessageManagerImpl implements MessageManager {

    @Inject
    MessageDao messageDao;

    @Inject
    public MessageManagerImpl(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public List<Message> getMessages(String user) {
        return Optional.ofNullable(messageDao.loadMessagesOfUser(user))
                .orElse(Collections.emptyList());
    }

    @Override
    public LiveData<List<Message>> getMessagesLive(String user) {
        return Optional.ofNullable(messageDao.loadMessagesOfUserLive(user))
                .orElse(new MutableLiveData<>(Collections.emptyList()));
    }
}
