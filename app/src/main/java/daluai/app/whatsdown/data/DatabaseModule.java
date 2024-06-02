package daluai.app.whatsdown.data;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import daluai.app.whatsdown.data.dao.UserValueDao;

/**
 * Database module for dao injection.
 * The component used in 'InstallIn' annotation is will determine to which classes the bindings are visible.
 * 'SingletonComponent' is used for whole Application.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * AppDatabase is the one responsible for providing singleton,
     * therefore singleton annotation is omitted.
     */
    @Provides
    public static AppDatabase provideDatabase(Application application) {
        return AppDatabase.getDatabase(application.getApplicationContext());
    }

    @Provides
    @Singleton
    public static UserValueDao provideExpenseDao(AppDatabase database) {
        return database.userValueDao();
    }
}
