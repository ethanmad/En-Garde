package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class MainActivity extends Activity implements CardAlertFragment.CardAlertListener {
    long timeRemaining, periodLength, breakLength;
    long[] startVibrationPattern, endVibrationPattern;
    int scoreOne, scoreTwo, periodNumber, mode;
    TextView timer, scoreOneView, scoreTwoView, periodView;
    boolean timerRunning, inPeriod, inBreak, oneHasYellow, oneHasRed, twoHasYellow, twoHasRed;
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
        periodView = (TextView) findViewById(R.id.periodView);

        if (savedInstanceState != null && savedInstanceState.containsKey("timeRemaining")) { // retrieve previous data
            timeRemaining = savedInstanceState.getLong("timeRemaining");
            periodLength = savedInstanceState.getLong("periodLength");
            scoreOne = savedInstanceState.getInt("scoreOne");
            scoreTwo = savedInstanceState.getInt("scoreTwo");
            timerRunning = savedInstanceState.getBoolean("timerRunning");
            periodNumber = savedInstanceState.getInt("periodNumber");
            breakLength = savedInstanceState.getLong("breakLength");
            mode = savedInstanceState.getInt("mode");
            inPeriod = savedInstanceState.getBoolean("inPeriod");
            inBreak = savedInstanceState.getBoolean("inBreak");

        } else { //create new data
            timeRemaining = periodLength = 3 * 3 * 1000;
            scoreOne = scoreTwo = 0;
            timerRunning = false;
            periodNumber = 1; refreshPeriod();
            breakLength = 1 * 10 * 1000;
            mode = 1;
            inPeriod = true;
            inBreak = false;
            oneHasYellow = oneHasRed = twoHasRed = twoHasYellow = false;
        }

        refreshScores(); // update scoreViews

        // set-up blinking animation used when timer is paused
        blink = new AlphaAnimation(0.0f, 1.0f);
        blink.setDuration(1000);
        blink.setStartOffset(0);
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.ABSOLUTE);

        // used to signal to user that timeRemaining has expired
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startVibrationPattern = new long[]{0, 50, 100, 50};
        endVibrationPattern = new long[]{0, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50/**/};
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
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
        savedInstanceState.putLong("timeRemaining", timeRemaining);
        savedInstanceState.putLong("periodLength", periodLength);
        savedInstanceState.putInt("scoreOne", scoreOne);
        savedInstanceState.putInt("scoreTwo", scoreTwo);
        savedInstanceState.putBoolean("timerRunning", timerRunning);
        savedInstanceState.putInt("periodNumber", periodNumber);
        savedInstanceState.putLong("breakLength", breakLength);
        savedInstanceState.putInt("mode", mode);
        savedInstanceState.putBoolean("inPeriod", inPeriod);
        savedInstanceState.putBoolean("inBreak", inBreak);
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
        if (timerRunning) pauseTimer();
        else startTimer(timeRemaining);
    }

    private void refreshTimer(long millisUntilFinished) {
        long minutes = millisUntilFinished / 60000;
        long seconds = millisUntilFinished / 1000 - minutes * 60;
        long milliseconds = millisUntilFinished % 1000 / 10;
        String timeStr = String.format("%1d:%02d.%02d", minutes,
                seconds, milliseconds);
        timer.setText(timeStr);
    }

    private void startTimer(long time) {
        timer.clearAnimation();
        vibrator.vibrate(startVibrationPattern, -1);
        countDownTimer = new CountDownTimer(time, 10) {
            public void onTick(long millisUntilFinished) {
                refreshTimer(millisUntilFinished);
                timeRemaining = millisUntilFinished;
            }

            public void onFinish() {
                endPeriod();
            }
        }.start();
        timerRunning = true;
    }

    private void pauseTimer() {
        ringer.stop();
        vibrator.cancel();
        vibrator.vibrate(100);
        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
            timer.startAnimation(blink);
        }
    }

    private void endPeriod() {
        timer.setText("Done!");
        vibrator.vibrate(endVibrationPattern, -1);
        ringer.play();
        timerRunning = false;
        inPeriod = !inPeriod;
        inBreak = !inBreak;
        if (inPeriod) {
            timeRemaining = periodLength;
            nextPeriod();
        } else if (inBreak)
            timeRemaining = breakLength;
    }

    private void nextPeriod() {
        periodNumber++;
        refreshPeriod();
    }
    private void refreshPeriod() {
        periodView.setText(getResources().getString(R.string.period) + " " + periodNumber);
    }

    private void resetTime() {
        timeRemaining = periodLength;
        timer.setText("" + timeRemaining);
        refreshTimer(timeRemaining);
        timerRunning = false;
        ringer.stop();
        vibrator.cancel();
        timer.clearAnimation();
        periodNumber = 1;
    }

    // methods for scores
    private void refreshScores() {
        scoreOneView.setText("" + scoreOne);
        scoreTwoView.setText("" + scoreTwo);
    }

    public void addScore(View view) { //onClick for score textViews
        switch (view.getId()) {
            case R.id.scoreOne:
                scoreOne++;
                break;
            case R.id.scoreTwo:
                scoreTwo++;
                break;
            case R.id.doubleTouchButton:
                scoreOne++;
                scoreTwo++;
                break;
        }
        pauseTimer();
        refreshScores();
    }

    public void subScore(View view) {
        switch (view.getId()) {
            case R.id.scoreOne:
                scoreOne++;
                break;
            case R.id.scoreTwo:
                scoreTwo++;
                break;
        }
    }

    private void resetScores() {
        scoreOne = 0;
        if (timerRunning)
            countDownTimer.cancel();
        scoreTwo = 0;
        refreshScores();
    }

    public void resetAll(MenuItem menuItem) { // onClick for action_reset
        resetScores();
        if (timeRemaining != periodLength)
            resetTime();
        resetCards();

    }

    // methods for cards
    public void onDialogClick(DialogFragment dialogFragment, int fencer, int cardType) {
        giveCard(fencer, cardType);
    }

    public void giveCard(int fencer, int cardType) {
        Intent intent = new Intent(this, CardActivty.class);
        boolean alreadyHadYellow = false;
        switch (fencer) {
            case (0):
                switch (cardType) {
                    case (0):
                        if (oneHasYellow)
                            alreadyHadYellow = true;
                        oneHasYellow = true;
                        if (!alreadyHadYellow)
                            break;
                    case (1):
                        scoreTwo++;
                        oneHasRed = true;
                        break;
                }
                if (oneHasRed) intent.putExtra("red", true);
                startActivity(intent);
                break;
            case (1):
                switch (cardType) {
                    case (0):
                        if(twoHasYellow)
                            alreadyHadYellow = true;
                        twoHasYellow = true;
                        if(!alreadyHadYellow)
                            break;
                    case (1):
                        scoreOne++;
                        twoHasRed = true;
                        break;
                }
                if (twoHasRed) intent.putExtra("red", true);
                startActivity(intent);
        }
        refreshScores();
        pauseTimer();
    }

    private void resetCards() {
        oneHasYellow = oneHasRed = twoHasRed = twoHasYellow = false;
    }

    public void showCardDialog(View view) { // onClick for yellowCardButton & redCardButton
        FragmentManager man = this.getFragmentManager();
        CardAlertFragment dialog = new CardAlertFragment(view);
        dialog.show(man, "Penalty Card");
    }

}