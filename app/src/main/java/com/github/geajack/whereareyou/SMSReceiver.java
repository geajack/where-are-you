package com.github.geajack.whereareyou;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.BATTERY_SERVICE;

public class SMSReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null) {
            byte[][] pdus = (byte[][]) extras.get("pdus");

            if (pdus.length > 0)
            {
                SmsMessage message = SmsMessage.createFromPdu(pdus[0]);
                String messageBody = message.getMessageBody();
                String phoneNumber = message.getOriginatingAddress();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                String secretPhrase = preferences.getString("secret_phrase", "where are you");

                if (messageBody.trim().toLowerCase().equals(secretPhrase.trim().toLowerCase()))
                {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    try
                    {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(new LocationClientListener(context, phoneNumber));
                    }
                    catch (SecurityException exception)
                    {
                        SmsManager smsManager = SmsManager.getDefault();
                        String response = "I'm sorry, I can't give you my location because I'm lacking Android permissions.";
                        smsManager.sendTextMessage(phoneNumber, null, response, null, null);
                    }
                }
            }
        }
    }

    private static class LocationClientListener implements OnSuccessListener<Location>
    {
        private final String phoneNumber;
        private final Context context;

        public LocationClientListener(Context context, String phoneNumber) {
            this.phoneNumber = phoneNumber;
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSuccess(Location location)
        {
            String locality = null;
            try {
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    locality = addresses.get(0).getLocality();
                }
            } catch (IOException ignored) {
            }

            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            int battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            String message = "I am at (" + location.getLatitude() + ", " + location.getLongitude() + ")";
            if (locality != null)
            {
                message += " in " + locality;
            }
            message += ". My battery level is " + battery + "%.";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }
}
