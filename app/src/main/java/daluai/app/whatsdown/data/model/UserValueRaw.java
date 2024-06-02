package daluai.app.whatsdown.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import daluai.app.whatsdown.data.manager.UserValue;
import daluai.app.whatsdown.data.manager.UserValueType;

@Entity(tableName = "user_values")
public class UserValueRaw {

    @PrimaryKey
    @NonNull
    public String key;

    @NonNull
    public String value;

    @NonNull
    public String type;

    public UserValueRaw() {
        key = "stub";
        value = "stub";
        type = "stub";
    }

    @Ignore
    public UserValueRaw(@NonNull String key, @NonNull String value, @NonNull String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public UserValue toCooked() {
        var userValueType = UserValueType.fromDbValue(type);
        return new UserValue(userValueType.convertString(value));
    }

    @NonNull
    @Override
    public String toString() {
        return "UserValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}