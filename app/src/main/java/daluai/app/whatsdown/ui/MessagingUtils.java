package daluai.app.whatsdown.ui;

import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_AES_SECRET;
import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_MESSAGING_PORT;

import org.json.JSONException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.function.Consumer;

import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.whatsdown.MessageNetworkPacket;
import daluai.lib.encryption.AESEncrypter;

public final class MessagingUtils {

    private MessagingUtils() {
    }

    private static final Logger LOG = Logger.ofClass(MessagingUtils.class);
    private static final AESEncrypter ENC = new AESEncrypter(WHATS_DOWN_AES_SECRET);

    /**
     * Create runnable loop for message listening.
     */
    public static Runnable createMessageSocketListener(Consumer<MessageNetworkPacket> messageConsumer) {
        return () -> {
            try (var serverSocket = new ServerSocket(WHATS_DOWN_MESSAGING_PORT)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    handleIncomingMessage(socket, messageConsumer);
                }
            } catch (IOException e) {
                LOG.e("Socket related error while reading message ", e);
            }
        };
    }

    /**
     * Decrypt incoming message packet and call consumer on it.
     */
    public static void handleIncomingMessage(Socket socket, Consumer<MessageNetworkPacket> messageConsumer) {
        try {
            var inputStream = socket.getInputStream();
            byte[] byteReadBuffer = new byte[1024];
            int byteCount = inputStream.read(byteReadBuffer);
            inputStream.close();

            // ~ if you don't snip away the 0 value tail, decryption will most likely fail
            byte[] responseBytes = Arrays.copyOfRange(byteReadBuffer, 0, byteCount);

            var decryptedBytes = ENC.decryptBytes(responseBytes);
            String response = new String(decryptedBytes);
            MessageNetworkPacket messagePacket = MessageNetworkPacket.fromJson(response);
            messageConsumer.accept(messagePacket);
        } catch (IOException e) {
            LOG.e("Failed to read from socket", e);
        } catch (GeneralSecurityException e) {
            LOG.e("Failed to decrypt incoming message", e);
        } catch (JSONException e) {
            LOG.e("Failed to parse incoming message into object", e);
        }
    }

    /**
     * Encrypt and send bytes to target ip.
     */
    public static void sendMessage(String targetIp, byte[] bytes) {
        try (Socket socket = new Socket(targetIp, WHATS_DOWN_MESSAGING_PORT)) {
            var encryptedBytes = ENC.encryptBytes(bytes);
            var outputStream = socket.getOutputStream();
            outputStream.write(encryptedBytes);
            outputStream.flush();
            outputStream.close();
            LOG.d("Sent " + bytes.length + " bytes to ip " + targetIp);
        } catch (IOException e) {
            LOG.e("Error when connecting to host: " + targetIp, e);
        } catch (GeneralSecurityException e) {
            LOG.e("Failed to encrypt bytes. Message not sent", e);
        }
    }
}
