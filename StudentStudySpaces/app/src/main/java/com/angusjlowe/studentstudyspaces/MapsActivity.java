package com.angusjlowe.studentstudyspaces;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.media.Rating;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Permission;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient client;
    private HashMap<Marker, String> locationKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        locationKeys = new HashMap<>();
    }
    public void changeType(View view){
        if(mMap.getMapType()==GoogleMap.MAP_TYPE_NORMAL)
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
    /*public void onZoom(View view){
        if(view.getId()==R.id.Bin)
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        if(view.getId()==R.id.Bout)
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
    }*/
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.9078, 127.7669),6));//start position
        for(String[] keyAndName : StaticValues.KeysNamesAndCoords.keySet()) {
            Marker m = mMap.addMarker(new MarkerOptions().position(StaticValues.KeysNamesAndCoords.get(keyAndName)).title(keyAndName[1]));
            locationKeys.put(m, keyAndName[0]);
        }
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            Marker newpoint;
            @Override
            public void onMapLongClick(LatLng point){
                LatLngBounds.builder();
                if(newpoint!=null)
                    newpoint.remove();
                newpoint = mMap.addMarker(new MarkerOptions().position(point).title("new location").snippet("tap to add new location").draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker){
                Toast.makeText(MapsActivity.this,"moving to information tab",Toast.LENGTH_SHORT).show();
                String locationKey = locationKeys.get(marker);
                Intent intent = new Intent(MapsActivity.this, InfoWindow.class);
                intent.putExtra("KEY", locationKey);
                startActivity(intent);
            }
        });
    }
    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,Manifest.permission.ACCESS_FINE_LOCATION,true);
        }
        else if(mMap!=null){
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public boolean onMyLocationButtonClick(){
        Toast.makeText(this, "moving to your location", Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode!=LOCATION_PERMISSION_REQUEST_CODE)
            return;
        if(PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION))
            enableMyLocation();
        else
            mPermissionDenied=true;
    }
    @Override
    protected void onResumeFragments(){
        super.onResumeFragments();
        if(mPermissionDenied){
            showMissingPermissionError();
            mPermissionDenied=false;
        }
    }
    private void showMissingPermissionError(){
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(),"dialog");
    }
}