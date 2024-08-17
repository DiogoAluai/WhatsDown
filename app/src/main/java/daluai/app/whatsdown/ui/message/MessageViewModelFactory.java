package daluai.app.whatsdown.ui.message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import daluai.app.whatsdown.data.manager.MessageManager;

public class MessageViewModelFactory implements ViewModelProvider.Factory {

    private final MessageManager messageManager;
    private final String user;

    public MessageViewModelFactory(MessageManager messageManager, String user) {
        this.messageManager = messageManager;
        this.user = user;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MessageViewModel.class)) {
            return (T) new MessageViewModel(messageManager, user);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");    }
}
