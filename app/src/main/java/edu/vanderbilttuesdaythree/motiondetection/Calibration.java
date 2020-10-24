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
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calibration extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    float x = 0;
    float y = 0;
    float z = 0;
    int counter = 0;

    int xPrecision;
    int yPrecision;
    int zPrecision;

    TextView txtInfo;
    TextView X;
    TextView Y;
    TextView Z;
    EditText pZ;
    EditText pX;
    EditText pY;
    Button btnCalibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        txtInfo = findViewById(R.id.txtInfo);
        X = findViewById(R.id.txtX);
        Y = findViewById(R.id.txtY);
        Z = findViewById(R.id.txtZ);
        pX = findViewById(R.id.pX);
        pY = findViewById(R.id.pY);
        pZ = findViewById(R.id.pZ);
        btnCalibrate = findViewById(R.id.btnUpdatePrecision);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xPrecision = settingsData.getInt("xPrecision",10);
        yPrecision = settingsData.getInt("yPrecision",10);
        zPrecision = settingsData.getInt("zPrecision",10);

        pX.setText(Integer.toString(xPrecision));
        pY.setText(Integer.toString(yPrecision));
        pZ.setText(Integer.toString(zPrecision));

        final int[] currentCount = new int[1];
        final int[] currentCount2 = new int[1];

        new Thread() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = settingsData.edit();
                editor.putBoolean("firstTime", false);
                currentCount[0] = counter;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentCount2[0] = counter;
                String display = "Phone stats:\nMax Sample Rate: " + (currentCount2[0] - currentCount[0]) + " Hz";
                new update().execute(display);
                editor.putInt("maxHertz", currentCount2[0] - currentCount[0]);
                editor.apply();
            }
        }.start();

        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //(!pX.getText().toString().equals("") && !pX.getText().toString().equals("") && !pX.getText().toString().equals(""))
                try{
                    xPrecision = Integer.parseInt(pX.getText().toString());
                    yPrecision = Integer.parseInt(pY.getText().toString());
                    zPrecision = Integer.parseInt(pZ.getText().toString());
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putInt("xPrecision",xPrecision);
                    editor.putInt("yPrecision",yPrecision);
                    editor.putInt("zPrecision",zPrecision);
                    editor.commit();
                } catch (Exception e) {
                    Toast fail = Toast.makeText(getApplicationContext(),"No blanks or numbers smaller than 1 allowed, please try again.",Toast.LENGTH_LONG);
                    fail.show();
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                boolean keepRunning = true;
                while(keepRunning) {
                    String display = "X Accel: " + Round(x, xPrecision);
                    new updateX().execute(display);
                    display = "Y Accel: " + Round(y, yPrecision);
                    new updateY().execute(display);
                    display = "Z Accel: " + Round(z, zPrecision);
                    new updateZ().execute(display);
                    try {
                        Thread.sleep(30);
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
            txtInfo.setText(s);
        }
    }

    class updateX extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            X.setText(s);
        }
    }

    class updateY extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Y.setText(s);
        }
    }

    class updateZ extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Z.setText(s);
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

        counter++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public double Round(float input,int scale) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}