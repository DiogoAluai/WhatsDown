package daluai.app.whatsdown;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageNetworkPacket {

    private String userOrigin;

    private String userTarget;

    private String message;

    public MessageNetworkPacket(String userOrigin, String userTarget, String message) {
        this.userOrigin = userOrigin;
        this.userTarget = userTarget;
        this.message = message;
    }

    public String toJson() throws JSONException {
        return new JSONObject()
                .put("userOrigin", userOrigin)
                .put("userTarget", userTarget)
                .put("message", message)
                .toString();
    }

    public static MessageNetworkPacket fromJson(String jsonString) throws JSONException {
        var json = new JSONObject(jsonString);

        String userOrigin = (String) json.get("userOrigin");
        String userTarget = (String) json.get("userTarget");
        String message = (String) json.get("message");

        return new MessageNetworkPacket(userOrigin, userTarget, message);
    }

    public String getUserOrigin() {
        return userOrigin;
    }

    public void setUserOrigin(String userOrigin) {
        this.userOrigin = userOrigin;
    }

    public String getUserTarget() {
        return userTarget;
    }

    public void setUserTarget(String userTarget) {
        this.userTarget = userTarget;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
