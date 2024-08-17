package daluai.app.whatsdown.ui;

import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_MESSAGING_PORT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.ToastHandler;
import daluai.app.whatsdown.MessageNetworkPacket;

public final class MessagingUtils {

    private MessagingUtils() {
    }

    private static final Logger LOG = Logger.ofClass(MessagingUtils.class);

    public static Runnable createMessageSocketListener(ToastHandler toastHandler,
                                                       Consumer<MessageNetworkPacket> messageConsumer) {
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

    public static void handleIncomingMessage(Socket socket, Consumer<MessageNetworkPacket> messageConsumer) throws IOException {
        var inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        inputStream.close();

        String response = new String(buffer, 0, bytesRead);
        try {
            MessageNetworkPacket messagePacket = MessageNetworkPacket.fromJson(response);
            messageConsumer.accept(messagePacket);
        } catch (Exception e) {
            LOG.e("Failed to parse message packet, skipping consumer call", e);
        }
    }

    public static void sendMessage(String targetIp, byte[] bytesToSend) {
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
