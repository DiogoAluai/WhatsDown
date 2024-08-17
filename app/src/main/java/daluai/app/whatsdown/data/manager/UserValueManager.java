package daluai.app.whatsdown.data.manager;

import java.util.function.Consumer;

import javax.inject.Singleton;

import daluai.app.whatsdown.data.manager.dto.UserValue;
import daluai.app.whatsdown.data.manager.dto.UserValueKey;

/**
 * Syncing on boot might be costly.
 * Instead let's return default values and update database when null is fetched.
 */
@Singleton
public interface UserValueManager {

    /**
     * Cannot be ran on main thread
     */
    <T> UserValue<T> getUserValue(UserValueKey<T> userValueKey);

    <T> void getUserValue(UserValueKey<T> userValueKey, Consumer<UserValue<T>> callback);

    <T> void getUserValue(UserValueKey<T> userValueKey, Consumer<UserValue<T>> callback, boolean runParallel);

    <T> void updateUserValue(UserValueKey<T> userValueKey, T value);

}
