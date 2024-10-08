package daluai.app.whatsdown.data.manager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;

import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.UiUtils;
import daluai.app.whatsdown.data.dao.UserValueDao;
import daluai.app.whatsdown.data.manager.dto.UserValue;
import daluai.app.whatsdown.data.manager.dto.UserValueKey;
import daluai.app.whatsdown.data.model.UserValueRaw;

public class UserValueManagerImpl implements UserValueManager {

    private static final Logger LOG = Logger.ofClass(UserValueManagerImpl.class);

    @Inject
    UserValueDao userValueDao;

    private final Executor executor;

    @Inject
    public UserValueManagerImpl() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public <T> UserValue<T> getUserValue(UserValueKey<T> userValueKey) {
        var userValueRaw = userValueDao.loadById(userValueKey.getKey());
        if (userValueRaw == null) {
            userValueRaw = userValueKey.toRaw();
            LOG.i("Did not find populated user value. Upserting and returning default.");
            userValueDao.upsert(userValueRaw);
        } else {
            LOG.i("Found user value: " + userValueRaw);
        }
        return userValueRaw.toCooked();
    }

    @Override
    public <T> void getUserValue(UserValueKey<T> userValueKey,
                                 Consumer<UserValue<T>> callback) {
        getUserValue(userValueKey, callback, true);
    }

    @Override
    public <T> void getUserValue(UserValueKey<T> userValueKey,
                                 Consumer<UserValue<T>> callback,
                                 boolean runParallel) {
        if (runParallel) {
            executor.execute(() -> getUserValueOnMainThread(userValueKey, callback));
        } else {
            getUserValueOnMainThread(userValueKey, callback);
        }
    }

    private <T> void getUserValueOnMainThread(UserValueKey<T> userValueKey,
                                              Consumer<UserValue<T>> callback) {
        var userValueRaw = userValueDao.loadById(userValueKey.getKey());
        if (userValueRaw == null) {
            userValueRaw = userValueKey.toRaw();
            LOG.i("Did not find populated user value. Upserting and returning default.");
            userValueDao.upsert(userValueRaw);
        } else {
            LOG.i("Found user value: " + userValueRaw);
        }
        UserValue<T> userValueCooked = userValueRaw.toCooked();
        // Note: this does not look right, but I'm afraid to change it
        UiUtils.runCallbackOnMainThread(callback, userValueCooked);
    }

    @Override
    public <T> void updateUserValue(UserValueKey<T> userValueKey, T value) {
        executor.execute(() -> {
            var type = userValueKey.getType();
            var userValueRaw = new UserValueRaw(
                    userValueKey.getKey(),
                    type.convertValue(value),
                    type.getDbValue()
            );
            userValueDao.upsert(userValueRaw);
        });
    }
}
