package com.angusjlowe.firebasedemo;

import com.firebase.client.Firebase;

/**
 * Created by Angus on 2016-06-27.
 */
public class Coords extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
