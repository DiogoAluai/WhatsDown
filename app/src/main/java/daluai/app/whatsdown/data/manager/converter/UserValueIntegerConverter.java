package daluai.app.whatsdown.data.manager.converter;

public class UserValueIntegerConverter implements UserValueConverter<Integer> {

    @Override
    public Integer fromString(String string) {
        return Integer.valueOf(string);
    }

    @Override
    public String toString(Integer value) {
        return value.toString();
    }
}
