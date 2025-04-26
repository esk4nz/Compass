package com.example.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassImage;
    private TextView headingText;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImage = findViewById(R.id.compass_image);
        headingText = findViewById(R.id.heading);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values.clone();
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values.clone();

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDeg = (float) Math.toDegrees(azimuthInRadians);
                azimuthInDeg = (azimuthInDeg + 360) % 360;

                int roundedAzimuth = Math.round(azimuthInDeg);
                headingText.setText(String.format("%d° %s", roundedAzimuth, getDirection(azimuthInDeg)));


                RotateAnimation ra = new RotateAnimation(
                        currentDegree,
                        -azimuthInDeg,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                ra.setDuration(250);
                ra.setFillAfter(true);
                compassImage.startAnimation(ra);
                currentDegree = -azimuthInDeg;
            }
        }
    }

    private String getDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) return "N";
        else if (degree < 67.5) return "NE";
        else if (degree < 112.5) return "E";
        else if (degree < 157.5) return "SE";
        else if (degree < 202.5) return "S";
        else if (degree < 247.5) return "SW";
        else if (degree < 292.5) return "W";
        else return "NW";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

