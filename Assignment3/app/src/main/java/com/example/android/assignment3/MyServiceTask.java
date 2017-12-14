package com.example.android.assignment3;

import android.content.Context;
import android.content.Intent;
import android.service.carrier.CarrierMessagingService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

//import static com.example.android.assignment3.MyService.ACTION;


/**
 * Created by avaniarora on 12/3/17.
 */

public class MyServiceTask implements Runnable {
    private boolean running;
    private Context context;
    private boolean checkIfMoved;
    //private TextView textView;
    private Set<ResultCallback> resultCallbacks = Collections.synchronizedSet(
            new HashSet<ResultCallback>());
    private ConcurrentLinkedQueue<ServiceResult> freeResults =
            new ConcurrentLinkedQueue<ServiceResult>();

    public int value = 0;
    private boolean flag;
    private long first_accel_time;
    private MyService myService = new MyService();

    public void setFirst_accel_time(long first_accel_time) {
        this.first_accel_time = first_accel_time;
    }

    public long getFirst_accel_time() {
        return first_accel_time;
    }

    public boolean isCheckIfMoved() {
        return checkIfMoved;
    }

    public void setCheckIfMoved(boolean checkIfMoved) {
        this.checkIfMoved = checkIfMoved;
    }





    public MyServiceTask(Context _context) {
        context = _context;
        // Put here what to do at creation.
    }


    @Override
    public void run() {

        running = true;

        while (running) {

            try {

                Thread.sleep(3000);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            //Log.d("Run","BeforeMove");
            flag = didItMove();
            //Log.d("Run","AfterMove");
            notifyResultCallback(flag);
        }
    }

    public void stopProcessing() {
        running = false;
    }

    public boolean didItMove(){
       //long currentTime = System.currentTimeMillis();
        //Log.d("Checking","Moved");
        if(checkIfMoved){
            Log.d("Checking","Moved");
            setCheckIfMoved(false);
            //Log.d("Checking","1"+getFirst_accel_time());
            //Toast.makeText("Hi","Moved",Toast.LENGTH_SHORT).show();
           // notifyResult();
            return true;

        }
        //Log.d("Checking","Not Moved");
        return false;
    }

    public boolean checkResult(){
        return flag;
    }
    public void addResultCallback(ResultCallback resultCallback) {
        //Log.i(LOG_TAG, "Adding result callback");
        resultCallbacks.add(resultCallback);
    }

    public void releaseResult(ServiceResult r) {
        //Log.i(LOG_TAG, "Freeing result holder for " + r.intValue);
        freeResults.offer(r);
    }

    public void removeResultCallback(ResultCallback resultCallback) {
        //Log.i(LOG_TAG, "Removing result callback");
        // We remove the callback...
        resultCallbacks.remove(resultCallback);
        // ...and we clear the list of results.
        // Note that this works because, even though mResultCallbacks is a synchronized set,
        // its cardinality should always be 0 or 1 -- never more than that.
        // We have one viewer only.
        // We clear the buffer, because some result may never be returned to the
        // free buffer, so using a new set upon reattachment is important to avoid
        // leaks.
        freeResults.clear();
    }
    private void createResultsBuffer() {
        // I create some results to talk to the callback, so we can reuse these instead of creating new ones.
        // The list is synchronized, because integers are filled in the service thread,
        // and returned to the free pool from the UI thread.
        freeResults.clear();
        for (int i = 0; i < 10; i++) {
            freeResults.offer(new ServiceResult());
        }
    }

    private void notifyResultCallback(boolean move) {
        //Log.d("Insidenotifyresult",move + "");
        if (!resultCallbacks.isEmpty()) {
            // If we have no free result holders in the buffer, then we need to create them.
            if (freeResults.isEmpty()) {
                createResultsBuffer();
            }
            ServiceResult result = freeResults.poll();
            // If we got a null result, we have no more space in the buffer,
            // and we simply drop the integer, rather than sending it back.
            if (result != null) {
                result.booleanValue = move;
               // Log.d("Insidenotifyresult",move + "");

                for (ResultCallback resultCallback : resultCallbacks) {
                    //Log.i(LOG_TAG, "calling resultCallback for " + result.intValue);
                    resultCallback.onResultReady(result);
                }
            }
        }
        else {
            if(move == true){
                value = 1;

                Log.d("Notifyresult", "We need to somehow save this");

                ServiceResult result = new ServiceResult();
                Log.d("result", ""+result);
                // If we got a null result, we have no more space in the buffer,
                // and we simply drop the integer, rather than sending it back.
                result.booleanValue = move;
                    // Log.d("Insidenotifyresult",move + "");

                    for (ResultCallback resultCallback : resultCallbacks) {
                        //Log.i(LOG_TAG, "calling resultCallback for " + result.intValue);
                        resultCallback.onResultReady(result);
                    }
                }
            }



    }

    public interface ResultCallback {
        void onResultReady(ServiceResult result);
    }


}
