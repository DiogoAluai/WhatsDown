package daluai.app.whatsdown.ui.message;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_SERVICE_IP;
import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_USER;
import static daluai.app.whatsdown.ui.MessagingUtils.sendMessage;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import daluai.app.sdk_boost.wrapper.LazyView;
import daluai.app.sdk_boost.wrapper.LazyViewFactory;
import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.ToastHandler;
import daluai.app.sdk_boost.wrapper.UiUtils;
import daluai.app.whatsdown.MessageNetworkPacket;
import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.manager.MessageManager;
import daluai.app.whatsdown.data.manager.UserValueManager;
import daluai.app.whatsdown.data.manager.dto.UserValueKeys;
import daluai.app.whatsdown.data.model.Message;

@AndroidEntryPoint
public class MessageActivity extends AppCompatActivity {

    // one for listening, other two to receive incoming messages
    public static final int THREAD_POOL_COUNT = 5;

    private static final Logger LOG = Logger.ofClass(MessageActivity.class);

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_POOL_COUNT);

    private final LazyView<TextView> messageTargetTitle;
    private final LazyView<ListView> messageListView;
    private final LazyView<EditText> messageEditText;
    private final LazyView<ImageButton> sendButton;

    @Inject
    MessageManager messageManager;

    @Inject
    UserValueManager userValueManager;

    private ToastHandler toastHandler;
    private MessageViewModel messageViewModel;
    private String messagingTargetIp;
    private String messagingTargetUser;

    public MessageActivity() {
        super(R.layout.activity_message);
        var lazyViewFactory = new LazyViewFactory(this);
        this.messageTargetTitle = lazyViewFactory.createView(R.id.messageTargetTitle);
        this.messageListView = lazyViewFactory.createView(R.id.messageListView);
        this.messageEditText = lazyViewFactory.createView(R.id.messageEditText);
        this.sendButton = lazyViewFactory.createView(R.id.sendButton);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastHandler = new ToastHandler(this);

        messagingTargetIp = getIntent().getStringExtra(INTENT_MESSAGE_SERVICE_IP);
        messagingTargetUser = getIntent().getStringExtra(INTENT_MESSAGE_USER);

        messageViewModel = new ViewModelProvider(this,
                new MessageViewModelFactory(messageManager, messagingTargetUser))
                .get(MessageViewModel.class);

        // ~ this is fine, no need for live data since intent will always have the information
        // even after the destroy/create cycle
        messageTargetTitle.get().setText(messagingTargetUser);

        EXECUTOR.execute(this::setMessagesListViewAdapter);

        sendButton.get().setOnClickListener(createSendMessageOnClick());
    }

    /**
     * Set initial state adapter, only then add messages observer in main thread
     */
    private void setMessagesListViewAdapter() {
        List<Message> startingMessages = messageManager.getMessages(messagingTargetUser);
        messageListView.get().setAdapter(createMessageListViewAdapter(startingMessages));

        Observer<List<Message>> messagesObserver = messages -> {
            messageListView.get().setAdapter(createMessageListViewAdapter(messages));
            scrollMessageListToBottom();
        };
        UiUtils.runCallbackOnMainThread(() ->
                messageViewModel.getMessagesLive().observe(this, messagesObserver));
    }

    /**
     * Post scroll to bottom to messages list view
     */
    private void scrollMessageListToBottom() {
        var listView = messageListView.get();
        var adapter = listView.getAdapter();
        int lastElementIndex = adapter.getCount() - 1;
        if (lastElementIndex > -1) {
            listView.post(() -> listView.setSelection(lastElementIndex));
        }
    }

    @NonNull
    private ArrayAdapter<Message> createMessageListViewAdapter(List<Message> startingMessages) {
        return new MessageListViewAdapter(this, android.R.layout.simple_list_item_1, startingMessages);
    }

    @NonNull
    private View.OnClickListener createSendMessageOnClick() {
        return button -> EXECUTOR.execute(() -> {
            String messageToSend = messageEditText.get().getText().toString();
            String myUsername = userValueManager.getUserValue(UserValueKeys.USERNAME).getValue();
            var messagePacket = new MessageNetworkPacket(
                    myUsername,
                    messagingTargetUser,
                    messageToSend
            );
            try {
                // receiving messages are captured the listener defined in MainActivity
                sendMessage(messagingTargetIp, messagePacket.toJson().getBytes());
                messageManager.saveMessage(Message.fromSendingPacket(messagePacket));
                UiUtils.runCallbackOnMainThread(() -> messageEditText.get().setText(""));
                toastHandler.showToast("Message sent");
            } catch (JSONException e) {
                LOG.e("Failed to jsonfy message", e);
                toastHandler.showToast("Could not send message");
            }
        });
    }
}
