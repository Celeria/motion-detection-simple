package edu.vanderbilttuesdaythree.motiondetection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

public class Record extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    float x = 0;
    float y = 0;
    float z = 0;

    boolean keepRunning = true;

    TextView info;
    Button stopRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Context mContext = getApplicationContext();
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock =  powerManager.newWakeLock(PARTIAL_WAKE_LOCK,"motionDetection:keepAwake");
        wakeLock.acquire();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        info = findViewById(R.id.txtRecordInfo);
        stopRecord = findViewById(R.id.btnStopRecording);

        stopRecord.setEnabled(true);

        final SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int samplingRate = settingsData.getInt("samplingRate",100);
        final int xPrecision = settingsData.getInt("xPrecision",10);
        final int yPrecision = settingsData.getInt("yPrecision",10);
        final int zPrecision = settingsData.getInt("zPrecision",10);

        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keepRunning = false;
                stopRecord.setText("COME BACK TO THIS PAGE TO RESTART RECORDING\nPRESS BACK BUTTON ON PHONE TO DO SO");
                wakeLock.release();
                stopRecord.setEnabled(false);
            }
        });

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double totalMovement = 0;
                double prevX = Round(x,xPrecision);
                double prevY = Round(y,yPrecision);
                double prevZ = Round(z,zPrecision);
                double prevAccel = Math.sqrt((prevX * prevX) + (prevY * prevY) + (prevZ * prevZ));
                long sleepTime = (long)(1000/(double)samplingRate);
                long firstTime = System.currentTimeMillis();
                while(keepRunning) {
                    double currentX = Round(x,xPrecision);
                    double currentY = Round(y,yPrecision);
                    double currentZ = Round(z,zPrecision);
                    double currentAccel = Math.sqrt((currentX * currentX) + (currentY * currentY) + (currentZ * currentZ));
                    totalMovement += Math.abs(currentAccel - prevAccel);
                    prevAccel = currentAccel;
                    long currentTime = System.currentTimeMillis();
                    String display = "Total Movement: " + Round((float) totalMovement,Math.max(zPrecision,Math.max(xPrecision,yPrecision))) + "\nTime Elapsed:\n" + Round((float)(currentTime-firstTime)/1000,1) + " seconds";
                    new update().execute(display);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    class update extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            info.setText(s);
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];
        x = ax;
        y = ay;
        z = az;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Removed this default code so that it still runs while screen is off
        //sensorManager.unregisterListener(this);
    }

    public double Round(float input,int scale) {
        if (scale > 30) {
            scale = 30;
        }
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}