package com.example.googlemapdemot6;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import de.nitri.gauge.Gauge;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private Geocoder geocoder;
    private Marker marker;
    private float currentSpeed;
    private Gauge gauge;

    private final String[] PERMISSION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (checkPermission()) {
            init();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String s : PERMISSION) {
                if (ActivityCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(PERMISSION, 0);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermission()) {
            init();
        } else {
            finish();
        }
    }

    private void init() {

        gauge = findViewById(R.id.gauge);
        Button btn = findViewById(R.id.btn_search);
        btn.setOnClickListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        currentSpeed = 0;
        gauge.setValue(currentSpeed);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);
        initMap();
    }

    @SuppressLint("MissingPermission")
    private void initMap() {
        Places.initialize(getApplicationContext(),getString(R.string.google_maps_key));

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);//cho phep zoom in zoom out map len

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1,1,this);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1,1,this);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationChanged(Location location) {

        float s1 = location.getSpeed()*3.6f;//lay ra toc do
        //float s2 = location.getSpeedAccuracyMetersPerSecond();//chinh xac toc do hon nhung api 26 moi dung dc
        currentSpeed = s1;
        String ss = String.format("%.1f",currentSpeed);
        if(0<=currentSpeed && currentSpeed<=240){
            gauge.moveToValue(currentSpeed);
            gauge.setUpperText(ss+"");
        }
//        Log.i("Speed 1: ",""+s1);
        LatLng lng = new LatLng(location.getLatitude(),location.getLongitude());
        CameraPosition position = new CameraPosition(
                lng,18,0,0);
        //v:zoom
        //v1:tilt:Góc, tính theo độ, của góc camera từ nadir (đối diện trực tiếp với Trái đất)
        //       //. Xem độ nghiêng (float) để biết chi tiết về các hạn chế về phạm vi giá trị
        //       //. phạm vi từ 0 đến 90 độ.
        //v2:bearing://Hướng mà máy ảnh hướng vào, theo độ theo chiều kim đồng hồ từ phía bắc.
        //        // Giá trị này sẽ được chuẩn hóa trong phạm vi bao gồm 0 độ và độc quyền 360 độ
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
//        Toast.makeText(this, ""+getAdressNameByPosition(latLng), Toast.LENGTH_SHORT).show();
        if(marker!=null){
            marker.remove();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(marker!=null){
            marker.remove();
        }

        String address = getAdressNameByPosition(latLng);
        marker = drawMarker("LongClick",address,BitmapDescriptorFactory.HUE_BLUE,latLng);

    }

    private Marker drawMarker(String title,String snippet,float hue,LatLng lng){

        //Polygon//ve khoanh vung
        //Polyline
        //Circle//ve hinh tron

        MarkerOptions options = new MarkerOptions();
        options.position(lng);
        options.icon(BitmapDescriptorFactory.defaultMarker(hue));
        options.title(title);
        options.snippet(snippet);
        return mMap.addMarker(options);
    }


    //thuong dung bat dong bo
    private String getAdressNameByPosition(LatLng lng){
        try {
            //truyen vi tri ra list ten duong
            List<Address> arr = geocoder.getFromLocation(lng.latitude,lng.longitude,1);
            String address = "";
            if(arr.size()>0){
                address = arr.get(0).getAddressLine(0);
            }
            return address;
        }catch (Exception ex){
            ex.printStackTrace();
            return "";
        }
    }

    @Override
    public void onClick(View v) {
// Set the fields to specify which types of place data to
// return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

// Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)//co 2 mode OVERLAY VA FULLSCREEN
                .build(this);
        startActivityForResult(intent, 1);
    }
}
