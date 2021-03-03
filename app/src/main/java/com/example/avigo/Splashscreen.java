package com.example.avigo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Transaction;


public class Splashscreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentFirebaseUser != null) {
                    startActivity(new Intent(Splashscreen.this,ContactActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(Splashscreen.this,RegistrationActivity2.class));
                    finish();
                }
            }
        },2500);


    }
}







