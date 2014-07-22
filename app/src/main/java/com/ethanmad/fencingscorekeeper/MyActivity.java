package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MyActivity extends Activity {
    long time, originalTime;
    int scoreOne;
    int scoreTwo;
    TextView timer, scoreOneView, scoreTwoView;
    boolean timerRunning;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    Uri alert;
    Ringtone ringer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        timer = (TextView) findViewById(R.id.timer);
        scoreOneView = (TextView) findViewById(R.id.scoreOne);
        scoreTwoView = (TextView) findViewById(R.id.scoreTwo);


        if (savedInstanceState != null && savedInstanceState.containsKey("time")) { //retrieve previous data
            time = savedInstanceState.getLong("time");
            originalTime = savedInstanceState.getLong("originalTime");
            scoreOne = savedInstanceState.getInt("scoreOne");
            scoreTwo = savedInstanceState.getInt("scoreTwo");
            timerRunning = savedInstanceState.getBoolean("timerRunning");
        } else { //create new data
            time = originalTime =  10 * 1000;
            scoreOne = scoreTwo = 0;
            timerRunning = false;
        }

        refreshScores(); //update scoreViews

        //used to signal to user that time has expired
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // I can't see this ever being null (as always have a default notification)
            // but just in case
            if (alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        ringer = RingtoneManager.getRingtone(getApplicationContext(), alert);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("time", time);
        savedInstanceState.putLong("originalTime", originalTime);
        savedInstanceState.putInt("scoreOne", scoreOne);
        savedInstanceState.putInt("scoreTwo", scoreTwo);
        savedInstanceState.putBoolean("timerRunning", timerRunning);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void countDown(View v) { //onClick method for timer
        ringer.stop();
        vibrator.cancel();

        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
        } else {
            countDownTimer = new CountDownTimer(time, 1) {
                public void onTick(long millisUntilFinished) {
                    String timeStr = String.format("%02d:%02d.%02d", millisUntilFinished / 60000, millisUntilFinished / 1000, millisUntilFinished % 1000 / 10);
                    timer.setText(timeStr);
                    time = millisUntilFinished;
                }

                public void onFinish() {
                    timer.setText("Time Up!");
                    vibrator.vibrate(5000);
                    ringer.play();
                    time = originalTime;

                }
            }.start();
            timerRunning = true;
        }
    }
    //methods to deal with scores
    public void refreshScores() {
        scoreOneView.setText("" + scoreOne);
        scoreTwoView.setText("" + scoreTwo);
    }
    public void addScoreOne(View view) { //onClick for scoreOne
        scoreOne++;
        refreshScores();
    }
    public void subScoreOne(View view) {
        scoreOne--;
        refreshScores();
    }
    public void addScoreTwo(View view) { //onClick for scoreTwo
        scoreTwo++;
        refreshScores();
    }
    public void subScoreTwo(View view) {
        scoreTwo--;
        refreshScores();
    }

}
