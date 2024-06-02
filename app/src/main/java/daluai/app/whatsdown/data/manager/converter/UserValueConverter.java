package daluai.app.whatsdown.data.manager.converter;

public interface UserValueConverter<T> {

    T fromString(String string);

    String toString(T value);

}
