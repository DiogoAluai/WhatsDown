package daluai.app.whatsdown;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class HiltBaseApplication extends Application {

    // This Application class will hold the Dagger generated components.
    // It's connected to the rest of the application via the AndroidManifest file.

}
