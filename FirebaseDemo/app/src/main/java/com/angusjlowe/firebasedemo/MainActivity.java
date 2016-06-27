package com.angusjlowe.firebasedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String coordsString = new String("");
    Firebase ref = new Firebase("https://fir-demo-d3194.firebaseio.com/");
    Firebase coords = ref.child("study spaces").child("coords");
    TextView coordsTextView;
    EditText editTextLocationName;
    EditText editTextCoords;
    Button buttonAddLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        coordsTextView = (TextView) findViewById(R.id.textViewCoords);
        editTextCoords = (EditText) findViewById(R.id.editTextCoords);
        editTextLocationName = (EditText) findViewById(R.id.editTextLocationName);
        buttonAddLocation = (Button) findViewById(R.id.buttonAddLocation);
        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String coordsText = editTextCoords.getText().toString();
                String locationText = editTextLocationName.getText().toString();
                Map<String, Object> newEntry = new HashMap<String, Object>();
                newEntry.put(locationText, coordsText);
                coords.updateChildren(newEntry);
                Toast.makeText(MainActivity.this, "Your location was sent successfully", Toast.LENGTH_LONG).show();
            }
        });
        coords.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                coordsString = new String("");
                Map<String, String> coordsMap = dataSnapshot.getValue(Map.class);
                for(String s : coordsMap.keySet()) {
                    coordsString += s + "--" + coordsMap.get(s) + " *** ";
                }
                coordsTextView.setText(coordsString.substring(0, coordsString.length() - 5));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }


}
