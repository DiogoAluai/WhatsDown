package daluai.app.whatsdown.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import daluai.app.whatsdown.data.dao.UserValueDao;
import daluai.app.whatsdown.data.manager.UserValue;
import daluai.app.whatsdown.data.manager.UserValueKeys;
import daluai.app.whatsdown.data.model.UserValueRaw;

@HiltViewModel
public class UsernameViewModel extends ViewModel {

    private final LiveData<UserValueRaw> usernameRawLive;

    @Inject
    public UsernameViewModel(UserValueDao userValueDao) {
        usernameRawLive = userValueDao.getUserValueLive(UserValueKeys.USERNAME.getKey());

    }

    @SuppressWarnings("unchecked")
    public LiveData<UserValue<String>> getUsernameLive() {
        return Transformations.map(usernameRawLive,
                userValueRaw -> Optional.ofNullable(userValueRaw)
                        .map(userVal -> (UserValue<String>) userVal.toCooked())
                        .orElse(null));
    }
}
