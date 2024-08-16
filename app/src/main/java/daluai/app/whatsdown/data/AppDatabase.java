package daluai.app.whatsdown.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.dao.MessageDao;
import daluai.app.whatsdown.data.dao.UserValueDao;
import daluai.app.whatsdown.data.model.Message;
import daluai.app.whatsdown.data.model.UserValueRaw;

@Database(entities = {UserValueRaw.class, Message.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    /**
     * Fetch database singleton.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, context.getString(R.string.database_name))
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserValueDao userValueDao();
    public abstract MessageDao messageDao();
}
