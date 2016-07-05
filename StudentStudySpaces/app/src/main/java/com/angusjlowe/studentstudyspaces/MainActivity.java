package com.angusjlowe.studentstudyspaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private EditText editTextName;
    Button buttonAddLocation;
    String alertDialogMessage;
    Firebase ref = new Firebase("https://studentstudyspaces.firebaseio.com/");
    Firebase studySpaces = ref.child("study spaces");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAddLocation = (Button) findViewById(R.id.buttonAddLocation);

        studySpaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                //for the Map
                HashMap<String,LatLng> namesAndCoords = new HashMap<String, LatLng>();
                String detailsString = new String("");
                for(DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    //get study space details and convert to string for display on textview in mainactivity
                    Map<String, Object> details = dataSnapshot.getValue(Map.class);
                    String name = (String) details.get("name");
                    String location = (String) details.get("location");
                    DataSnapshot comments = dataSnapshot.child("comments");
                    for(DataSnapshot comment : comments.getChildren()) {
                        Map<String, Object> commentDetails = comment.getValue(Map.class);
                        for(String attribute : commentDetails.keySet()) {
                            detailsString += attribute + ": " + commentDetails.get(attribute);
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
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //update database according to user input - add study space attributes
                String stringLocation = editTextLocation.getText().toString();
                String stringName = editTextName.getText().toString();
                Map<String, Object> details = new HashMap<String, Object>();
                Map<String, Object> studySpacesMap = new HashMap<String, Object>();
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
}

