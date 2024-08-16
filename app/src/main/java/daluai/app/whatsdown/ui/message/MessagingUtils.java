package daluai.app.whatsdown.ui.message;

import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_MESSAGING_PORT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.ToastHandler;

public final class MessagingUtils {

    private MessagingUtils() {
    }

    private static final Logger LOG = Logger.ofClass(MessagingUtils.class);

    static Runnable getMessageSocketListener(ToastHandler toastHandler,
                                             Consumer<String> messageConsumer) {
        return () -> {
            try (var serverSocket = new ServerSocket(WHATS_DOWN_MESSAGING_PORT)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    handleIncomingMessage(socket, messageConsumer);
                }
            } catch (IOException e) {
                LOG.e("Error while reading message ", e);
                toastHandler.showToast("Error receiving message");
            }
        };
    }

    static void handleIncomingMessage(Socket socket, Consumer<String> messageConsumer) throws IOException {
        var inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        inputStream.close();

        String response = new String(buffer, 0, bytesRead);
        messageConsumer.accept(response);
    }

    static void sendMessage(String targetIp, byte[] bytesToSend) {
        try (Socket socket = new Socket(targetIp, WHATS_DOWN_MESSAGING_PORT)) {
            var outputStream = socket.getOutputStream();
            outputStream.write(bytesToSend);
            outputStream.flush();
            outputStream.close();
            LOG.d("Sent " + bytesToSend.length + " bytes to ip " + targetIp);
        } catch (IOException e) {
            LOG.e("Error when connecting to host: " + targetIp, e);
        }
    }
}
