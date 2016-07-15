package com.angusjlowe.studentstudyspaces;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.media.MediaRecorder;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CheckIn extends AppCompatActivity {

    private String userName = StaticValues.userName;
    private DatabaseReference ref;
    private DatabaseReference occupancyList;

    private EditText editTextLocationId;
    private String location;
    private String occupantKey;
    Button buttonCheckIn;
    Button buttonCheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        editTextLocationId = (EditText) findViewById(R.id.editTextLocationId);
        buttonCheckIn = (Button) findViewById(R.id.buttonCheckIn);
        buttonCheckOut = (Button) findViewById(R.id.buttonCheckOut);
        ref = FirebaseDatabase.getInstance().getReference();
    }

    public void checkInNow(View v) {
        location = editTextLocationId.getText().toString();
        occupancyList = ref.child("study_spaces").child(location).child("occupants");
        DatabaseReference newOccupant = occupancyList.push();
        newOccupant.setValue(userName);
        occupantKey = newOccupant.getKey();

        //starting the mic
        //note: there should be a sort of loading activity during this time, like a spinning wheel or circle on the screen.
        decibels = ref.child("study_spaces").child(location).child("decibel_list");
        max = decibel_levels();

        DatabaseReference sounds = decibels.push();
        sounds.setValue(max);

        //switch button functionalities
        buttonCheckIn.setVisibility(View.INVISIBLE);
        buttonCheckOut.setVisibility(View.VISIBLE);
    }

    public void checkOutNow(View v) {
        occupancyList.child(occupantKey).removeValue();
        buttonCheckOut.setVisibility(View.INVISIBLE);
        buttonCheckIn.setVisibility(View.VISIBLE);
    }

    //insert the things
    public Int decibel_levels(){
        MediaRecorder rec = new MediaRecorder;
        Int max;

        // create microphone and begin to record.
        rec.setAudioSource(MediaRecorder.Audiosource.MIC);
        rec.prepare();
        rec.start();
        rec.getMaxAmplitude();
        rec.release();

        Timeunit.SECONDS.sleep(3); // getMaxAmplitude compares its values to the last sample of the location sounds
        max = rec.getMaxAmplitude();

        return max;
    }
}
