package com.example.android.travelmatics;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by DAKONY on 7/31/2019.
 */

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mFirebaseAuth;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static ArrayList<TravelDeal>mDeals;
    private static final int RC_SIGN_IN = 123;
    private static UserActivity caller;
    private FirebaseUtil(){}
    public static boolean isAdmin;

    public static void openFbReference(String ref, final UserActivity callerActivity){
        if(firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;

            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() == null){
                        FirebaseUtil.signIn();
                    }else {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome Back", Toast.LENGTH_SHORT).show();
                }
            };
            connectStorage();

        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin=false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("admin")
                                .child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FirebaseUtil.isAdmin = true;
               caller.showMenu();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    private static void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());


// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    public static void attachListeer(){
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }
    public static void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }
    public static void connectStorage(){
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }
}
