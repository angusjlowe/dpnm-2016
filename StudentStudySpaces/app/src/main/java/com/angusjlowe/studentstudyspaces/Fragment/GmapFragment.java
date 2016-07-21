package com.angusjlowe.studentstudyspaces.Fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.angusjlowe.studentstudyspaces.InfoWindow;
import com.angusjlowe.studentstudyspaces.InfoWindowAdd;
import com.angusjlowe.studentstudyspaces.MainActivity;
import com.angusjlowe.studentstudyspaces.PermissionUtils;
import com.angusjlowe.studentstudyspaces.R;
import com.angusjlowe.studentstudyspaces.StaticValues;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;


public class GmapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient client;
    private HashMap<Marker, String> locationKeys;
    public static String newposition;
    private Spinner maptype;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gmap, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationKeys = new HashMap<>();
        maptype = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.maptype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maptype.setAdapter(adapter);
        maptype.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) GmapFragment.this);
        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        client = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();
        fragment.getMapAsync(this);
    }

    private void updateMapType(){
        if (mMap==null)
            return;
        String layerName = ((String) maptype.getSelectedItem());
        if(layerName.equals("Normal"))
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else if(layerName.equals("Hybrid"))
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else if(layerName.equals("Terrain"))
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        else if(layerName.equals("Satellite"))
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
            Log.i("LDA","Error setting layer with name "+layerName);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateMapType();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //do nothing
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        updateMapType();
        for(String[] keyNameAndRating : StaticValues.KeysNamesRatingsAndCoords.keySet()) {
            Marker m = mMap.addMarker(new MarkerOptions().position(StaticValues.KeysNamesRatingsAndCoords.get(keyNameAndRating)).title(keyNameAndRating[1]).snippet("Rating: "+ keyNameAndRating[2]).icon(BitmapDescriptorFactory.fromResource(R.drawable.s3)));
            locationKeys.put(m, keyNameAndRating[0]);
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
                final Handler handler = new Handler();
                final long start = SystemClock.uptimeMillis();
                final long duration = 1000;
                final Interpolator interpolator = new BounceInterpolator();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long elapsed = SystemClock.uptimeMillis() - start;
                        float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                        newpoint.setAnchor(0.5f, 1.0f+2*t);
                        if(t>0.0){
                            handler.postDelayed(this, 16);
                        }
                    }
                });
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker){
                if(locationKeys.get(marker)!=null) {
                    Toast.makeText(getActivity(), "moving to information tab", Toast.LENGTH_SHORT).show();
                    String locationKey = locationKeys.get(marker);
                    Intent infow = new Intent(getActivity(), InfoWindow.class);
                    infow.putExtra("KEY", locationKey);
                    startActivity(infow);
                }
                else{
                    newposition = marker.getPosition().toString();
                    Intent infowa = new Intent(getActivity(), InfoWindowAdd.class);
                    startActivity(infowa);
                }
            }
        });
    }
    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION,true);
        }
        else if(mMap!=null){
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick(){
        Toast.makeText(getActivity(), "moving to your location", Toast.LENGTH_SHORT).show();
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


    private void showMissingPermissionError(){
        PermissionUtils.PermissionDeniedDialog.newInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.angusjlowe.studentstudyspaces/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.angusjlowe.studentstudyspaces/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
//    public void changecolor(){
//        Float number = 2.1f;
//        ColorMatrix desatMatrix = new ColorMatrix();
//        desatMatrix.setSaturation(number);
//        ColorFilter paintColorFilter = new ColorMatrixColorFilter(desatMatrix);
//        Paint paint = new Paint();
//        paint.setColorFilter(paintColorFilter);
//        Canvas canvas = new Canvas(null);
//        canvas.drawBitmap(null,0,0,paint);
//    }
}
