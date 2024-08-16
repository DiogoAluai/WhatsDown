package daluai.app.whatsdown.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

import daluai.app.whatsdown.data.model.Message;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM messages WHERE `user` == :user")
    List<Message> loadMessagesOfUser(String user);

    @Query("SELECT * FROM messages WHERE `user` == :user")
    LiveData<List<Message>> loadMessagesOfUserLive(String user);

    @Insert
    void insert(Message message);

    @Upsert
    void upsert(Message message);

    @Insert
    void insertAll(Message... message);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);
}
