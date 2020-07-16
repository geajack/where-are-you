package com.github.geajack.whereareyou;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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

public class SMSReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        String strMessage = "";
        String strMsgBody = "";
        String strMsgSrc = "";

        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");

            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                strMsgBody = smsmsg.getMessageBody().toString();
                strMsgSrc = smsmsg.getOriginatingAddress();

                strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;
            }

        }

        if (strMsgBody.trim().toLowerCase().equals("where are you"))
        {
            final String message = strMessage;
            final String phoneNumber = strMsgSrc;
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            /*Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Channel")
                                    .setSmallIcon(R.drawable.notification_icon)
                                    .setContentTitle("Where Are You?")
                                    .setContentText(String.valueOf(location.getLatitude()))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);

                            CharSequence name = context.getString(R.string.channel_name);
                            int importance = NotificationManager.IMPORTANCE_DEFAULT;
                            NotificationChannel channel = new NotificationChannel("Channel", name, importance);

                            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                            notificationManager.createNotificationChannel(channel);

                            notificationManager.notify(0, builder.build());*/

                            Geocoder gcd = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String locality = null;
                            if (addresses.size() > 0) {
                                locality = addresses.get(0).getLocality();
                            }

                            String message = "I am at " + location.getLongitude() + ", " + location.getLatitude() + " in " + locality;
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                        }
                    }
            );
        }
    }
}
