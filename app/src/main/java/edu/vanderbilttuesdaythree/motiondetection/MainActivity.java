package edu.vanderbilttuesdaythree.motiondetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    EditText setSampling;
    TextView viewSampling;
    Button calibrate;
    Button saveSampling;
    Button startRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Main Menu");

        setSampling = findViewById(R.id.samplingRate);
        calibrate = findViewById(R.id.btnRecalibrate);
        saveSampling = findViewById(R.id.btnSaveSampling);
        startRecord = findViewById(R.id.btnStartRecord);
        viewSampling = findViewById(R.id.txtSetSampling);

        final SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean firstTime = settingsData.getBoolean("firstTime",true);
        final int samplingRate = settingsData.getInt("samplingRate",50);
        int actualSamplingRate = (int)(1000 / (long)(1000/(double)samplingRate));
        String samplingText = "Set Sampling Rate: " + samplingRate + "\nActual Sampling Rate: " + actualSamplingRate
                + "\n\nSet new sampling rate below:";
        viewSampling.setText(samplingText);

        final Intent goCalibrate = new Intent(this,Calibration.class);
        if (firstTime) {
            startActivity(goCalibrate);
        }

        final int maxSample = settingsData.getInt("maxHertz",0);
        setSampling.setHint("Max HZ: " + maxSample);

        final Intent goRecord = new Intent(this,Record.class);
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(goRecord);
            }
        });

        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(goCalibrate);
            }
        });

        saveSampling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = settingsData.edit();
                if (!setSampling.getText().toString().equals("")){
                    if (Integer.parseInt(setSampling.getText().toString()) <= maxSample) {
                        editor.putInt("samplingRate", Integer.parseInt(setSampling.getText().toString()));
                        editor.apply();
                        int samplingRate2 = Integer.parseInt(setSampling.getText().toString());
                        int actualSamplingRate = (int)(1000 / (long)(1000/(double)samplingRate2));
                        String samplingText = "Set Sampling Rate: " + samplingRate2 + "\nActual Sampling Rate: " + actualSamplingRate
                                + "\n\nSet new sampling rate below:";
                        viewSampling.setText(samplingText);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int maxSample = settingsData.getInt("maxHertz",0);
        setSampling.setHint("Max HZ: " + maxSample);
    }
}
