package daluai.app.whatsdown.data.manager;

import daluai.app.whatsdown.data.manager.converter.UserValueConverter;
import daluai.app.whatsdown.data.manager.converter.UserValueIntegerConverter;
import daluai.app.whatsdown.data.manager.converter.UserValueStringConverter;
import daluai.app.whatsdown.ui.ALog;

public enum UserValueType {


    STRING("STRING", new UserValueStringConverter()),
    INTEGER("INTEGER", new UserValueIntegerConverter());

    private static final ALog LOG = new ALog(UserValueType.class);

    private final String dbValue;
    private final UserValueConverter converter;

    UserValueType(String dbValue, UserValueConverter converter) {
        this.dbValue = dbValue;
        this.converter = converter;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static UserValueType fromDbValue(String thatDbValue) {
        for (var type : UserValueType.values()) {
            if (type.getDbValue().equals(thatDbValue)) {
                return type;
            }
        }
        LOG.e("Could not parse provided value into UserValueType.");
        return null;
    }

    public <T> String convertValue(T value) {
        return converter.toString(value);
    }

    public <T> T convertString(String stringValue) {
        return (T) converter.fromString(stringValue);
    }
}
