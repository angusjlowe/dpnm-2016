package com.angusjlowe.locationsexample;

import com.firebase.client.Firebase;

/**
 * Created by Angus on 2016-06-29.
 */
public class Context extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
