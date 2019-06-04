package com.example.myapplication;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class UserActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarTop);
        setSupportActionBar(toolbar);
        toolbar.setTitle("User: " + mUser.getEmail());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_user, menu);
        return super.onCreateOptionsMenu(menu);


    }
}
