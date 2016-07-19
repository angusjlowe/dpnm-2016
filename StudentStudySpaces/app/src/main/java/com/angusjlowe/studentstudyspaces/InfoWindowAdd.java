package com.angusjlowe.studentstudyspaces;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.phenotype.Flag;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.*;

/**
 * Created by Sungjin Park on 7/13/2016.
 */
public class InfoWindowAdd extends AppCompatActivity {

    private String lat;
    private String lng;
    private EditText tname;
    private RatingBar r;
    public String locationname;
    public String newRating;
    Button buttonSubmit;
    TextView submitted;

    //firebase instance variables
    DatabaseReference ref;
    FirebaseAuth firebaseAuth;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window_add);
        ref = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        getPosition();
        tname = (EditText) findViewById(R.id.texteditLocationName);
        r = (RatingBar) findViewById(R.id.ratingBar2);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    private void getPosition() {
        lat = MapsActivity.newposition.substring(MapsActivity.newposition.indexOf("(")+1,MapsActivity.newposition.indexOf(".")+6);
        lng = MapsActivity.newposition.substring(MapsActivity.newposition.indexOf(",")+1,MapsActivity.newposition.lastIndexOf(".")+6);
    }

    public void buttonOnClickC(View v){
        buttonSubmit = (Button) findViewById(R.id.buttonSubmitComment);
        submitted = (TextView) findViewById(R.id.textView);
        locationname = tname.getText().toString();
        newRating = Float.toString(r.getRating());
        Map<String, Object> details = new HashMap<String, Object>();
        details.put("location", lat + ", " + lng);
        details.put("name", locationname);
        details.put("rating", "");
        details.put("image", "");
        details.put("decibel", "");
        details.put("decibel_list", "0");
        details.put("rating_list", newRating);
        details.put("num_occupants", "0");
        ref.child("study_spaces").push().setValue(details);
        submitted.setText("New location = "+ locationname + "\nwith rating of " + newRating +"\nat "+lat+", "+lng);
        buttonSubmit.setVisibility(View.INVISIBLE);
        submitted.setVisibility(View.VISIBLE);
        Toast.makeText(InfoWindowAdd.this, "Location Added Successfully\n"+submitted.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
