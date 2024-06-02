package daluai.app.whatsdown.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> services;

    public DeviceAdapter(Context context, ArrayList<String> services) {
        super(context, 0, services);
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String service = services.get(position);
        TextView serviceName = convertView.findViewById(android.R.id.text1);
        serviceName.setText(service);

        return convertView;
    }
}
