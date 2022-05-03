package com.gorets.khub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    SharedPreferences storage;

    ProgressBar progressBar;
    com.google.android.material.floatingactionbutton.FloatingActionButton button;
    TextView textViewToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String data = loadData();
        if (data != null){

            textViewToken = findViewById(R.id.textViewToken);
            progressBar = findViewById(R.id.progressBar);
            String OTP = TOTP.getOTP(data);
            textViewToken.setText(OTP);


            (new Thread(new Runnable() {
                String time = String.valueOf(new Date().getTime() / 1000 / 30);
                @Override
                public void run() {
                    while (!Thread.interrupted())
                        try {
                            Thread.sleep(500);
                            runOnUiThread(new Runnable(){

                                @Override
                                public void run() {

                                    String tmpTime = String.valueOf(new Date().getTime() / 1000 / 30);
                                    if (!tmpTime.equals(time)){
                                        time = tmpTime;
                                        String OTP = TOTP.getOTP(data);
                                        textViewToken.setText(OTP);
                                    }

                                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("ss");
                                    int sec = Integer.parseInt(formatForDateNow.format(new Date())) ;
                                    if (sec > 30) {
                                        progressBar.setProgress(sec - 30);
                                    }else{
                                        progressBar.setProgress(sec);
                                    }



                                }
                            });
                        }
                        catch (InterruptedException e) {
                            // ooops
                        }
                }
            })).start();


            Toast.makeText(MainActivity.this, "Done!", Toast.LENGTH_SHORT).show();
        }

    }

    public void Scan(View v){
        Intent instant = new Intent(this, ScanCode.class);
        startActivity(instant);
    }

    private String loadData() {
        storage = getSharedPreferences("MyPref", MODE_PRIVATE);
        String savedText = storage.getString("SECRET", "");
        return savedText;
    }
}