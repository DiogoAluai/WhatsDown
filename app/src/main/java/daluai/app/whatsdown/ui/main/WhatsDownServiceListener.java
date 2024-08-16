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

    private static final Logger LOG = Logger.ofClass(WhatsDownServiceListener.class);

    private final ArrayList<String> serviceList;
    private final DeviceAdapter deviceAdapter;

    public WhatsDownServiceListener(Context context) {
        serviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(context, serviceList);
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
            serviceList.remove(service);
            deviceAdapter.notifyDataSetChanged();
        }, serviceName);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        ServiceInfo serviceInfo = event.getInfo();
        LOG.i("Adding service to list: " + serviceInfo);
        logServiceInfo(serviceInfo);
        UiUtils.runCallbackOnMainThread(this::addServiceIfNotPresent, serviceInfo);
    }

    private void addServiceIfNotPresent(ServiceInfo serviceInfo) {
        String username = serviceInfo.getPropertyString(PROP_USER_ALIAS);
        if (!isWhatsDownService(serviceInfo) || serviceList.contains(username)) {
            return;
        }
        serviceList.add(username);
        deviceAdapter.notifyDataSetChanged();
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

    public DeviceAdapter getDeviceAdapter() {
        return deviceAdapter;
    }
}
