package daluai.app.whatsdown.ui.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.model.Message;

public class MessageListViewAdapter extends ArrayAdapter<Message> {


    public MessageListViewAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Message message = getItem(position);
        if (message == null) {
            return convertView;
        }

        if (convertView == null) {
            var layoutId = message.isMine ? R.layout.item_message_right : R.layout.item_message_left;
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.text_message);
        textView.setText(message.messageText);

        return convertView;
    }
}
