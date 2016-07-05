package com.angusjlowe.studentstudyspaces;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddComments extends AppCompatActivity {

    int votes;
    Button upButton;
    Button downButton;
    EditText editTextSelectStudySpace;
    EditText editTextAddComment;
    EditText editTextSelectStudySpaceForVoting;
    EditText editTextSelectComment;

    Firebase studySpaces = new Firebase("https://studentstudyspaces.firebaseio.com/").child("study spaces");
    Firebase commentVotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comments);
        downButton = (Button) findViewById(R.id.buttonDownVote);
        upButton = (Button) findViewById(R.id.buttonUpVote);
        editTextSelectComment = (EditText) findViewById(R.id.editTextSelectComment);
        editTextAddComment = (EditText) findViewById(R.id.editTextContent);
        editTextSelectStudySpace = (EditText) findViewById(R.id.editTextStudySpaceName);
        editTextSelectStudySpaceForVoting = (EditText) findViewById(R.id.editTextSelectStudySpaceForVoting);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studySpaceId = editTextSelectStudySpaceForVoting.getText().toString();
                String commentId = editTextSelectComment.getText().toString();
                commentVotes = studySpaces.child(studySpaceId).child("comments").child(commentId).child("votes");
                commentVotes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String votesString = dataSnapshot.getValue(String.class);
                        votes = Integer.parseInt(votesString);
                        votes --;
                        commentVotes.setValue(String.valueOf(votes));
                        commentVotes.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studySpaceId = editTextSelectStudySpaceForVoting.getText().toString();
                String commentId = editTextSelectComment.getText().toString();
                commentVotes = studySpaces.child(studySpaceId).child("comments").child(commentId).child("votes");
                commentVotes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String votesString = dataSnapshot.getValue(String.class);
                        votes = Integer.parseInt(votesString);
                        votes ++;
                        commentVotes.setValue(String.valueOf(votes));
                        commentVotes.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });
    }

    public void addComment(View v) {
        String content = editTextAddComment.getText().toString();
        String studySpaceId = editTextSelectStudySpace.getText().toString();
        Firebase comments = studySpaces.child(studySpaceId).child("comments");
        Map<String, Object> commentDetails = new HashMap<>();
        commentDetails.put("content", content);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        commentDetails.put("date", date.toString());
        commentDetails.put("votes", "0");
        comments.push().setValue(commentDetails);
    }

}
