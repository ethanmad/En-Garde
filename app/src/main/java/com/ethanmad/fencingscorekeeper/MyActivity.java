package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MyActivity extends Activity {
    long time;
    int scoreOne;
    int scoreTwo;
    TextView timer, scoreOneView, scoreTwoView;
    boolean timerRunning;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        timer = (TextView) findViewById(R.id.timer);
        scoreOneView = (TextView) findViewById(R.id.scoreOne);
        scoreTwoView = (TextView) findViewById(R.id.scoreTwo);

        if (savedInstanceState != null && savedInstanceState.containsKey("time")) {
            time = savedInstanceState.getInt("time");
            scoreOne = savedInstanceState.getInt("scoreOne");
            scoreTwo = savedInstanceState.getInt("scoreTwo");
            timerRunning = savedInstanceState.getBoolean("timerRunning");
        } else {
            time = 180 * 1000;
            scoreOne = scoreTwo = 0;
            timerRunning = false;
        }

        refreshScores();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("time", time);
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

    //methods to deal with timer
    //TODO: subtract from time and check if timer has been started previously
    public void countDown(View v) {
        if (timerRunning) {
            countDownTimer.cancel();
            //Log.v("Scorekeeper", "Paused");
            timerRunning = false;
        } else {
            countDownTimer = new CountDownTimer(time, 1000) {
                public void onTick(long millisUntilFinished) {
                    timer.setText("" + millisUntilFinished / 1000);
                    time = millisUntilFinished;
                }

                public void onFinish() {
                    timer.setText("done!");
                }
            }.start();
            //Log.v("Scorekeeper", "Started");
            timerRunning = true;
        }
    }

    //methods to deal with scores
    public void refreshScores() {
        scoreOneView.setText("" + scoreOne);
        scoreTwoView.setText("" + scoreTwo);
    }
    public void addScoreOne(View view) {
        scoreOne++;
        refreshScores();
    }
    public void subScoreOne(View view) {
        scoreOne--;
        refreshScores();
    }
    public void addScoreTwo(View view) {
        scoreTwo++;
        refreshScores();
    }
    public void subScoreTwo(View view) {
        scoreTwo--;
        refreshScores();
    }

}
