package com.example.resue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
    }

    public void logoutUser(View view) {

        if(mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }
        Intent optionsActivityIntent = new Intent(getApplicationContext(), OptionsActivity.class);

        startActivity(optionsActivityIntent);

        finish();
    }
}
