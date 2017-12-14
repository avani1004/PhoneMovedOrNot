package com.example.android.assignment3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.FloatMath;
import android.util.Log;

import java.util.Date;

import static android.provider.ContactsContract.Intents.Insert.ACTION;


/**
 * Created by avaniarora on 12/3/17.
 */

public class MyService extends Service implements SensorEventListener {

    private Thread myThread;
    private MyServiceTask myTask;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mGravity;
    private boolean serviceBound;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private Date currDate;
    private Date prevDate;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

  //  private LocalBroadcastManager localBroadcastManager;
   // public static final String ACTION = "com.example.android.assignment3.MyService";


    private final IBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        currDate = new Date();
        prevDate = currDate;
        //localBroadcastManager = LocalBroadcastManager.getInstance(this);
       // first_accel_time=0;
        serviceBound = false;
        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.start();

    }


    @Override
    public IBinder onBind(Intent intent) {

        return myBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!myThread.isAlive()) {
            myThread.start();
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
             wakeLock= powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
            wakeLock.acquire();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
    wakeLock.release();
        myTask.stopProcessing();



    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            myTask.setFirst_accel_time(System.currentTimeMillis());

            //Log.d("my","m"+myTask.getFirst_accel_time());
            //myTask.setCheckIfMoved(true);
            //Date currentTime = Calendar.getInstance().getTime();
            mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            currDate = new Date();
            if(mAccel > 3){
                if (currDate.getTime() - prevDate.getTime()>=30000) {
                    myTask.setCheckIfMoved(true);
                    //mSensorManager.unregisterListener(this);
                    //Toast.makeText(this, "The acceleration values at \nDate: "+ currDate + "\nare: "+ Arrays.toString(mGravity),Toast.LENGTH_LONG).show();
                    prevDate = currDate;
                }
            }
           /* if(myTask.checkResult()){
                Log.d("ncjd","hjcbd");
                //notifyResult();
            }*/

        }


    }

    public void addResultCallback(MyServiceTask.ResultCallback resultCallback) {
        myTask.addResultCallback(resultCallback);
    }

    public void releaseResult(ServiceResult result) {
        myTask.releaseResult(result);
    }

    public void removeResultCallback(MyServiceTask.ResultCallback resultCallback) {
        myTask.removeResultCallback(resultCallback);
    }
    public void stopListener(){
        if(mSensor!=null){
        mSensorManager.unregisterListener(this);
    }}
    /*public LocalBroadcastManager notifyResult()
    {
        //return myTask.checkResult();
        Intent intent = new Intent(ACTION);
        intent.putExtra("moved",myTask.checkResult());
        localBroadcastManager.sendBroadcast(intent);
        return localBroadcastManager;
    }*/






}








