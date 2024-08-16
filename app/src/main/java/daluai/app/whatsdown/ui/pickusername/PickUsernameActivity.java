package daluai.app.whatsdown.ui.pickusername;

import static daluai.app.whatsdown.ui.ActivityApi.INTENT_EXTRA_USERNAME;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import daluai.app.sdk_boost.wrapper.Logger;
import daluai.app.whatsdown.R;
import daluai.app.whatsdown.data.manager.UserValueKeys;
import daluai.app.whatsdown.data.manager.UserValueManager;

@AndroidEntryPoint
public class PickUsernameActivity extends AppCompatActivity {

    // todo: handle user going back without setting username

    @Inject
    UserValueManager userValueManager;

    private static final Logger LOG = Logger.ofClass(PickUsernameActivity.class);

    private Button button;
    private EditText usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        initializeComponents();
        addTextChangedListener();
        setOnClickListener();
    }

    private void initializeComponents() {
        button = findViewById(R.id.usernameDoneButton);
        usernameInput = findViewById(R.id.usernameEditText);
        userValueManager.getUserValue(UserValueKeys.USERNAME, userValue -> {
            if (userValue == null || userValue.getValue().equals(UserValueKeys.USERNAME.getDefaultValue())) {
                // leave edit text empty so that user sees hint
                return;
            }
            usernameInput.setText(userValue.getValue());
        });
    }

    private void addTextChangedListener() {
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUsernameInputInvalid(s)) {
                    usernameInput.setError("Only letters and digits are allowed");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setOnClickListener() {
        button.setOnClickListener(view -> {
            String usernameInputString = usernameInput.getText().toString();
            LOG.i("Username input: " + usernameInputString);
            if (isUsernameInputInvalid(usernameInputString)) {
                Toast.makeText(getApplicationContext(), "Invalid username", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra(INTENT_EXTRA_USERNAME, usernameInputString);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private static boolean isUsernameInputInvalid(CharSequence s) {
        String userString = s.toString();
        return userString.trim().isEmpty() || !userString.matches("[a-zA-Z0-9]*");
    }
}
