package com.example.resue;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class ContactsActivity extends AppCompatActivity {

    private BottomNavigationView contactBottomNavigationView;
    FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        contactBottomNavigationView = (BottomNavigationView)findViewById(R.id.contacts_bottom_navigationView);

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragement_container, new SavedContactFragement());
        transaction.addToBackStack(null);
        transaction.commit();

        contactBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()){
                    case R.id.saved_contacts_id:
                        fragment = new SavedContactFragement();
                        loadFragement(fragment);
                        return true;
                    case R.id.list_id:
                        fragment = new ContactListFragement();
                        loadFragement(fragment);
                        return true;
                }
                return false;
            }
        });

    }
    private void loadFragement(Fragment fragment){
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragement_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
