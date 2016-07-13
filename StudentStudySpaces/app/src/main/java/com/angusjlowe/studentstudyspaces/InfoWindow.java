package com.angusjlowe.studentstudyspaces;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Sungjin Park on 7/6/2016.
 */
public class InfoWindow extends AppCompatActivity {
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private static Button bsubmit;
    private static TextView trating;
    private static RatingBar ratingb;
    private TextView textViewUrls;
    private DatabaseReference ref;
    private DatabaseReference studySpace;
    private DatabaseReference ratingList;
    private String locationKey;
    private Map<String, Object> map;
    private TextView textViewData;
    private String ratingListString;
    private String imageUrls;
    private Uri uri;
    private UploadTask uploadTask;
    private StorageReference storageReference;
    private StorageReference studySpacePhotoRef;
    private FirebaseUser user;
    private FirebaseAuth auth;

    private FirebaseStorage storage;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);
        Intent i = getIntent();
        locationKey = i.getStringExtra("KEY");
        ref = FirebaseDatabase.getInstance().getReference();
        studySpace = ref.child("study_spaces").child(locationKey);
        ratingList = studySpace.child("rating_list");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://studentstudyspaces.appspot.com");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        ratingb = (RatingBar) findViewById(R.id.ratingBar);
        bsubmit = (Button) findViewById(R.id.buttonsbm);
        textViewData = (TextView) findViewById(R.id.textViewData);
        trating = (TextView) findViewById(R.id.textv);
        textViewUrls = (TextView) findViewById(R.id.textViewUrls);
        listenerForRatingBar();
        //placeCurrentPhotos();
        bsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(InfoWindow.this, String.valueOf(ratingb.getRating()),Toast.LENGTH_SHORT).show();
                String newRatingListString = ratingListString + ", " + String.valueOf(ratingb.getRating());
                ratingList.setValue(newRatingListString);
                bsubmit.setVisibility(View.INVISIBLE);

            }
        });
        studySpace.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String,Object>>() {};
                map = dataSnapshot.getValue(genericTypeIndicator);
                String dataString = map.toString();
                textViewData.setText(dataString);
                ratingListString = (String) map.get("rating_list");
                imageUrls = (String) map.get("image");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    public void listenerForRatingBar() {
        ratingb.setOnRatingBarChangeListener(
                new RatingBar.OnRatingBarChangeListener(){
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser){
                        trating.setText(String.valueOf(rating));
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void selectPhoto(View v) {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    public void uploadPhoto(View v) {
        Uri file = uri;
        studySpacePhotoRef = storageReference.child("images/").child(locationKey).child(user.getUid());
        uploadTask = studySpacePhotoRef.putFile(file);


// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
//        Uri url = studySpacePhotoRef.getDownloadUrl().getResult();
  //      String urlString = url.toString();
    //    imageUrls += urlString + ", ";
      //  studySpace.child("image").setValue(imageUrls);
        Toast.makeText(InfoWindow.this, "Photo Uploaded Successfully",Toast.LENGTH_SHORT).show();
    }

/*    public void placeCurrentPhotos() {
        if(!(imageUrls.equals(""))) {
            List<String> list = new ArrayList<String>(Arrays.asList(imageUrls.split(", ")));
            String urls = new String("");
            for(String s : list) {
                urls += s + " ";
            }
            textViewUrls.setText(urls);
        }

    }*/
}