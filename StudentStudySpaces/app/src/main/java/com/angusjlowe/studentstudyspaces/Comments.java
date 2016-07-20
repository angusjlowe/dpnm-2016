package com.angusjlowe.studentstudyspaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Comments extends AppCompatActivity {

    private Toolbar myToolbar;
    private ListView commentsView;
    private EditText editTextAddComment;
    private String locationKey;
    private DatabaseReference ref;
    private int currentVotes;
    private String commentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarInfoWindow);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle("Comments");
        commentsView = (ListView) findViewById(R.id.commentsView);
        editTextAddComment = (EditText) findViewById(R.id.editTextAddComment);
        Intent i = getIntent();
        locationKey = i.getStringExtra("KEY");
        ref = FirebaseDatabase.getInstance().getReference();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String,Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                Map<String, Object> commentsMap = dataSnapshot.child("study_spaces").child(locationKey).child("comments").getValue(genericTypeIndicator);
                Set<String> commentKeysSet = commentsMap.keySet();
                ArrayList<String> commentKeys = new ArrayList<String>(commentKeysSet);
                String[][] comments = new String[commentKeys.size()][4];
                for(int j = 0; j < commentKeys.size(); j++) {
                    Map<String, Object> currentComment = (Map<String, Object>) commentsMap.get(commentKeys.get(j));
                    String[] details = new String[4];
                    details[0] = (String) currentComment.get("content");
                    details[1] = (String) currentComment.get("date");
                    details[2] = (String) currentComment.get("votes");
                    details[3] = commentKeys.get(j);
                    comments[j] = details;
                }
                commentsInit(comments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void commentsInit(final String[][] commentsArg) {
        //copy arg into new array
        final String[][] comments = new String[commentsArg.length][4];
        for(int i = 0; i < comments.length; i++) {
            comments[i] = commentsArg[i];
        }
        //--------  bubble sort of votes
        boolean flag = true;
        String temp[];
        while(flag) {
            flag = false;
            for(int j = 0; j < comments.length - 1; j++) {
                if(Integer.parseInt(comments[j][2]) < Integer.parseInt(comments[j+1][2])) {
                    temp = comments[j];
                    comments[j] = comments[j+1];
                    comments[j+1] = temp;
                    flag = true;
                }
            }
        }
        //--------
        List<Map<String, String>> map = new ArrayList<Map<String,String>>();
        for(int i = 0; i < comments.length; i++) {
            Map<String, String> row = new HashMap<String, String>(2);
            row.put("content", comments[i][0]);
            row.put("dateAndVotes", comments[i][1] + "\nVotes: " + comments[i][2]);
            map.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, map, android.R.layout.simple_list_item_2, new String[] {"content", "dateAndVotes"},
                new int[] {android.R.id.text1, android.R.id.text2});
        commentsView.setAdapter(adapter);
        commentsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
                commentKey = comments[i][3];
                ref.child("study_spaces").child(locationKey).child("comments").child(commentKey)
                        .child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentVotes = Integer.parseInt(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(Comments.this).setTitle("Votes");
                builder.setPositiveButton("Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ref.child("study_spaces").child(locationKey).child("comments").child(commentKey)
                                .child("votes").setValue(String.valueOf(currentVotes + 1));
                    }
                });
                builder.setNegativeButton("Down", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ref.child("study_spaces").child(locationKey).child("comments").child(commentKey)
                                .child("votes").setValue(String.valueOf(currentVotes - 1));
                    }
                });
                builder.show();
            }
        });
    }

    public void addComment(View v) {
        String content = editTextAddComment.getText().toString();
        DatabaseReference comments = ref.child("study_spaces").child(locationKey).child("comments");
        Map<String, Object> commentDetails = new HashMap<>();
        commentDetails.put("content", content);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        commentDetails.put("date", date.toString());
        commentDetails.put("votes", "0");
        comments.push().setValue(commentDetails);
    }

}
