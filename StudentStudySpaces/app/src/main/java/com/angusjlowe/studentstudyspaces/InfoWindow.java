package com.angusjlowe.studentstudyspaces;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Created by Sungjin Park on 7/6/2016.
 */
public class InfoWindow extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private static Button bsubmit;
    private static TextView trating;
    private static RatingBar ratingb;
    private DatabaseReference ref;
    private DatabaseReference studySpace;
    private DatabaseReference ratingList;
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
    private FirebaseAuth auth;
    private ImageView[] imageViews;
    private Bitmap bitmapForUpload;
    private ProgressBar progressBarUpload;

    private FirebaseStorage storage;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);
        firebaseInit();
        viewsInit();
        listenerForRatingBar();
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
                ratingListString = (String) map.get("rating_list");
                textViewLocationTitle.setText((String) map.get("name"));
                textViewAverageRating.setText((String) map.get("rating"));
                imageUrls = (String) map.get("image");
                try {
                    placeCurrentPhotos(imageUrls);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    //memory allocation errors
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bitmapForUpload = bitmap;
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

    public void uploadPhoto(View v) throws FileNotFoundException {
        new uploadPhotoTask(bitmapForUpload).execute();

    }

    public class uploadPhotoTask extends AsyncTask<Void,Void,Boolean> {
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
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
        if(!(formattedUrls.equals(""))) {
            for(int i = 0; i < imageViews.length; i++) {
                new DownloadImageTask(imageViews[i])
                        .execute(urlsArray[i]);
            }
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageViews) {
            this.imageView = imageViews;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            //change the code to include multiple photos
            Bitmap bitmap = null;
            String url = urls[0];
            if(!url.equals("")) {
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap results) {
            int width = results.getWidth();
            int height = results.getHeight();
            if(width >= 3000 || height >= 3000) {
                width *= 0.3;
                height *= 0.3;
            }
            imageView.setImageBitmap(Bitmap.createScaledBitmap(results,width,height,false));
        }
    }

    public void goToPhotos(View v) {
        Intent intent = new Intent(this, Photos.class);
        intent.putExtra("urls", "");
        startActivity(intent);
    }

    public void firebaseInit() {
        Intent i = getIntent();
        locationKey = i.getStringExtra("KEY");
        ref = FirebaseDatabase.getInstance().getReference();
        studySpace = ref.child("study_spaces").child(locationKey);
        ratingList = studySpace.child("rating_list");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://studentstudyspaces.appspot.com");
        auth = FirebaseAuth.getInstance();
    }

    public void viewsInit() {
        ratingb = (RatingBar) findViewById(R.id.ratingBar);
        bsubmit = (Button) findViewById(R.id.buttonsbm);
        trating = (TextView) findViewById(R.id.textv);
        textViewLocationTitle = (TextView) findViewById(R.id.textViewLocationTitle);
        ImageView imageViewCurrentPhoto1 = (ImageView) findViewById(R.id.imageViewCurrentPhoto1);
        ImageView imageViewCurrentPhoto2 = (ImageView) findViewById(R.id.imageViewCurrentPhoto2);
        imageViews = new ImageView[] {imageViewCurrentPhoto1, imageViewCurrentPhoto2};
        textViewAverageRating = (TextView) findViewById(R.id.textViewAverageRating);
        progressBarUpload = (ProgressBar) findViewById(R.id.progressBarUpload);
    }
}