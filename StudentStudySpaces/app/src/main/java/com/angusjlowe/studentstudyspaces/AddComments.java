package com.angusjlowe.studentstudyspaces;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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
    private DatabaseReference ref;
    private DatabaseReference studySpaces;
    private DatabaseReference commentVotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comments);
        ref = FirebaseDatabase.getInstance().getReference();
        studySpaces = ref.child("study_spaces");
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
                    public void onCancelled(DatabaseError databaseError) {

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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });
            }
        });
    }

    public void addComment(View v) {
        String content = editTextAddComment.getText().toString();
        String studySpaceId = editTextSelectStudySpace.getText().toString();
        DatabaseReference comments = studySpaces.child(studySpaceId).child("comments");
        Map<String, Object> commentDetails = new HashMap<>();
        commentDetails.put("content", content);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        commentDetails.put("date", date.toString());
        commentDetails.put("votes", "0");
        comments.push().setValue(commentDetails);
    }

}
