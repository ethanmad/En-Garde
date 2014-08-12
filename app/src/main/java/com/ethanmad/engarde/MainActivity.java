package com.ethanmad.engarde;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;

public class MainActivity extends Activity implements CardAlertFragment.CardAlertListener {
    long timeRemaining, periodLength, breakLength;
    long[] startVibrationPattern, endVibrationPattern;
    int scoreOne, scoreTwo, periodNumber, mode;
    TextView timer, scoreOneView, scoreTwoView, periodView;
    ImageView yellowIndicatorLeft, redIndicatorLeft, yellowIndicatorRight, redIndicatorRight;
    boolean timerRunning, inPeriod, inBreak, oneHasYellow, oneHasRed, twoHasYellow, twoHasRed;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    Uri alert;
    Ringtone ringer;
    Animation blink;
    ArrayDeque<Integer> recentActions;
    int[] recentActionsArray;
    MenuItem actionUndo;
    Toast toast;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        timer = (TextView) findViewById(R.id.timer);
        scoreOneView = (TextView) findViewById(R.id.scoreOne);
        scoreTwoView = (TextView) findViewById(R.id.scoreTwo);
        periodView = (TextView) findViewById(R.id.periodView);
        yellowIndicatorLeft = (ImageView) findViewById(R.id.yellowCircleViewOne);
        redIndicatorLeft = (ImageView) findViewById(R.id.redCircleViewOne);
        yellowIndicatorRight = (ImageView) findViewById(R.id.yellowCircleViewTwo);
        redIndicatorRight = (ImageView) findViewById(R.id.redCircleViewTwo);

        // import previous data if it exists, otherwise use default values on right
        if (savedInstanceState == null) savedInstanceState = new Bundle();
        periodLength = savedInstanceState.getLong("periodLength", 3 * 60 * 1000);
        timeRemaining = savedInstanceState.getLong("timeRemaining", periodLength);
        scoreOne = savedInstanceState.getInt("scoreOne", 0);
        scoreTwo = savedInstanceState.getInt("scoreTwo", 0);
        timerRunning = savedInstanceState.getBoolean("timerRunning", false);
        periodNumber = savedInstanceState.getInt("periodNumber", 1);
        breakLength = savedInstanceState.getLong("breakLength", 1 * 60 * 1000);
        mode = savedInstanceState.getInt("mode", 5);
        inPeriod = savedInstanceState.getBoolean("inPeriod", true);
        inBreak = savedInstanceState.getBoolean("inBreak", false);
        oneHasYellow = savedInstanceState.getBoolean("oneHasYellow", false);
        oneHasRed = savedInstanceState.getBoolean("oneHasRed", false);
        twoHasYellow = savedInstanceState.getBoolean("twoHasYellow", false);
        twoHasRed = savedInstanceState.getBoolean("twoHasRed", false);
        recentActionsArray = savedInstanceState.getIntArray("recentActionsArray");

        updateViews(); // update all views from default strings to real data
        loadSettings(); // load user settings

        if (recentActionsArray == null) recentActions = new ArrayDeque<Integer>(0);
        else for (int action : recentActionsArray)
            recentActions.push(action);

