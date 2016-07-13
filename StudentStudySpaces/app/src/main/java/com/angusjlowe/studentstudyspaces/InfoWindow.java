package com.angusjlowe.studentstudyspaces;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private UploadTask uploadTask;
    private StorageReference storageReference;
    private StorageReference studySpacePhotoRef;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ImageView imageView;
    private String[] urlsArray;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView[] imageViewsArray;

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
        studySpacePhotoRef = storageReference.child("images/").child(locationKey).child(user.getEmail());
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = studySpacePhotoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri url = taskSnapshot.getDownloadUrl();
                if(url != null) {
                    String urlString = url.toString();
                    imageUrls += urlString + ", ";
                }
                studySpace.child("image").setValue(imageUrls);
                Toast.makeText(InfoWindow.this, "Photo Uploaded Successfully",Toast.LENGTH_SHORT).show();
            }
        });

    }


    //change this
    public void placeCurrentPhotos(String imageUrls) throws IOException {
        if(!(imageUrls.equals(""))) {
            //remove duplicates
            imageUrls.replaceAll(" ", "");
            String[] imageUrlsArray = imageUrls.split(",");
            ArrayList<String> imagesUrlsList = new ArrayList<String>(Arrays.asList(imageUrlsArray));
            ArrayList<String> temp = new ArrayList<>(imagesUrlsList);
            ArrayList<String> urls = new ArrayList<>();
            for(String s : imagesUrlsList) {
                temp.remove(s);
                if(!temp.contains(s)) {
                    urls.add(s);
                }
            }
            //for the extra info to be passed on to the photos activity
            urlsArray = urls.toArray(new String[urls.size()]);
            //for each url, download it
            int a = 0;
            for(int i = 0; i < urlsArray.length; i++) {
                URL url = new URL(urlsArray[i]);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                if(imageViewsArray[a]!=null) {
                    imageViewsArray[a].setImageBitmap(bmp);
                }
                a++;
            }

        }

    }

    public void goToPhotos(View v) {
        Intent intent = new Intent(this, Photos.class);
        intent.putExtra("urls", urlsArray);
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
        user = auth.getCurrentUser();
        ratingb = (RatingBar) findViewById(R.id.ratingBar);
        bsubmit = (Button) findViewById(R.id.buttonsbm);
        trating = (TextView) findViewById(R.id.textv);
        textViewLocationTitle = (TextView) findViewById(R.id.textViewLocationTitle);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        imageView4 = (ImageView) findViewById(R.id.imageView4);
        imageViewsArray = new ImageView[] {imageView1, imageView2, imageView3, imageView4};
    }
}