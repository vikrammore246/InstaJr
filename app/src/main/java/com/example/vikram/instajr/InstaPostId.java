package com.example.vikram.instajr;

import com.google.firebase.firestore.Exclude;

import android.support.annotation.NonNull;

public class InstaPostId {

    @Exclude
    public String InstaPostId;

    public <T extends InstaPostId> T withId(@NonNull final String id){

        this.InstaPostId = id;
        return (T) this;

    }
}
