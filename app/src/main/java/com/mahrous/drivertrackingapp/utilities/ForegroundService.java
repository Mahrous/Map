package com.mahrous.drivertrackingapp.utilities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mahrous.drivertrackingapp.R;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    DatabaseReference databaseReference;
    String userId;
    SharedPreferences preferences;
    Handler handler = new Handler();
    Runnable runnable2;
    private String userToken;


    @Override
    public void onCreate() {
        super.onCreate();

    }


    private void gelLocation() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, new

                    LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            float speedX = location.getSpeed() * 18 ;
                            float speed = speedX / 5 ;

                            updateStatusFirebase(location.getLongitude(), location.getLatitude(),speed);



                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                            // TODO Auto-generated method stub
                        }
                    });

        }
    }


    private void updateStatusFirebase(double lo, double la, float speed) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("driver").child(userId);
        databaseReference.child("lon").setValue(String.valueOf(lo));
        databaseReference.child("lat").setValue(String.valueOf(la));
        databaseReference.child("speed_limit").setValue(String.valueOf(speed));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        String tripId = intent.getStringExtra("tripId");
        String driverId = intent.getStringExtra("driverId");
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.formavatar)
                .build();

        startForeground(1, notification);


        gelLocation();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable2);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }





}