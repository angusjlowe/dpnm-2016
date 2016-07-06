package com.angusjlowe.studentstudyspaces;



import com.firebase.client.Firebase;

/**
 * Created by Angus on 2016-07-01.
 */
public class Context extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
