package daluai.app.whatsdown.ui;

import java.util.Map;

import javax.jmdns.ServiceInfo;

public final class WhatsDownConstants {

    private WhatsDownConstants() {
    }

    public static final int WHATS_DOWN_MESSAGING_PORT = 48089;
    public static final String WHATS_DOWN_AES_SECRET = "AAAABBBBCCCCDDDD";

    public static final String PROP_WHATS_DOWN = "WhatsDown";
    public static final String WHATS_DOWN_UP = "Up";

    public static final String PROP_USER_ALIAS = "userAlias";

    public static ServiceInfo createMyWhatsDownService(String username) {
        return ServiceInfo.create(
                "_http._tcp.local.",
                username,
                8080,
                0,
                0,
                Map.of(
                        PROP_WHATS_DOWN, WHATS_DOWN_UP,
                        PROP_USER_ALIAS, username
                ));
    }
}
