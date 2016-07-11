package com.angusjlowe.studentstudyspaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText editTextLocation;
    private EditText editTextName;
    Button buttonAddLocation;
    String alertDialogMessage;
    DatabaseReference studySpaces;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.i("MainActivity", "Broooo");
            startActivity(new Intent(this, GoogleSignInActivity.class));
            finish();
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        studySpaces = mFirebaseDatabaseReference.child("study_spaces");
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAddLocation = (Button) findViewById(R.id.buttonAddLocation);




        studySpaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                GenericTypeIndicator<Map<String,Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                };
                //for the Map
                HashMap<String,LatLng> namesAndCoords = new HashMap<String, LatLng>();
                String detailsString = new String("");
                for(DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    //get study space details and convert to string for display on textview in mainactivity
                    Map<String, Object> details = dataSnapshot.getValue(genericTypeIndicator);
                    String name = (String) details.get("name");
                    String location = (String) details.get("location");
                    DataSnapshot comments = dataSnapshot.child("comments");
                    if(comments != null) {
                        for(DataSnapshot comment : comments.getChildren()) {
                            Map<String, Object> commentDetails = comment.getValue(genericTypeIndicator);
                            for(String attribute : commentDetails.keySet()) {
                                detailsString += attribute + ": " + commentDetails.get(attribute);
                            }
                        }
                    }
                    detailsString += name + ": " + location + " ";
                    alertDialogMessage = detailsString;


                    //format study space details and add to hashmap for markers
                    location.replaceAll(" ", "");
                    String[] latAndLngArray = location.split(",");
                    double lat = Double.parseDouble(latAndLngArray[0]);
                    double lng = Double.parseDouble(latAndLngArray[1]);
                    LatLng newLatLng = new LatLng(lat,lng);
                    namesAndCoords.put(name, newLatLng);
                }

                //store value of hashmap in static values class
                StaticValues.namesAndCoords = namesAndCoords;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //update database according to user input - add study space attributes
                String stringLocation = editTextLocation.getText().toString();
                String stringName = editTextName.getText().toString();
                Map<String, Object> details = new HashMap<String, Object>();
                details.put("location", stringLocation);
                details.put("name", stringName);
                details.put("rating", "");
                details.put("image", "");
                details.put("decibel", "");
                details.put("decibel_list", "");
                details.put("rating_list", "");



                //update study space
                studySpaces.push().setValue(details);



                Toast.makeText(getApplicationContext(), "Database updated successfully", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void serverCheck(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("Firebase JSON").setMessage(alertDialogMessage);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }


    public void goToMap(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void goToAddComments(View v) {
        Intent intent = new Intent(this, AddComments.class);
        startActivity(intent);
    }

    public void goToSignIn(View v) {
        Intent intent = new Intent(this, GoogleSignInActivity.class);
        startActivity(intent);
    }

    public void signOut(View v) {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("hi", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}

