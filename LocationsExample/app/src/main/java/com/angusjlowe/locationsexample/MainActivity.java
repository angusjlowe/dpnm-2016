package com.angusjlowe.locationsexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private Button buttonAddLocation;
    private TextView textViewLocations;
    ArrayList<Integer> studySpaceIds = new ArrayList<>();
    Firebase ref = new Firebase("https://locationsexample-66bd7.firebaseio.com/");
    Firebase studySpaces = ref.child("study spaces");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAddLocation = (Button) findViewById(R.id.buttonAddLocation);
        textViewLocations = (TextView) findViewById(R.id.textViewLocations);
        studySpaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                String listOfLocations = new String("");
                for(DataSnapshot studyspace : dataSnapshots.getChildren()) {
                    Map<String, Object> details = studyspace.getValue(Map.class);
                    for(String attribute : details.keySet()) {
                        listOfLocations += attribute + ": " + details.get(attribute) + "\n";
                    }
                }
                textViewLocations.setText(listOfLocations);
                //for the Map
                HashMap<String,LatLng> namesAndCoords = new HashMap<String, LatLng>();
                for(DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    String studySpaceIdString = dataSnapshot.getKey();
                    Integer studySpaceId = Integer.parseInt(studySpaceIdString);
                    studySpaceIds.add(studySpaceId);
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
                StaticValues.namesAndCoords = namesAndCoords;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringLocation = editTextLocation.getText().toString();
                String stringName = editTextName.getText().toString();
                Map<String, Object> details = new HashMap<String, Object>();
                Map<String, Object> studySpacesMap = new HashMap<String, Object>();
                details.put("location", stringLocation);
                details.put("name", stringName);

                //determine the largest study space id before adding a new one
                Integer[] studySpaceIdsArray = studySpaceIds.toArray(new Integer[studySpaceIds.size()]);
                for(Integer i : studySpaceIdsArray) {
                    if(i > StaticValues.largestId) {
                        StaticValues.largestId = i;
                    }
                }

                studySpacesMap.put(String.valueOf(StaticValues.largestId + 1), details);
                studySpaces.updateChildren(studySpacesMap);
            }
        });
    }


    public void goToMap(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
