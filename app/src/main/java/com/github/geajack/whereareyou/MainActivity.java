package com.github.geajack.whereareyou;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] requiredPermissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < requiredPermissions.length; i++)
        {
            if (checkSelfPermission(requiredPermissions[i]) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsToRequest.add(requiredPermissions[i]);
            }
        }

        if (permissionsToRequest.size() > 0)
        {
            setContentView(R.layout.permissions_page);
            Button grantPermissionsButton = (Button) findViewById(R.id.grantPermissionsButton);
            grantPermissionsButton.setOnClickListener(new PermissionsButtonListener(this, (String[]) permissionsToRequest.toArray(new String[0])));
        }
        else
        {
            goToMainLayout();
        }
    }

    private void goToMainLayout()
    {
        setContentView(R.layout.activity_main);
        final EditText phraseInput = (EditText) findViewById(R.id.phraseInput);
        phraseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String secretPhrase = phraseInput.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("secret_phrase", secretPhrase);
                editor.apply();
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String secretPhrase = preferences.getString("secret_phrase", "where are you");

        phraseInput.setText(secretPhrase);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean somePermissionsDenied = false;
        for (int i = 0; i < grantResults.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                somePermissionsDenied = true;
            }
        }

        if (!somePermissionsDenied)
        {
            goToMainLayout();
        }
    }

    static private class PermissionsButtonListener implements View.OnClickListener {
        private Activity parentActivity;
        private final String[] permissionsToRequest;

        public PermissionsButtonListener(Activity parentActivity, String[] permissionsToRequest) {
            this.parentActivity = parentActivity;
            this.permissionsToRequest = permissionsToRequest;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View button) {
            parentActivity.requestPermissions(permissionsToRequest, 1000);
        }
    }
}