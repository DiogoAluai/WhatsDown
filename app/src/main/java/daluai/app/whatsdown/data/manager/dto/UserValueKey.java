package daluai.app.whatsdown.data.manager.dto;

import daluai.app.whatsdown.data.model.UserValueRaw;

public class UserValueKey<T> {

    private final String key;
    private final T defaultValue;

    private final UserValueType type;

    private UserValueKey(String key, T defaultValue, UserValueType type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public static <Y> UserValueKey<Y> of(String key, Y defaultValue, UserValueType type) {
        return new UserValueKey<>(key, defaultValue, type);
    }

    public UserValueRaw toRaw() {
        return new UserValueRaw(key, type.convertValue(defaultValue), type.getDbValue());
    }


    public String getKey() {
        return key;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public UserValueType getType() {
        return type;
    }
}
