package com.example.vikram.instajr;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPostFragment extends Fragment {

    private ImageView newPostImage;
    private EditText newPosteDesc;
    private Button newPostBtn;
    private ProgressBar newPostProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String currentUserId;

    private Bitmap compressedImageFile;

    private Uri postImageUri = null;

    private Context context;

    private HomeFragment homeFragment;
    private AddPostFragment addPostFragment;


    public AddPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        context = container.getContext();

        homeFragment = new HomeFragment();
        addPostFragment = new AddPostFragment();

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();


        newPostImage = view.findViewById(R.id.newPostImageViewFr);
        newPosteDesc = view.findViewById(R.id.newPostEditTextFr);
        newPostBtn = view.findViewById(R.id.newPostBtnFr);
        newPostProgress = view.findViewById(R.id.newPostProgressFr);

        Drawable progressDrawable = newPostProgress.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        newPostProgress.setProgressDrawable(progressDrawable);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setMinCropResultSize(512,512)
//                        .setAspectRatio(1,1)
//                        .start(getContext(), this);


                Intent intent = CropImage.activity(postImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .getIntent(getContext());
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

            }
        });



        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPosteDesc.getText().toString();

                if (!desc.isEmpty() && postImageUri != null){

                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();


                    // PHOTO UPLOAD
                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(context)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();


                    //PHOTO UPLOAD

                    UploadTask filePath = storageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
                    filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();


                            if (task.isSuccessful()){

                                File newThumbFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(context)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(1)
                                            .compressToBitmap(newThumbFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                //PHOTO UPLOAD

                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                        .child(randomName + ".jpg")
                                        .putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();


                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUri);
                                        postMap.put("imageThumb", downloadThumbUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", currentUserId);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()){

                                                    Toast.makeText(context, "Post Added", Toast.LENGTH_LONG).show();

                                                    Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_container);


                                                    replaceFrag(homeFragment,currentFragment);

                                                }else {



                                                }

                                                newPostProgress.setVisibility(View.INVISIBLE);

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //FAILURE HANDELING

                                    }
                                });


                            }else {

                                newPostProgress.setVisibility(View.INVISIBLE);

                            }

                        }
                    });

                }else {

                    Toast.makeText(context, "Please select a photo and add some Description", Toast.LENGTH_LONG).show();

                }

            }
        });


        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }



//    private void initializeFragment(){
//
//        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//
//        fragmentTransaction.add(R.id.main_container, homeFragment);
//        fragmentTransaction.add(R.id.main_container, notificationFragment);
//        fragmentTransaction.add(R.id.main_container, accountFragment);
//        fragmentTransaction.add(R.id.main_container, addPostFragment);
//
//        fragmentTransaction.hide(notificationFragment);
//        fragmentTransaction.hide(accountFragment);
//        fragmentTransaction.hide(addPostFragment);
//
//        fragmentTransaction.commit();
//
//    }




private void replaceFrag(Fragment fragment, Fragment currentFragment){

    android.support.v4.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.main_container, homeFragment);


    fragmentTransaction.hide(addPostFragment);

    fragmentTransaction.show(fragment);
}




}
