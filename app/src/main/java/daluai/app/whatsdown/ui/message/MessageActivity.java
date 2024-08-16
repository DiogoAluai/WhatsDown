package daluai.app.whatsdown.ui.message;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_SERVICE_IP;
import static daluai.app.whatsdown.ui.message.MessagingUtils.getMessageSocketListener;
import static daluai.app.whatsdown.ui.message.MessagingUtils.sendMessage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import daluai.app.sdk_boost.wrapper.LazyView;
import daluai.app.sdk_boost.wrapper.LazyViewFactory;
import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.ToastHandler;
import daluai.app.sdk_boost.wrapper.UiUtils;
import daluai.app.whatsdown.R;

public class MessageActivity extends AppCompatActivity {

    // one for listening, other two to receive incoming messages
    public static final int THREAD_POOL_COUNT = 3;

    private static final Logger LOG = Logger.ofClass(MessageActivity.class);

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_POOL_COUNT);

    private final LazyView<TextView> feedbackTextView;
    private final LazyView<EditText> messageEditText;
    private final LazyView<Button> sendButton;

    private ToastHandler toastHandler;
    private String messagingTargetIp;

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

        var messageSocketListener = getMessageSocketListener(toastHandler, message ->
                UiUtils.runCallbackOnMainThread(() -> feedbackTextView.get().setText(message)));

        EXECUTOR.execute(messageSocketListener);

        sendButton.get().setOnClickListener(createSendMessageOnClick());
    }

    @NonNull
    private View.OnClickListener createSendMessageOnClick() {
        return button -> EXECUTOR.execute(() -> {
            String messageToSend = messageEditText.get().getText().toString();
            sendMessage(messagingTargetIp, messageToSend.getBytes());
        });
    }
}
