package com.angusjlowe.studentstudyspaces;

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
    private Button buttonAddLocation;
    private TextView textViewLocations;
    ArrayList<Integer> studySpaceIds = new ArrayList<>();
    ArrayList<Integer> commentIds = new ArrayList<>();
    Firebase ref = new Firebase("https://studentstudyspaces.firebaseio.com/");
    Firebase studySpaces = ref.child("study spaces");
    SeekBar seekBar;
    TextView textViewRating;
    EditText editTextComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAddLocation = (Button) findViewById(R.id.buttonAddLocation);
        textViewLocations = (TextView) findViewById(R.id.textViewLocations);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textViewRating = (TextView) findViewById(R.id.textViewRating);
        editTextComment = (EditText) findViewById(R.id.editTextComment);

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
                    detailsString += comments.getKey();
                    for(DataSnapshot comment : comments.getChildren()) {
                        Map<String, Object> commentDetails = comment.getValue(Map.class);
                        for(String attribute : commentDetails.keySet()) {
                            detailsString += attribute + ": " + commentDetails.get(attribute);
                        }
                    }
                    detailsString += name + ": " + location + " ";
                    textViewLocations.setText(detailsString);

                    //find max id number of study spaces
                    String studySpaceIdString = dataSnapshot.getKey();
                    Integer studySpaceId = Integer.parseInt(studySpaceIdString);
                    studySpaceIds.add(studySpaceId);

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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewRating.setText(String.valueOf(progress));
            }
        });

        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //update database according to user input - add study space attributes
                String stringLocation = editTextLocation.getText().toString();
                String stringName = editTextName.getText().toString();
                String stringComment = editTextComment.getText().toString();
                String stringRating  = String.valueOf(seekBar.getProgress());
                Map<String, Object> details = new HashMap<String, Object>();
                Map<String, Object> studySpacesMap = new HashMap<String, Object>();
                Map<String, Object> commentDetails = new HashMap<String, Object>();
                Map<String, Object> comments = new HashMap<String, Object>();
                details.put("location", stringLocation);
                details.put("name", stringName);
                details.put("rating", stringRating);
                //add comment attributes
                commentDetails.put("content", stringComment);
                commentDetails.put("date", "07/01/2017/14:19");
                commentDetails.put("votes", "0");

                //determine the largest study space id before adding a new one
                Integer[] studySpaceIdsArray = studySpaceIds.toArray(new Integer[studySpaceIds.size()]);
                for(Integer i : studySpaceIdsArray) {
                    if(i > StaticValues.largestStudySpaceId) {
                        StaticValues.largestStudySpaceId = i;
                    }
                }

                //add nested comments attributes
                comments.put("1", commentDetails);
                details.put("comments", comments);
                //update study space
                studySpacesMap.put(String.valueOf(StaticValues.largestStudySpaceId + 1), details);
                studySpaces.updateChildren(studySpacesMap);


                Toast.makeText(getApplicationContext(), "Database updated successfully", Toast.LENGTH_LONG).show();
            }
        });

    }


    public void goToMap(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}

