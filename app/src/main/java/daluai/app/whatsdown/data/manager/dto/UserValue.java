package daluai.app.whatsdown.data.manager.dto;

public class UserValue<T> {

    T value;

    UserValueType type;

    public UserValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "UserValue{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}
