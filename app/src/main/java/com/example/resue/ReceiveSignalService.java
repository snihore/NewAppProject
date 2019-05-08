package com.example.resue;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

import static com.example.resue.App.CHANNEL_ID;


public class ReceiveSignalService extends Service {

    private BluetoothSocket bluetoothSocket;
    static final int STATE_MESSAGE_RECEIVED = 4;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final BluetoothSocket objReceived = (BluetoothSocket) ((ObjectWrapperForBinder)intent.getExtras().getBinder("inputSocket")).getData();
        bluetoothSocket = objReceived;
        Log.i("HELPER_2", bluetoothSocket.getRemoteDevice().getName());


        Intent notificationIntent = new Intent(this, AlarmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = null;

        if(bluetoothSocket != null){
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(bluetoothSocket.getRemoteDevice().getName())
                    .setContentText(bluetoothSocket.getRemoteDevice().getAddress())
                    .setSmallIcon(R.drawable.rescuelogo)
                    .setContentIntent(pendingIntent)
                    .build();
        }else{
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Rescue")
                    .setContentText("Device Not Connected")
                    .setSmallIcon(R.drawable.rescuelogo)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        if(notification != null){
            startForeground(1, notification);
        }

        //do heavy work on a background thread

        if(bluetoothSocket != null){
            ReciveSignalThread reciveSignalThread = new ReciveSignalThread(bluetoothSocket);
            reciveSignalThread.start();
        }

        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case STATE_MESSAGE_RECEIVED :

                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);

                    Log.i("RECEIVED SIGNAL", tempMsg);

                    Intent alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
                    startActivity(alarmIntent);
                    break;
            }
            return true;
        }
    });

    public class ReciveSignalThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;

        public ReciveSignalThread(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempIn = null;

            try{
                tempIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try{
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
