package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class MyActivity extends Activity {
    long time, originalTime;
    int scoreOne, scoreTwo;
    TextView timer, scoreOneView, scoreTwoView;
    boolean timerRunning, oneHasYellow, oneHasRed, twoHasYellow, twoHasRed;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    Uri alert;
    Ringtone ringer;
    Animation blink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        timer = (TextView) findViewById(R.id.timer);
        scoreOneView = (TextView) findViewById(R.id.scoreOne);
        scoreTwoView = (TextView) findViewById(R.id.scoreTwo);


        if (savedInstanceState != null && savedInstanceState.containsKey("time")) { // retrieve previous data
            time = savedInstanceState.getLong("time");
            originalTime = savedInstanceState.getLong("originalTime");
            scoreOne = savedInstanceState.getInt("scoreOne");
            scoreTwo = savedInstanceState.getInt("scoreTwo");
            timerRunning = savedInstanceState.getBoolean("timerRunning");
        } else { //create new data
            time = originalTime =  3 * 60 * 1000;
            scoreOne = scoreTwo = 0;
            timerRunning = false;
        }

        refreshScores(); // update scoreViews

        // set-up blinking animation used when timer is paused
        blink = new AlphaAnimation(0.0f, 1.0f);
        blink.setDuration(1000);
        blink.setStartOffset(0);
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.ABSOLUTE);

        // used to signal to user that time has expired
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // just in case
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
        getMenuInflater().inflate(R.menu.items, menu);
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

    //methods for timer
    public void countDown(View v) { // onClick method for timer
        ringer.stop();
        vibrator.cancel();

        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
            timer.startAnimation(blink);
        } else {
            startTimer();
        }
    }
    private void refreshTimer(long millisUntilFinished) {
        long minutes = millisUntilFinished / 60000;
        long seconds = millisUntilFinished / 1000 - minutes * 60;
        long milliseconds = millisUntilFinished % 1000 / 10;
        String timeStr = String.format("%1d:%02d.%02d", minutes,
                seconds, milliseconds);
        timer.setText(timeStr);
    }
    private void startTimer() {
        timer.clearAnimation();
        countDownTimer = new CountDownTimer(time, 10) {
            public void onTick(long millisUntilFinished) {
                refreshTimer(millisUntilFinished);
                time = millisUntilFinished;
            }

            public void onFinish() {
                timer.setText("Done!");
                vibrator.vibrate(5000);
                ringer.play();
                time = originalTime;

            }
        }.start();
        timerRunning = true;
    }
    private void pauseTimer() {
        ringer.stop();
        vibrator.cancel();
        if(timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
            timer.startAnimation(blink);
        }
    }
    private void endTimer() {
        timer.setText("Done!");
        vibrator.vibrate(5000); //TODO: set vibrate pattern
    }
    private void resetTime() {
        time = originalTime;
        timer.setText("" + time);
        refreshTimer(time);
        Log.d("", "Time reset");
        timerRunning = false;
        ringer.stop();
        vibrator.cancel();
        timer.clearAnimation();
    }

    // methods for scores
    private void refreshScores() {
        scoreOneView.setText("" + scoreOne);
        scoreTwoView.setText("" + scoreTwo);
    }
    public void addScoreOne(View view) { // onClick for scoreOne
        pauseTimer();
        scoreOne++;
        refreshScores();
    }
    public void subScoreOne(View view) {
        pauseTimer();
        scoreOne--;
        refreshScores();
    }
    public void addScoreTwo(View view) { // onClick for scoreTwo
        scoreTwo++;
        refreshScores();
    }
    public void subScoreTwo(View view) {
        scoreTwo--;
        refreshScores();
    }
    public void addScoreBoth (View view) { // onClick for doubleTouchButton
        pauseTimer();
        scoreOne++;
        scoreTwo++;
        refreshScores();
    }
    private void resetScores() {
        scoreOne = 0;
        if(timerRunning)
            countDownTimer.cancel();
        scoreTwo = 0;
        refreshScores();
    }

    public void resetAll(MenuItem menuItem) { // onClick for action_reset
        resetScores();
        if(time != originalTime)
            resetTime();
        resetCards();
    }

    // methods for cards
    public void giveOneYellow(View view) {

    }
    public void giveOneRed(View view) {

    }
    public void giveTwoYellow(View view){

    }
    public void giveTwoRed(View view){

    }
    private void resetCards() {
       oneHasYellow = oneHasRed = twoHasRed = twoHasYellow = false;
    }

    public void showDialogYellowCard(View view) { // onClick for yellowCardButton
        FragmentManager man = this.getFragmentManager();
        YellowCardAlertFragment dialog = new YellowCardAlertFragment();
        dialog.show(man,"Yellow Card");
    }
    public void showDialogRedCard(View view) { // onClick for redCardButton
        FragmentManager man = this.getFragmentManager();
        RedCardAlertFragment dialog = new RedCardAlertFragment();
        dialog.show(man, "Red Card");
    }

}
