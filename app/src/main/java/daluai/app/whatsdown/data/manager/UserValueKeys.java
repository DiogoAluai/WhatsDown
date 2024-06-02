package daluai.app.whatsdown.data.manager;

public class UserValueKeys {

    public static final UserValueKey<String> USERNAME =
            UserValueKey.of("username", "Missing username", UserValueType.STRING);

    public static final UserValueKey<Integer> USER_ID =
            UserValueKey.of("userId", -1, UserValueType.INTEGER);

}
