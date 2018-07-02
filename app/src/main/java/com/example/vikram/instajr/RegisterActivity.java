package com.example.vikram.instajr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmailText, regPasswordText, regConfirmPassText;
    private Button regBtn;
    private TextView regLoginText;
    private ProgressBar regProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        regEmailText = (EditText)findViewById(R.id.regEmail);
        regPasswordText = (EditText)findViewById(R.id.regPassword);
        regConfirmPassText = (EditText)findViewById(R.id.regConfirmPassword);
        regBtn = (Button) findViewById(R.id.regBtn);
        regLoginText = (TextView) findViewById(R.id.signUpText);
        regProgress = (ProgressBar)findViewById(R.id.regProgress);

        Drawable progressDrawable = regProgress.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        regProgress.setProgressDrawable(progressDrawable);


        regLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });



        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = regEmailText.getText().toString().trim();
                String pass = regPasswordText.getText().toString().trim();
                String confirmPass = regConfirmPassText.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){

                    if (pass.equals(confirmPass)){

                        regProgress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    startActivity(new Intent(RegisterActivity.this,AccountSetupActivity.class));
                                    finish();

                                }else {

                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + errorMsg, Toast.LENGTH_LONG).show();


                                }

                                regProgress.setVisibility(View.INVISIBLE);

                            }
                        });

                    }else {
                        Toast.makeText(RegisterActivity.this, "Passwords Doesn't Match", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
            finish();
        }
    }
}
