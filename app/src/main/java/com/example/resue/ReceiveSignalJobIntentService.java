package com.example.resue;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class ReceiveSignalJobIntentService extends JobIntentService {

    private static final String TAG = "ReceiveSignalJobIntentService";

    private BluetoothSocket bluetoothSocket;

    static final int STATE_MESSAGE_RECEIVED = 4;

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

    static void enqueueWork(Context context, Intent work){
        enqueueWork(context, ReceiveSignalJobIntentService.class, 123, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "onHandleWork");

        BluetoothSocketHelper helper = (BluetoothSocketHelper) intent.getSerializableExtra("inputSocket");
        bluetoothSocket = helper.getBluetoothSocket();

        if(bluetoothSocket != null){
            ReciveSignalThread reciveSignalThread = new ReciveSignalThread(bluetoothSocket);
            reciveSignalThread.start();
        }

        for (int i=0; i<100; i++){
            SystemClock.sleep(1000);

            if(i == 10){
                Intent alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
                startActivity(alarmIntent);
            }
        }
        if(isStopped()){
            return;
        }


    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        Log.i(TAG, "DESTROY");
        super.onDestroy();
    }

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
