package daluai.app.whatsdown.ui.message;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_SERVICE_IP;
import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_USER;
import static daluai.app.whatsdown.ui.MessagingUtils.sendMessage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;

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

    private final LazyView<TextView> feedbackTextView;
    private final LazyView<EditText> messageEditText;
    private final LazyView<Button> sendButton;

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
        feedbackTextView = lazyViewFactory.createView(R.id.feedback);
        messageEditText = lazyViewFactory.createView(R.id.messageEditText);
        sendButton = lazyViewFactory.createView(R.id.sendButton);
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

        sendButton.get().setOnClickListener(createSendMessageOnClick());

        messageViewModel.getMessagesLive().observe(this, messages -> {
            var index = messages.size() - 1;
            if (index > -1) {
                feedbackTextView.get().setText(messages.get(index).messageText);
            }
        });
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
