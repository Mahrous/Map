package com.mahrous.drivertrackingapp.ui.map;

import static java.lang.Double.parseDouble;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mahrous.drivertrackingapp.R;

import com.mahrous.drivertrackingapp.databinding.ActivityMapsBinding;
import com.mahrous.drivertrackingapp.utilities.ForegroundService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private ActivityMapsBinding binding;
    SharedPreferences.Editor locationEditor;
    double lo, la;

    SharedPreferences locationPref;
    Marker userLocationMarker;
    private GoogleMap mMap;

    Handler handler = new Handler();
    Runnable runnable;
    Handler handlerUpdateLocation = new Handler();
    Runnable runnableUpdateLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        binding.setModel(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        setUpData();
       updateLocationToFB();

        binding.playForegroundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        handlerUpdateLocation.removeCallbacks(runnableUpdateLocation);
    }

    private void updateLocation(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        else {
            Log.e("TAG", "onLocationChanged: iam not working "  );

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, new

                    LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                            Log.e("TAG", "onLocationChanged: iam working " + location.getLatitude() );
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


    private void updateLocationToFB() {

        handlerUpdateLocation.postDelayed(runnableUpdateLocation = new Runnable() {
            public void run() {

                handler.postDelayed(runnableUpdateLocation, 1000);
                updateLocation();
            }
        }, 1000);
    }



    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }



    private void setUserLocationMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (userLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_pnd));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float)0.5 , (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdate();
        }else {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
        handler.removeCallbacks(runnable);
        handlerUpdateLocation.removeCallbacks(runnableUpdateLocation);
    }

    private void setUpData(){

        locationPref = getSharedPreferences("location", MODE_PRIVATE);
        locationEditor = locationPref.edit();
        la = parseDouble(locationPref.getString("lat", "0"));
        lo = parseDouble(locationPref.getString("long", "0"));


    }

    public void startService() {
        handler.removeCallbacks(runnable);
        handlerUpdateLocation.removeCallbacks(runnableUpdateLocation);
        Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);


    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerDragListener(this);
    }


    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }



}