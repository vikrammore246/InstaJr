package com.example.vikram.instajr;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String currentUserId;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private SearchFragment searchFragment;

    private BottomNavigationView mainBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.appBarLayout);
        setSupportActionBar(mainToolbar);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mainBottomNav = findViewById(R.id.mainBottomNav);

            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            searchFragment = new SearchFragment();

            //replaceFragment(homeFragment);
            initializeFragment();


            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                    switch (item.getItemId()) {

                        case R.id.bottom_action_home:
                            replaceFragment(homeFragment, currentFragment);
                            return true;

                        case R.id.action_search_gray:
                            replaceFragment(searchFragment, currentFragment);
                            return true;

                        case R.id.bottom_action_notification:
                            replaceFragment(notificationFragment, currentFragment);
                            return true;


                        case R.id.bottom_action_account:
                            finish();
                            startActivity(new Intent(MainActivity.this, AccountSetupActivity.class));
                            //replaceFragment(accountFragment, currentFragment);
                            return true;

                        case R.id.bottom_action_add_post:
                            //replaceFragment(addPostFragment, currentFragment);
                            finish();
                            startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                            return true;

                        default:
                            return false;

                    }

                }
            });

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null){
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }else {

            currentUserId = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                        if (!task.getResult().exists()){

                            startActivity(new Intent(MainActivity.this, AccountSetupActivity.class));
                            finish();

                        }

                    }else {

                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();

                    }

                }
            });

        }
    }

    private void initializeFragment(){

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, notificationFragment);
        fragmentTransaction.add(R.id.main_container, searchFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(searchFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(searchFragment);

        }


        if(fragment == notificationFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(searchFragment);

        }

        if(fragment == searchFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

}
