package com.example.vikram.instajr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSetupActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar accToolbar;
    private Uri mainImmageUri = null;

    private CircleImageView profileImage;
    private EditText setupName;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private String userId;
    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        accToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.accSetup_toolbar);
        setSupportActionBar(accToolbar);
        getSupportActionBar().setTitle("Setup Your Account");

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profilePhotoSetup);
        setupName = (EditText)findViewById(R.id.setupName);
        setupBtn = (Button)findViewById(R.id.setupBtn);
        setupProgress = findViewById(R.id.setupPageProgress);

        Drawable progressDrawable = setupProgress.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        setupProgress.setProgressDrawable(progressDrawable);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImmageUri = Uri.parse(image);

                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.profile);

                        Glide.with(AccountSetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);

                    }

                }else {

                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this, "FireStore Retrieve Error : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = setupName.getText().toString().trim();

                if (!userName.isEmpty() && mainImmageUri != null) {

                setupProgress.setVisibility(View.VISIBLE);

                if (isChanged) {


                        userId = firebaseAuth.getCurrentUser().getUid();


                    File newImageFile = new File(mainImmageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(AccountSetupActivity.this)
                                .setMaxHeight(100)
                                .setMaxWidth(100)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    }catch (IOException e){
                        e.printStackTrace();
                    }


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumbData = baos.toByteArray();


                        UploadTask imagePath = storageReference.child("Profile Images").child(userId + ".jpg").putBytes(thumbData);

                        imagePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    StoreFirestore(task, userName);

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(AccountSetupActivity.this, "Image Error : " + error, Toast.LENGTH_LONG).show();
                                    setupProgress.setVisibility(View.INVISIBLE);

                                }


                            }
                        });

                    }else {

                    StoreFirestore(null, userName);

                }

                }else {

                    Toast.makeText(AccountSetupActivity.this,"Please fill all the attributes and add an image", Toast.LENGTH_LONG).show();

                }

            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(AccountSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(AccountSetupActivity.this, "Allow The Permission",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountSetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                    }else {

                        ImageLoader();
                    }

                }else {

                    ImageLoader();

                }

            }
        });

    }

    private void StoreFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String userName) {

        Uri download_url;

        if (task != null) {

            download_url = task.getResult().getDownloadUrl();

        }else {

            download_url = mainImmageUri;

        }


        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image",download_url.toString());

        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Toast.makeText(AccountSetupActivity.this, "Settings Saved", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(AccountSetupActivity.this, MainActivity.class));
                    finish();


                }else {

                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);

            }
        });

    }

    private void ImageLoader() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImmageUri = result.getUri();
                profileImage.setImageURI(mainImmageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
