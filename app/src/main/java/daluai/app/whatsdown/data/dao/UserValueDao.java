package daluai.app.whatsdown.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

import daluai.app.whatsdown.data.model.UserValueRaw;

@Dao
public interface UserValueDao {

    @Query("SELECT * FROM user_values")
    List<UserValueRaw> getAll();

    @Query("SELECT * FROM user_values WHERE `key` IN (:keys)")
    List<UserValueRaw> loadAllByIds(String[] keys);

    @Query("SELECT * FROM user_values WHERE `key` = :key")
    UserValueRaw loadById(String key);

    @Insert
    void insert(UserValueRaw userValueRaw);

    @Upsert
    void upsert(UserValueRaw userValueRaw);

    @Insert
    void insertAll(UserValueRaw... userValueRaws);

    @Update
    void update(UserValueRaw userValueRaw);

    @Delete
    void delete(UserValueRaw userValueRaw);
}