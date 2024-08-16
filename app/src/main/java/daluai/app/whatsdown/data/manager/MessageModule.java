package daluai.app.whatsdown.data.manager;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@SuppressWarnings("unused")
@InstallIn(SingletonComponent.class)
public abstract class MessageModule {

    @Binds
    @Singleton
    @SuppressWarnings("unused")
    public abstract MessageManager bindMessageManager(MessageManagerImpl impl);

}