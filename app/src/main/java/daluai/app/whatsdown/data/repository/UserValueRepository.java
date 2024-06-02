package daluai.app.whatsdown.data.repository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import daluai.app.whatsdown.data.dao.UserValueDao;
import daluai.app.whatsdown.data.model.UserValueRaw;
import daluai.app.whatsdown.data.utils.UiUtils;
import daluai.app.whatsdown.ui.ALog;

@Singleton
public class UserValueRepository {

    private static final ALog LOG = new ALog(UserValueRepository.class);

    private final Executor executor;

    private final UserValueDao userValueDao;

    @Inject
    public UserValueRepository(UserValueDao userValueDao) {
        this.userValueDao = userValueDao;
        executor = Executors.newSingleThreadExecutor();
    }

    public void getAllUserValue(Consumer<List<UserValueRaw>> callback) {
        LOG.i("Fetching all user values, and applying callback.");
        applyCallbackOnMainThread(userValueDao::getAll, callback);
    }

    // todo: change to custom keys object
    public <T> void getAllUserValueByIds(Consumer<List<UserValueRaw>> callback, String... keys) {
        LOG.i("Fetching user values by ids, and applying callback.");
        applyCallbackOnMainThread(() -> userValueDao.loadAllByIds(keys), callback);
    }

    private <T> void applyCallbackOnMainThread(Supplier<List<UserValueRaw>> fetcherDaoMethod,
                                               Consumer<List<UserValueRaw>> callback) {
        executor.execute(() -> {
            // dao must run on separate thread
            var userValues = fetcherDaoMethod.get();
            // then we update the UI on the main thread
            UiUtils.runCallbackOnMainThread(callback, userValues);
        });
    }

    public void insert(UserValueRaw userValueRaw) {
        LOG.i("Inserting user value: " + userValueRaw);
        executor.execute(() -> userValueDao.insert(userValueRaw));
    }

    public void insertAll(UserValueRaw... userValueRaws) {
        LOG.i("Inserting user values: " + concatExpensesString(userValueRaws));
        executor.execute(() -> userValueDao.insertAll(userValueRaws));
    }

    private static String concatExpensesString(UserValueRaw[] userValueRaws) {
        return Arrays.stream(userValueRaws)
                .map(UserValueRaw::toString)
                .collect(Collectors.joining(","));
    }

    public void update(UserValueRaw userValueRaw) {
        LOG.i("Updating user value: " + userValueRaw);
        executor.execute(() -> userValueDao.update(userValueRaw));
    }

    public void upsert(UserValueRaw userValueRaw) {
        LOG.i("Upserting user value: " + userValueRaw);
        executor.execute(() -> userValueDao.upsert(userValueRaw));
    }

    public void delete(UserValueRaw userValueRaw) {
        LOG.i("Deleting user value: " + userValueRaw);
        executor.execute(() -> userValueDao.delete(userValueRaw));
    }
}