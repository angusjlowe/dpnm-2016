package com.angusjlowe.studentstudyspaces;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import pl.pawelkleczkowski.customgauge.CustomGauge;

/**
 * Created by Sungjin Park on 7/6/2016.
 */
public class InfoWindow extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private android.support.v7.widget.Toolbar myToolbar;
    private static Button bsubmit;
    private static TextView trating;
    private static RatingBar ratingb;
    private RatingBar ratingBarAverageRating;
    private DatabaseReference ref;
    private DatabaseReference studySpace;
    private DatabaseReference ratingList;
    private DatabaseReference userRef;
    private String locationKey;
    private Map<String, Object> map;
    private TextView textViewLocationTitle;
    private String ratingListString;
    private String imageUrls;
    private Uri uri;
    private TextView textViewAverageRating;
    private UploadTask uploadTask;
    private StorageReference storageReference;
    private StorageReference studySpacePhotoRef;
    private ImageView[] imageViews;
    private Bitmap bitmapForUpload;
    private ProgressBar progressBarUpload;
    private Switch switchCheckIn;
    private CustomGauge gauge1;
    private String keyForCurrentOccupant;
    private String currentLocation;

    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private int PICK_IMAGE_REQUEST = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);
        //initialize firebase and views first
        viewsInit();
        firebaseInit();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot userSnapshot = dataSnapshot.child("users").child(user.getUid());
                DataSnapshot studySpaceSnapshot = dataSnapshot.child("study_spaces").child(locationKey);
                currentLocation = userSnapshot.child("current_location").getValue(String.class);
                ratingListString = studySpaceSnapshot.child("rating_list").getValue(String.class);
                textViewLocationTitle.setText(studySpaceSnapshot.child("name").getValue(String.class));
                textViewAverageRating.setText(studySpaceSnapshot.child("rating").getValue(String.class));
                ratingBarAverageRating.setRating(Float.parseFloat(textViewAverageRating.getText().toString()));
                imageUrls = studySpaceSnapshot.child("image").getValue(String.class);
                int occupancyLevel = Integer.parseInt(studySpaceSnapshot.child("num_occupants").getValue(String.class)) * 200;
                gauge1.setValue(occupancyLevel);
                try {
                    //activate listeners dependent on map instance variable
                    placeCurrentPhotos(imageUrls);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for(DataSnapshot userSnap : studySpaceSnapshot.child("occupants").getChildren()) {
                    String occupantId = userSnap.getValue(String.class);
                    if(occupantId.equals(user.getUid())) {
                        keyForCurrentOccupant = userSnap.getKey();
                    }
                }
                switchChecker();
                listenerForRatingBar();
                listenerForRatingSubmit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_window, menu);
        return true;
    }

    public void listenerForRatingBar() {
        ratingb.setOnRatingBarChangeListener(
                new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
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
                bitmapForUpload = bitmap;
                Toast.makeText(InfoWindow.this, "Photo Ready for Upload", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.launcher_icon:
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public void uploadPhoto(View v) throws FileNotFoundException {
        new uploadPhotoTask(bitmapForUpload).execute();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InfoWindow Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.angusjlowe.studentstudyspaces/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InfoWindow Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.angusjlowe.studentstudyspaces/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public class uploadPhotoTask extends AsyncTask<Void, Void, Boolean> {
        Bitmap bitmap;

        public uploadPhotoTask(Bitmap bitmap) {
            this.bitmap = bitmap;
            progressBarUpload.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Uri file = uri;
            studySpacePhotoRef = storageReference.child("images/").child(locationKey).child(file.getLastPathSegment());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
            InputStream stream = new ByteArrayInputStream(baos.toByteArray());
            uploadTask = studySpacePhotoRef.putStream(stream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    imageUrls += downloadUrl + ", ";
                    studySpace.child("image").setValue(imageUrls);
                }
            });
            return true;
        }

        protected void onPostExecute(Boolean result) {
            progressBarUpload.setVisibility(View.GONE);
        }
    }


    public void placeCurrentPhotos(String imageUrls) throws IOException {
        String formattedUrls = imageUrls.replaceAll(" ", "");
        String[] urlsArray = formattedUrls.split(",");
        if (!(formattedUrls.equals(""))) {
            for (int i = 0; i < imageViews.length; i++) {
                if (urlsArray.length > 1 && !urlsArray[i].equals("")) {
                    Picasso.with(getBaseContext()).load(urlsArray[i]).placeholder(R.drawable.ic_image_placeholder)
                            .resize(1200, 780).onlyScaleDown().centerCrop().into(imageViews[i]);
                }
            }
        }

    }


    public void goToPhotos(View v) {
        Intent intent = new Intent(this, Photos.class);
        intent.putExtra("urls", "");
        startActivity(intent);
    }

    public void listenerForRatingSubmit() {
        bsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(InfoWindow.this, String.valueOf(ratingb.getRating()), Toast.LENGTH_SHORT).show();
                String newRatingListString = ratingListString + ", " + String.valueOf(ratingb.getRating());
                ratingList.setValue(newRatingListString);
                bsubmit.setVisibility(View.INVISIBLE);
                Toast.makeText(InfoWindow.this, "Rating Submitted", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void switchChecker() {
        if(currentLocation.equals("")) {
            switchCheckIn.setChecked(false);
        }
        else {
            switchCheckIn.setChecked(true);
        }

        switchCheckIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchCheckIn.setText("Check Out");
                    DatabaseReference newOccupant = studySpace.child("occupants").push();
                    keyForCurrentOccupant = newOccupant.getKey();
                    newOccupant.setValue(user.getUid());
                    userRef.child("current_location").setValue(locationKey);
                }
                else {
                    switchCheckIn.setText("Check In");
                    studySpace.child("occupants").child(keyForCurrentOccupant).removeValue();
                    userRef.child("current_location").setValue("");
                }
            }
        });
    }

    public void firebaseInit() {
        Intent i = getIntent();
        locationKey = i.getStringExtra("KEY");
        ref = FirebaseDatabase.getInstance().getReference();
        studySpace = ref.child("study_spaces").child(locationKey);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = ref.child("users").child(user.getUid());
        ratingList = studySpace.child("rating_list");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://studentstudyspaces.appspot.com");
    }

    public void viewsInit() {
        ratingb = (RatingBar) findViewById(R.id.ratingBar);
        bsubmit = (Button) findViewById(R.id.buttonsbm);
        trating = (TextView) findViewById(R.id.textv);
        textViewLocationTitle = (TextView) findViewById(R.id.textViewLocationTitle);
        ImageView imageViewCurrentPhoto1 = (ImageView) findViewById(R.id.imageViewCurrentPhoto1);
        ImageView imageViewCurrentPhoto2 = (ImageView) findViewById(R.id.imageViewCurrentPhoto2);
        imageViews = new ImageView[]{imageViewCurrentPhoto1, imageViewCurrentPhoto2};
        textViewAverageRating = (TextView) findViewById(R.id.textViewAverageRating);
        progressBarUpload = (ProgressBar) findViewById(R.id.progressBarUpload);
        ratingBarAverageRating = (RatingBar) findViewById(R.id.ratingBarAverageRating);
        switchCheckIn = (Switch) findViewById(R.id.switchCheckIn);
        gauge1 = (CustomGauge) findViewById(R.id.gauge1);
        myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarInfoWindow);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

}