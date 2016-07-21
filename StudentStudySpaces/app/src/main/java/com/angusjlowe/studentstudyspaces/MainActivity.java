package com.angusjlowe.studentstudyspaces;


import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.angusjlowe.studentstudyspaces.Fragment.GmapFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    boolean isUserThere = false;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference mFirebaseDatabaseReference;
    DatabaseReference studySpaces;
    DatabaseReference users;
    String currentLocationKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.i("MainActivity", "Broooo");
            startActivity(new Intent(this,  GoogleSignInActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        //set statusbar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        studySpaces = mFirebaseDatabaseReference.child("study_spaces");
        users = mFirebaseDatabaseReference.child("users");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                GenericTypeIndicator<Map<String,Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                //for the Map
                DataSnapshot studySpaces = dataSnapshots.child("study_spaces");
                DataSnapshot users = dataSnapshots.child("users");
                currentLocationKey = users.child(mFirebaseUser.getUid()).child("current_location").getValue(String.class);
                HashMap<String[],LatLng> KeysNamesRatingsAndCoords = new HashMap<String[], LatLng>();
                for(DataSnapshot dataSnapshot : studySpaces.getChildren()) {
                    //get study space details and convert to string for display on textview in mainactivity
                    String locationKey = dataSnapshot.getKey();
                    Map<String, Object> details = dataSnapshot.getValue(genericTypeIndicator);
                    String name = (String) details.get("name");
                    String location = (String) details.get("location");
                    String rating = (String) details.get("rating");
                    //format study space details and add to hashmap for markers
                    location.replaceAll(" ", "");
                    String[] latAndLngArray = location.split(",");
                    double lat = Double.parseDouble(latAndLngArray[0]);
                    double lng = Double.parseDouble(latAndLngArray[1]);
                    LatLng newLatLng = new LatLng(lat,lng);
                    String[] keyNameAndRating = new String[3];
                    keyNameAndRating[0] = locationKey;
                    keyNameAndRating[1] = name;
                    keyNameAndRating[2] = rating;
                    KeysNamesRatingsAndCoords.put(keyNameAndRating, newLatLng);
                }
                //store value of hashmap in static values class
                StaticValues.KeysNamesRatingsAndCoords = KeysNamesRatingsAndCoords;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header=navigationView.getHeaderView(0);
        TextView textViewWelcome = (TextView)header.findViewById(R.id.textViewWelcome);
        String welcome = "Welcome " + mFirebaseUser.getDisplayName();
        textViewWelcome.setText(welcome);
        navigationView.setNavigationItemSelectedListener(this);
        FragmentManager fm = getFragmentManager();
        if(StaticValues.KeysNamesRatingsAndCoords!=null) {
            fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        }

        existingUserSetUp();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();

        int id = item.getItemId();

        if (id == R.id.frame_map) {
            fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        } else if (id == R.id.your_location) {
            if(!(currentLocationKey.equals("") || currentLocationKey == null)) {
                Intent intent = new Intent(this, InfoWindow.class);
                intent.putExtra("KEY", currentLocationKey);
                startActivity(intent);
            }
            else {
                Toast.makeText(MainActivity.this, "Not checked in", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.sign_out) {
            signOut();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent intent = new Intent(this, GoogleSignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("hi", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public void existingUserSetUp() {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot user : dataSnapshot.getChildren()) {
                    if(user.getKey().equals(mFirebaseUser.getUid())) {
                        isUserThere = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(!isUserThere) {
            Map<String,Object> map = new HashMap<>();
            map.put("current_location", "");
            users.child(mFirebaseUser.getUid()).setValue(map);
        }
    }

}