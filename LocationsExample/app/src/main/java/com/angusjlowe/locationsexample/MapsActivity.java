package com.angusjlowe.locationsexample;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, LatLng> namesAndCoords;
    Firebase ref = new Firebase("https://locationsexample-66bd7.firebaseio.com/");
    Firebase studySpaces = ref.child("study spaces");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try{
            studySpaces.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshots) {
                    setNamesAndCoords(dataSnapshots);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public void setNamesAndCoords(DataSnapshot dataSnapshots) {
        namesAndCoords = new HashMap<String, LatLng>();
        for(DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
            Map<String, Object> details = dataSnapshot.getValue(Map.class);
            String name = (String) details.get("name");
            String location = (String) details.get("location");

            location.replaceAll(" ", "");
            String[] latAndLngArray = location.split(",");
            double lat = Double.parseDouble(latAndLngArray[0]);
            double lng = Double.parseDouble(latAndLngArray[1]);
            LatLng newLatLng = new LatLng(lat,lng);
            namesAndCoords.put(name, newLatLng);
        }
    }


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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(35.997201, 129.318099)));
        for(String name : namesAndCoords.keySet()) {
            mMap.addMarker(new MarkerOptions().position(namesAndCoords.get(name)).title(name));
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(35.997201, 129.318099)).title("Hello"));
    }
}
