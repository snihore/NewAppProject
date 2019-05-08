package com.example.resue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AlarmActivity extends AppCompatActivity {

    private TextView alarmCounterShow;
    private Button sosBtn;
    private ImageButton cancelSendSMSBtn;
    private RelativeLayout parentLayout;


    private SQLiteDatabase database;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        database = openOrCreateDatabase("Rescue", Context.MODE_PRIVATE, null);

        //start count
        timer.start();

        //cancel alarm and sending sms process
        cancelSendSMSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                alarmCounterShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                alarmCounterShow.setText("Canceled!");
            }
        });

        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToContacts();
                timer.start();
            }
        });
    }

    private void initializeViews(){
        alarmCounterShow = (TextView)findViewById(R.id.alarm_counter_show);
        sosBtn = (Button)findViewById(R.id.sos_btn);
        cancelSendSMSBtn = (ImageButton)findViewById(R.id.cancel_send_sms_btn);
        parentLayout = (RelativeLayout)findViewById(R.id.parent_layout);
    }

    CountDownTimer timer = new CountDownTimer(30000, 1000) {

        public void onTick(long millisUntilFinished) {
            long val = millisUntilFinished / 1000;
            if(val%2==0){
                parentLayout.setBackgroundColor(Color.RED);
            }else{
                parentLayout.setBackgroundColor(Color.GREEN);
            }
            alarmCounterShow.setText(""+val);
            //here you can have your logic to set text to edittext

        }

        public void onFinish() {
            alarmCounterShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            alarmCounterShow.setText("Sending ...");
            sendSmsToContacts();

            start();

        }

    };

    private ArrayList<String> fetchContactsFromDB(){

        database.execSQL("CREATE TABLE IF NOT EXISTS rescuecontacts(name varchar not null, mobile varchar not null);");
        Cursor cursor = database.rawQuery("SELECT mobile FROM rescuecontacts", null);
        ArrayList<String> mobiles = new ArrayList<>();
        int i = 0;
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                mobiles.add(cursor.getString(cursor.getColumnIndex("mobile")));
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        return mobiles;
    }

    private void sendSmsToContacts(){


        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            sendMessage();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 3786);
        }


    }

    private void sendMessage() {
        if(mAuth.getCurrentUser() != null){
            String message = "Sourabh needs a immediate help !";

            ArrayList<String> mobiles = fetchContactsFromDB();
            if(mobiles != null && mobiles.size()>0){
                for(String mobile: mobiles){
                    String temp = mobile.trim();
                    String finalMobile;
                    if(temp.contains("+91")){
                        String s = temp.substring(2);
                        finalMobile = s;
                    }else if(temp.contains("+91 ")){
                        String s =temp.split(" ")[1];
                        finalMobile = s;
                    }else{
                        String s = temp;
                        finalMobile = s;
                    }
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(finalMobile.trim(), null, message, null,null );

                }
                Toast.makeText(this, "Send!", Toast.LENGTH_SHORT).show();
                alarmCounterShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                alarmCounterShow.setText("Send!");
            }
        }else{
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 3786){
            if(grantResults.length>=0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                sendMessage();
            }else{
                Toast.makeText(this, "You Do'nt Have Required Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
