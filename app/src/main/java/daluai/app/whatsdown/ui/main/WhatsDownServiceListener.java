package daluai.app.whatsdown.ui.main;

import static daluai.app.whatsdown.ui.WhatsDownConstants.PROP_USER_ALIAS;
import static daluai.app.whatsdown.ui.WhatsDownConstants.PROP_WHATS_DOWN;
import static daluai.app.whatsdown.ui.WhatsDownConstants.WHATS_DOWN_UP;

import android.content.Context;

import java.util.ArrayList;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.sdk_boost.wrapper.UiUtils;

public class WhatsDownServiceListener implements ServiceListener {

    // Debugging option to show self in the devices list. You can send message to yourself!
    private static final boolean SHOW_SELF = false;

    private static final Logger LOG = Logger.ofClass(WhatsDownServiceListener.class);

    private final ArrayList<ServiceInfo> serviceList;
    private final DeviceDiscoveryAdapter deviceDiscoveryAdapter;

    private String myUsername;

    public WhatsDownServiceListener(Context context, String myUsername) {
        this.serviceList = new ArrayList<>();
        this.deviceDiscoveryAdapter = new DeviceDiscoveryAdapter(context, serviceList);
        this.myUsername = myUsername;
    }

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
        UiUtils.runCallbackOnMainThread(service -> {
            serviceList.removeIf(s -> s.getName().equals(service));
            deviceDiscoveryAdapter.notifyDataSetChanged();
        }, serviceName);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        ServiceInfo serviceInfo = event.getInfo();
        LOG.i("Adding service to list: " + serviceInfo);
        logServiceInfo(serviceInfo);
        UiUtils.runCallbackOnMainThread(this::addServiceIfNotPresent, serviceInfo);
    }

    private void logServiceInfo(ServiceInfo serviceInfo) {
        LOG.i(" - Service type: " + serviceInfo.getType());
        LOG.i(" - Service subtype: " + serviceInfo.getSubtype());
        LOG.i(" - WhatsDown property: " + serviceInfo.getPropertyString(PROP_WHATS_DOWN));
    }

    private void addServiceIfNotPresent(ServiceInfo serviceInfo) {
        if (isServiceAdded(serviceInfo) || !isWhatsDownService(serviceInfo)) {
            LOG.i("Skipping service registration for " + serviceInfo);
            return;
        }
        if (isMyService(serviceInfo) && !SHOW_SELF) {
            LOG.i("Skipping service registration for my own service: " + serviceInfo);
            return;
        }
        LOG.i("Adding service " + serviceInfo);
        serviceList.add(serviceInfo);
        deviceDiscoveryAdapter.notifyDataSetChanged();
    }

    private boolean isMyService(ServiceInfo serviceInfo) {
        String username = serviceInfo.getPropertyString(PROP_USER_ALIAS);
        return username != null && username.equals(myUsername);
    }

    private boolean isServiceAdded(ServiceInfo serviceInfo) {
        return serviceList.stream()
                .map(info -> info.getPropertyString(PROP_USER_ALIAS))
                .anyMatch(registeredServiceName ->
                        registeredServiceName.equals(serviceInfo.getPropertyString(PROP_USER_ALIAS)));
    }

    private boolean isWhatsDownService(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return false;
        }
        String whatsDownValue = serviceInfo.getPropertyString(PROP_WHATS_DOWN);
        return whatsDownValue != null && whatsDownValue.equals(WHATS_DOWN_UP);
    }

    public DeviceDiscoveryAdapter getDeviceAdapter() {
        return deviceDiscoveryAdapter;
    }

    public void setMyUsername(String username) {
        this.myUsername = username;
    }
}
