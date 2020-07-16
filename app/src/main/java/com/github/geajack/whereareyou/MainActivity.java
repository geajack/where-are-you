package com.github.geajack.whereareyou;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
            setContentView(R.layout.activity_main);
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