        // set-up blinking animation used when timer is paused TODO: make animation better (no fade)
        blink = new AlphaAnimation(0.0f, 1.0f);
        blink.setDuration(1000);
        blink.setStartOffset(0);
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.START_ON_FIRST_FRAME);

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
    protected void onResume() {
        super.onResume();
        if (recentActionsArray == null) {
            recentActions = new ArrayDeque<Integer>(0);
        } else for (int action : recentActionsArray) {
            recentActions.push(action);
        }

        loadSettings();
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
        savedInstanceState.putBoolean("oneHasYellow", oneHasYellow);
        savedInstanceState.putBoolean("oneHasRed", oneHasRed);
        savedInstanceState.putBoolean("twoHasYellow", twoHasYellow);
        savedInstanceState.putBoolean("twoHasRed", twoHasRed);
        recentActionsArray = new int[recentActions.size()];
        for (int i = recentActions.size() - 1; i >= 0; i--)
            recentActionsArray[i] = recentActions.pop();
        savedInstanceState.putIntArray("recentActionsArray", recentActionsArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items, menu);
        actionUndo = menu.findItem(R.id.action_undo);
        updateUndoButton();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    // METHODS FOR ALL TYPES
    private void updateViews() { // call all refresh methods (except updateTimer)
        updatePeriod();
        updateScores();
        updateCardIndicators();
        updateTimer(timeRemaining);
    }

    public void updateAll() {
        updateViews();
        updateUndoButton();
    }

    public void resetAll(MenuItem menuItem) { // onClick for action_reset
        resetScores();
        if (timeRemaining != periodLength) resetTime();
        resetCards();
        resetPeriod();
        resetRecentActions();
        updateAll();
        if(toast != null) toast.cancel();
    }


    // METHODS FOR TIME & PERIODS
    public void countDown(View v) { // onClick method for timer
        ringer.stop();
        vibrator.cancel();
        if (timerRunning) pauseTimer();
        else startTimer(timeRemaining);
    }

    private void updateTimer(long millisUntilFinished) {
        long minutes = millisUntilFinished / 60000;
        long seconds = millisUntilFinished / 1000 - minutes * 60;
        long milliseconds = millisUntilFinished % 1000 / 10;
        String timeStr = String.format("%1d:%02d.%02d", minutes,
                seconds, milliseconds);
        timer.setText(timeStr);
    }

    private void startTimer(long time) {
        timer.clearAnimation();
        timer.setTextColor(Color.WHITE);
        vibrator.vibrate(startVibrationPattern, -1);
        countDownTimer = new CountDownTimer(time, 10) {
            public void onTick(long millisUntilFinished) {
                updateTimer(millisUntilFinished);
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
        timer.setTextColor(Color.argb(180, 255, 20, 20));
        timer.setAnimation(blink);
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
        updatePeriod();
    }

    private void updatePeriod() {
        periodView.setText(getResources().getString(R.string.period) + " " + periodNumber);
    }

    private void resetPeriod() {
        periodNumber = 1;
    }

    private void resetTime() {
        timeRemaining = periodLength;
        timer.setText("" + timeRemaining);
        updateTimer(timeRemaining);
        timerRunning = false;
        ringer.stop();
        vibrator.cancel();
        timer.clearAnimation();
        periodNumber = 1;
    }

    // METHODS FOR SCORES
    private void updateScores() {
        scoreOneView.setText("" + scoreOne);
        scoreTwoView.setText("" + scoreTwo);
    }

    public void addScore(View view) { //onClick for score textViews
        switch (view.getId()) {
            case R.id.scoreOne:
                scoreOne++;
                recentActions.push(0);
                showToast(getResources().getString(R.string.gave), getResources().getString(R.string.touch),
                        getResources().getString(R.string.left));
                break;
            case R.id.scoreTwo:
                scoreTwo++;
                recentActions.push(1);
                showToast(getResources().getString(R.string.gave), getResources().getString(R.string.touch),
                        getResources().getString(R.string.right));
                break;
            case R.id.doubleTouchButton:
                scoreOne++;
                scoreTwo++;
                recentActions.push(2);
                showToast(getResources().getString(R.string.gave),getResources().getString(R.string.double_toast),
                        getResources().getString(R.string.touch));
                break;
        }
        pauseTimer();
        updateAll();
    }

    private void subScore(int fencer) {
        switch (fencer) {
            case 0:
                scoreOne--;
                break;
            case 1:
                scoreTwo--;
                break;
            case 2:
                scoreOne--;
                scoreTwo--;
                break;
        }
    }

    private void resetScores() {
        scoreOne = 0;
        if (timerRunning)
            countDownTimer.cancel();
        scoreTwo = 0;
        updateScores();
    }

    // METHODS FOR CARDS
    public void onDialogClick(DialogFragment dialogFragment, int fencer, int cardType) {
        giveCard(fencer, cardType);
    }

    public void giveCard(int fencer, int cardType) { // logic for assigning cards
        Intent cardIntent = new Intent(this, CardActivity.class);
        boolean alreadyHadYellow = false;
        switch (fencer) {
            case (0):
                switch (cardType) {
                    case (0):
                        if (oneHasYellow) {
                            alreadyHadYellow = true;
                            recentActions.push(4);
                        }
                        oneHasYellow = true;
                        if (!alreadyHadYellow) {
                            recentActions.push(3);
                            showToast(getResources().getString(R.string.gave), getResources().getString(R.string.yellow),
                                    getResources().getString(R.string.card), getResources().getString(R.string.left));
                            break;
                        }
                    case (1):
                        scoreTwo++;
                        oneHasRed = true;
                        recentActions.push(4);
                        showToast(getResources().getString(R.string.gave), getResources().getString(R.string.red),
                                getResources().getString(R.string.card), getResources().getString(R.string.left));
                        break;
                }
                if (oneHasRed) cardIntent.putExtra("red", true);
                startActivity(cardIntent);
                break;
            case (1):
                switch (cardType) {
                    case (0):
                        if (twoHasYellow) {
                            alreadyHadYellow = true;
                            recentActions.push(6);
                        }
                        twoHasYellow = true;
                        if (!alreadyHadYellow) {
                            recentActions.push(5);
                            showToast(getResources().getString(R.string.gave), getResources().getString(R.string.yellow),
                                    getResources().getString(R.string.card), getResources().getString(R.string.right));
                            break;
                        }
                    case (1):
                        scoreOne++;
                        twoHasRed = true;
                        recentActions.push(6);
                        showToast(getResources().getString(R.string.gave), getResources().getString(R.string.red),
                                getResources().getString(R.string.card), getResources().getString(R.string.right));
                        break;
                }
                if (twoHasRed) cardIntent.putExtra("red", true);
                startActivity(cardIntent); // launch card activity
        }
        updateAll();
        pauseTimer();
    }

    private void resetCards() { // remove all penalties and clear indicator views
        oneHasYellow = oneHasRed = twoHasRed = twoHasYellow = false;
        updateCardIndicators();
    }

    private void updateCardIndicators() { // update penalty indicator views
        if (oneHasYellow) yellowIndicatorLeft.setVisibility(View.VISIBLE);
        else yellowIndicatorLeft.setVisibility(View.INVISIBLE);
        if (oneHasRed) redIndicatorLeft.setVisibility(View.VISIBLE);
        else redIndicatorLeft.setVisibility(View.INVISIBLE);
        if (twoHasYellow) yellowIndicatorRight.setVisibility(View.VISIBLE);
        else yellowIndicatorRight.setVisibility(View.INVISIBLE);
        if (twoHasRed) redIndicatorRight.setVisibility(View.VISIBLE);
        else redIndicatorRight.setVisibility(View.INVISIBLE);
    }

    public void showCardDialog(View view) { // onClick for yellowCardButton & redCardButton
        FragmentManager man = this.getFragmentManager();
        CardAlertFragment dialog = new CardAlertFragment(view);
        dialog.show(man, "Penalty Card");
    }

    // METHODS FOR RECENT ACTIONS & UNDO
    // 0 = touch left, 1 = touch right, 2 = double touch,
    // 3 = yellow to left, 4 = red to left, 5 = yellow to right, 6 = red to right
    private void resetRecentActions() {
        recentActions = new ArrayDeque<Integer>(0);
    }

    private void undoAction(Integer action) {
        switch (action) {
            case 0:
                subScore(0);
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.touch),
                        getResources().getString(R.string.left));
                break;
            case 1:
                subScore(1);
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.touch),
                        getResources().getString(R.string.right));
                break;
            case 2:
                subScore(2);
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.double_toast),
                        getResources().getString(R.string.touch));
                break;
            case 3:
                oneHasYellow = false;
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.yellow),
                        getResources().getString(R.string.card), getResources().getString(R.string.left));
                break;
            case 4:
                oneHasRed = false;
                subScore(1);
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.red),
                        getResources().getString(R.string.card), getResources().getString(R.string.left));
                break;
            case 5:
                twoHasYellow = false;
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.yellow),
                        getResources().getString(R.string.card), getResources().getString(R.string.right));
                break;
            case 6:
                twoHasRed = false;
                subScore(0);
                showToast(getResources().getString(R.string.undid), getResources().getString(R.string.red),
                        getResources().getString(R.string.card), getResources().getString(R.string.right));
                break;
        }
        recentActions.pop();
        updateAll();
    }

    public void undoMostRecent(MenuItem item) {
        undoAction(recentActions.peek());
    }

    private void updateUndoButton() {
        if (recentActions.isEmpty()) actionUndo.setVisible(false);
        else actionUndo.setVisible(true);
    }

    public void openSettings(MenuItem item) {
        Intent settingsIntent = new Intent(this, Settings.class);
        startActivity(settingsIntent);
    }

    private void loadSettings() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mode = Integer.parseInt(sharedPreferences.getString("pref_mode", "5"));
    }

    private void showToast(String verb, String noun, String toWhom) {
        Context context = getApplicationContext();
        CharSequence text = verb + " " + noun + " " + toWhom + ".";
        int duration = Toast.LENGTH_SHORT;

        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void showToast(String verb, String noun, String color, String toWhom) {
        Context context = getApplicationContext();
        CharSequence text = verb + " " + " " + color + " " + noun + " " + toWhom + ".";
        int duration = Toast.LENGTH_SHORT;

        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}