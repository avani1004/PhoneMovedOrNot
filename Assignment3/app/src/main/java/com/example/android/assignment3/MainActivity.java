package com.example.android.assignment3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import com.example.android.assignment3.MyServiceTask.ResultCallback;

public class MainActivity extends AppCompatActivity implements com.example.android.assignment3.MyServiceTask.ResultCallback {
    private static final String LOG_TAG = "MyService";
    public static final int DISPLAY_NUMBER = 10;

    private MyService myService;
    private Handler mUiHandler;
    //private Handler mUiHandler1;
    //private MyServiceTask myServiceTask;
    private boolean serviceBound;

    private Button clear;
    private Button exit;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceBound = false;
//        mUiHandler = new Handler(getMainLooper(), new UiCallback());


        tv = (TextView) findViewById(R.id.textView);
        tv.setText("Everything was quiet");
        //myServiceTask = new MyServiceTask(getApplicationContext());
        //mTV = (TextView) findViewById(R.id.textView);
        clear = (Button) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (true) {
                    Log.i(LOG_TAG, "Stopping.");
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    stopService(intent);
                    Log.i(LOG_TAG, "Stopped.");
                    mUiHandler = new Handler(getMainLooper(), new UiCallback());
                    tv.setText("Everything was quiet");
                    startService(intent);
                    bindMyService();

            }

        }});

          exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)

            {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(MainActivity.this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
//                finish();
//                moveTaskToBack(true);
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("checkingResume","Resume");
        mUiHandler = new Handler(getMainLooper(), new UiCallback());
        Intent intent = new Intent(this,MyService.class);
        //if()
        //IntentFilter intentFilter = new IntentFilter(MyService.ACTION);
        startService(intent);
        bindMyService();

        //LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, intentFilter);
       // mTV.setText(null);
        //Log.d ("Resume", "Please print");





        //Todo call service () did it move
       // mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void bindMyService() {
        Intent intent = new Intent(this, MyService.class);
        Log.d("inside","Before bindmyservice");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("inside","After bindmyservice");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            MyService.MyBinder binder = (MyService.MyBinder) serviceBinder;
            Log.d("inside","serviceConnection");
            myService = binder.getService();
            serviceBound = true;
            myService.addResultCallback(MainActivity.this);
            /*if(myService.notifyResult()){
                Log.d("xbhs","xbhj");
               mTV.setText("Hi");
            }
            else{
                mTV.setText("Bye");
            }*/

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    @Override
    protected void onPause() {
        if (serviceBound) {
            if (myService != null) {
                myService.removeResultCallback(this);
            }
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;

            // If we like, stops the service.
           /* if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }*/
        }
        super.onPause();
    }

    @Override
    public void onResultReady(ServiceResult result) {
        if (result != null) {
            //Log.i(LOG_TAG, "Preparing a message for " + result.booleanValue);
        } else {
           // Log.e(LOG_TAG, "Received an empty result!");
        }
        Log.d("MainActivity", "Trying to print in main activity the result "+ result.booleanValue);
        mUiHandler.obtainMessage(DISPLAY_NUMBER, result).sendToTarget();

        //mUiHandler1.obtainMessage(DISPLAY_NUMBER, result).sendToTarget();

    }



    private class UiCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
           // Log.d("inside UI Handler","Blah");
            if (message.what == DISPLAY_NUMBER) {
                Log.d("inside UI Handler","Blah");
                // Gets the result.
                ServiceResult result = (ServiceResult) message.obj;
                // Displays it.
                if (result != null) {
                    Log.i(LOG_TAG, "Displaying: " + result.booleanValue);

                    /*else{
                        mTV.setText("Everything was quiet");
                    }*/
                    //mTV.setText("Hi");

                    if(result.booleanValue){
                        tv.setText("The phone was moved");
                        //myService.stopListener();

                    }

                    /*else if(!result.booleanValue){

                    }*/

                    //mTv.setText(Integer.toString(result.booleanValue));
                    // Tell the worker that the bitmap is ready to be reused
                    if (serviceBound && myService != null) {
                      //  Log.i(LOG_TAG, "Releasing result holder for " + result.booleanValue);
                        myService.releaseResult(result);
                    }
                } else {
                    //Log.e(LOG_TAG, "Error: received empty message!");
                }
            }
            return true;
        }
    }
    /*public BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("moved");
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            Log.i("MainActivity","result: " + result);
            // mSensorManager.unregisterListener(this);


        }
    };
*/
   /* @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = sensorEvent.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];

    }

}
*/
}
