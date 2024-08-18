package daluai.app.whatsdown.ui.main;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_SERVICE_IP;
import static daluai.app.whatsdown.ui.ActivityApi.INTENT_MESSAGE_USER;
import static daluai.app.whatsdown.ui.WhatsDownConstants.PROP_USER_ALIAS;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import javax.jmdns.ServiceInfo;

import daluai.app.sdk_boost.wrapper.ToastHandler;
import daluai.app.whatsdown.ui.message.MessageActivity;

public class DeviceDiscoveryAdapter extends ArrayAdapter<ServiceInfo> {

    private final ToastHandler toastHandler;
    private final Context context;
    private final ArrayList<ServiceInfo> services;

    public DeviceDiscoveryAdapter(Context context, ArrayList<ServiceInfo> services) {
        super(context, 0, services);
        this.toastHandler = new ToastHandler(context);
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        ServiceInfo serviceInfo = services.get(position);
        String username = serviceInfo.getPropertyString(PROP_USER_ALIAS);
        TextView serviceName = convertView.findViewById(android.R.id.text1);
        serviceName.setText(username);

        convertView.setOnClickListener(view -> startMessageActivity(serviceInfo));

        return convertView;
    }

    private void startMessageActivity(ServiceInfo service) {
        String username = service.getPropertyString(PROP_USER_ALIAS);

        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(INTENT_MESSAGE_SERVICE_IP, service.getInet4Addresses()[0].getHostAddress());
        intent.putExtra(INTENT_MESSAGE_USER, username);
        context.startActivity(intent);
    }
}
