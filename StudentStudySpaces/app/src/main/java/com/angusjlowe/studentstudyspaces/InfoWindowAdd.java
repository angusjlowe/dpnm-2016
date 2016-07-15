package com.angusjlowe.studentstudyspaces;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.phenotype.Flag;
import com.google.android.gms.vision.text.Text;

import static java.lang.String.*;

/**
 * Created by Sungjin Park on 7/13/2016.
 */
public class InfoWindowAdd extends AppCompatActivity {

    public TextView lat;
    public TextView lng;
    private EditText tname;
    public String locationname;
    public float lats;
    public float lngs;
    Button buttonSubmit;
    TextView submitted;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window_add);
        buttonSubmit = (Button) findViewById(R.id.bsubmit);
        getPosition();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        submitted = (TextView) findViewById(R.id.textViewSubmitted);
        submitted.setVisibility(View.INVISIBLE);
    }
    private void getPosition() {
        lat = (TextView) findViewById(R.id.tlat);
        lng = (TextView) findViewById(R.id.tlng);
        lat.setText(MapsActivity.newposition.substring(MapsActivity.newposition.indexOf("(")+1,MapsActivity.newposition.indexOf(".")+6));
        lng.setText(MapsActivity.newposition.substring(MapsActivity.newposition.indexOf(",")+1,MapsActivity.newposition.lastIndexOf(".")+6));
        lats = Float.parseFloat(lat.getText().toString());
        lngs = Float.parseFloat(lng.getText().toString());
        Toast.makeText(InfoWindowAdd.this, valueOf(lats) + " " +valueOf(lngs) , Toast.LENGTH_SHORT).show();
    }
    public void buttonOnClick(View v){
        Button button = (Button) v;
        ((Button) v).setText("submitted");
        tname = (EditText) findViewById(R.id.tname);
        locationname = tname.getText().toString();
        buttonSubmit.setVisibility(View.INVISIBLE);
        submitted.setText("Location Name = "+tname.getText());
        submitted.setVisibility(View.VISIBLE);
    }
}
