package daluai.app.whatsdown.data.manager;

public class UserValueKeys {

    // default username value is shared with UsernamePickerActivity hint for the EditText
    public static final UserValueKey<String> USERNAME =
            UserValueKey.of("username", "Usernameless", UserValueType.STRING);

    public static final UserValueKey<Integer> USER_ID =
            UserValueKey.of("userId", -1, UserValueType.INTEGER);

}
