package daluai.app.whatsdown.data.manager.converter;

public class UserValueStringConverter implements UserValueConverter<String> {


    @Override
    public String fromString(String string) {
        return string;
    }

    @Override
    public String toString(String value) {
        return value;
    }
}
