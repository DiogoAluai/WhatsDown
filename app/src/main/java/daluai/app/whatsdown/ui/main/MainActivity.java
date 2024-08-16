package daluai.app.whatsdown.ui.main;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_EXTRA_USERNAME;
import static daluai.app.whatsdown.ui.WhatsDownConstants.createMyWhatsDownService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.jmdns.JmDNS;

import dagger.hilt.android.AndroidEntryPoint;
import daluai.app.sdk_boost.wrapper.LazyView;
import daluai.app.sdk_boost.wrapper.LazyViewFactory;
import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.ToastHandler;
import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.manager.MessageManager;
import daluai.app.whatsdown.data.manager.UserValueManager;
import daluai.app.whatsdown.data.manager.dto.UserValueKeys;
import daluai.app.whatsdown.ui.pickusername.PickUsernameActivity;
import daluai.lib.network_utils.LocalIpProbe;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USERNAME_CODE = 1;

    private static final Logger LOG = Logger.ofClass(MainActivity.class);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final LazyView<TextView> usernameTitle;
    private final LazyView<ListView> listView;

    @Inject
    UserValueManager userValueManager;

    @Inject
    MessageManager messageManager;

    private ToastHandler toastHandler;
    private UsernameViewModel usernameViewModel;

    private JmDNS jmdns;
    private WhatsDownServiceListener whatsDownServiceListener;

    // todo: when list is empty there's a weird horizontal line on the UI

    public MainActivity() {
        super(R.layout.activity_main);
        var lazyViewFactory = new LazyViewFactory(this);
        usernameTitle = lazyViewFactory.createView(R.id.mainUsernameTitleLabel);
        listView = lazyViewFactory.createView(R.id.device_list_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastHandler = new ToastHandler(this);
        whatsDownServiceListener = new WhatsDownServiceListener(this);
        usernameViewModel = new ViewModelProvider(this).get(UsernameViewModel.class);

        initializeComponents();

        userValueManager.getUserValue(UserValueKeys.USERNAME, userValue -> {
            if (userValue == null || userValue.getValue().equals(UserValueKeys.USERNAME.getDefaultValue())) {
                // no username found, let's have the user pick one
                launchUsernameActivity();
            }
        });

        setObserverForUsernameTitle();
        registerAndStartServiceDiscovery();

    }

    private void initializeComponents() {
        usernameTitle.get().setOnClickListener(view -> launchUsernameActivity());
        listView.get().setAdapter(whatsDownServiceListener.getDeviceAdapter());
   }

    private void launchUsernameActivity() {
        LOG.i("Launching username picker activity.");
        Intent intent = new Intent(this, PickUsernameActivity.class);
        startActivityForResult(intent, REQUEST_USERNAME_CODE);
    }

    private void setObserverForUsernameTitle() {
        usernameViewModel.getUsernameLive().observe(this, username -> {
            String usernameString = username.getValue();
            if (usernameString == null) {
                usernameTitle.get().setText("Set username");
                return;
            }
            usernameTitle.get().setText(usernameString);
                EXECUTOR.execute(() -> {
                    jmdns.unregisterAllServices();
                    tryRegisterMyWhatsDownService(usernameString);
                });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_USERNAME_CODE) {
            LOG.e("Received unrecognizable activity request code: " + requestCode);
            toastHandler.showToast("Something went wrong, please restart the app.");
            return;
        }

        if (resultCode != RESULT_OK || data == null) {
            LOG.e("Username picking activity went wrong. Result code: " + resultCode);
            toastHandler.showToast("Something went wrong, please restart the app.");
            return;
        }

        String username = data.getStringExtra(INTENT_EXTRA_USERNAME);
        LOG.i("Received username from activity: " + username + ". Updating database.");
        userValueManager.updateUserValue(UserValueKeys.USERNAME, username);
    }

    private void registerAndStartServiceDiscovery() {
        EXECUTOR.execute(() -> {
            try {
                jmdns = JmDNS.create(LocalIpProbe.firstActiveIPv4Interface().getInetAddress());
            } catch (IOException e) {
                LOG.e("Could not create jmdns", e);
                toastHandler.showToast("Failed to initialize device listener");
                return;
            }
            jmdns.addServiceListener("_http._tcp.local.", whatsDownServiceListener);
            registerOurWhatsDownService();
        });
    }

    private void registerOurWhatsDownService() {
        LOG.i("Registering to mDns service");
        // already in a parallel thread, db queries are fine
        userValueManager.getUserValue(UserValueKeys.USERNAME, usernameValue ->
                tryRegisterMyWhatsDownService(usernameValue.getValue()),
                false);
    }

    private void tryRegisterMyWhatsDownService(String username) {
        try {
            if (username == null) {
                LOG.w("Skipping jmdsn registering, as provided username is null");
                return;
            }
            jmdns.registerService(createMyWhatsDownService(username));
        } catch (IOException e) {
            toastHandler.showToast("Failed to register to mDNS");
            LOG.e("Error registering my service", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG.i("Unregistering mDns services");
        if (jmdns != null) {
            try {
                LOG.i("Closing jmdns");
                jmdns.unregisterAllServices();
                jmdns.close();
            } catch (IOException e) {
                LOG.e("Error closing jmdns", e);
            }
        }
    }
}