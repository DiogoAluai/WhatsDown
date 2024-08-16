package daluai.app.whatsdown.ui.main;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_EXTRA_USERNAME;
import static daluai.app.whatsdown.ui.WhatsDownConstants.PROP_USER_ALIAS;
import static daluai.app.whatsdown.ui.WhatsDownConstants.PROP_WHATS_DOWN;
import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_UP;
import static daluai.app.whatsdown.ui.WhatsDownConstants.createMyWhatsDownService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import dagger.hilt.android.AndroidEntryPoint;
import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.manager.UserValueKeys;
import daluai.app.whatsdown.data.manager.UserValueManager;
import daluai.app.whatsdown.ui.pickusername.PickUsernameActivity;
import daluai.lib.network_utils.LocalIpProbe;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USERNAME_CODE = 1;

    private static final Logger LOG = Logger.ofClass(MainActivity.class);
    private static final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    UserValueManager userValueManager;

    private UsernameViewModel usernameViewModel;

    private ArrayList<String> serviceList;
    private DeviceAdapter deviceAdapter;
    private JmDNS jmdns;
    private TextView usernameTitle;

    // todo: when list is empty there's a weird horizontal line on the UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.i("Creating Main Activity");
        setContentView(R.layout.activity_main);
        initializeViewModel();
        initializeComponents();

        if (usernameViewModel.getUsernameLive() == null) {
            launchUsernameActivity();
        }
        setObserverForUsernameTitle();
        registerAndStartServiceDiscovery();
    }

    private void initializeViewModel() {
        usernameViewModel = new ViewModelProvider(this).get(UsernameViewModel.class);
    }

    private void initializeComponents() {
        usernameTitle = findViewById(R.id.mainUsernameTitleLabel);
        usernameTitle.setOnClickListener(view -> launchUsernameActivity());

        ListView listView = findViewById(R.id.device_list_view);
        serviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, serviceList);
        listView.setAdapter(deviceAdapter);
    }

    private void setObserverForUsernameTitle() {
        usernameViewModel.getUsernameLive().observe(this, username -> {
            String usernameString = username.getValue();
            if (usernameString == null) {
                usernameTitle.setText("Set username");
                return;
            }
            usernameTitle.setText(usernameString);
                executor.execute(() -> {
                    jmdns.unregisterAllServices();
                    tryRegisterMyWhatsDownService(usernameString);
                });
        });
    }

    private void launchUsernameActivity() {
        LOG.i("Launching username picker activity.");
        Intent intent = new Intent(this, PickUsernameActivity.class);
        startActivityForResult(intent, REQUEST_USERNAME_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_USERNAME_CODE) {
            LOG.e("Received unrecognizable activity request code: " + requestCode);
            Toast.makeText(getApplicationContext(), "Something went wrong, please restart the app.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (resultCode != RESULT_OK || data == null) {
            LOG.e("Username picking activity went wrong. Result code: " + resultCode);
            Toast.makeText(getApplicationContext(), "Something went wrong, please restart the app.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        String username = data.getStringExtra(INTENT_EXTRA_USERNAME);
        LOG.i("Received username from activity: " + username + ". Updating database.");
        userValueManager.updateUserValue(UserValueKeys.USERNAME, username);
    }

    private void registerAndStartServiceDiscovery() {
        executor.execute(() -> {
            try {
                jmdns = JmDNS.create(LocalIpProbe.firstActiveIPv4Interface().getInetAddress());
            } catch (IOException e) {
                LOG.e("Could not create jmdns", e);
                Toast.makeText(getApplicationContext(), "Failed to initialize device listener", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            jmdns.addServiceListener("_http._tcp.local.", getWhatsDownServiceListener());
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
            LOG.e("Error registering my service", e);
        }
    }

    @NonNull
    private ServiceListener getWhatsDownServiceListener() {
        return new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                // Only add in resolved method, as we need to verify the properties. There
                //are only available after resolution.
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                ServiceInfo serviceInfo = event.getInfo();
                String serviceName = serviceInfo.getName();
                LOG.i("Removing service from list " + serviceInfo);
                runOnUiThread(() -> {
                    serviceList.remove(serviceName);
                    deviceAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                ServiceInfo serviceInfo = event.getInfo();
                LOG.i("Adding service to list: " + serviceInfo);
                logServiceInfo(serviceInfo);
                runOnUiThread(() -> addServiceIfNotPresent(serviceInfo));
            }

            private void addServiceIfNotPresent(ServiceInfo serviceInfo) {
                String username = serviceInfo.getPropertyString(PROP_USER_ALIAS);
                if (!isWhatsDownService(serviceInfo) || serviceList.contains(username)) {
                    return;
                }
                serviceList.add(username);
                deviceAdapter.notifyDataSetChanged();
            }
        };
    }

    private String getUniqueUsername(String username) {
        if (!serviceList.contains(username)) {
            return username;
        }

        String newUsername;
        int counter = 1;
        do {
            newUsername = username + "(" + counter + ")";
        } while (serviceList.contains(newUsername));
        return newUsername;
    }

    private void logServiceInfo(ServiceInfo serviceInfo) {
        LOG.i(" - Service type: " + serviceInfo.getType());
        LOG.i(" - Service subtype: " + serviceInfo.getSubtype());
        LOG.i(" - WhatsDown property: " + serviceInfo.getPropertyString(PROP_WHATS_DOWN));
    }

    private boolean isWhatsDownService(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return false;
        }
        String whatsDownValue = serviceInfo.getPropertyString(PROP_WHATS_DOWN);
        return whatsDownValue != null && whatsDownValue.equals(WHATS_DOWN_UP);
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