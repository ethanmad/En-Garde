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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;

public class MainActivity extends Activity implements CardAlertFragment.CardAlertListener {
    int[] mRecentActionArray;
    private Fencer leftFencer, rightFencer;
    private long mTimeRemaining, mPeriodLength, mBreakLength, mPriorityLength;
    private long[] mStartVibrationPattern, mEndVibrationPattern, mTimesRemainingWhenPeriodSkippedArray;
    private int mPeriodNumber, mNextSectionType, mMode, mMaxPeriods;
    private TextView mTimer, mScoreLeftView, mRightScoreView, mPeriodView;
    private ImageView mYellowIndicatorLeft, mRedIndicatorLeft, mPriorityIndicatorLeft, mYellowIndicatorRight, mRedIndicatorRight, mPriorityIndicatorRight;
    private boolean mTimerRunning, mInPeriod, mInBreak, mInPriority, mShowDouble, mBlackBackground;
    private CountDownTimer mCountDownTimer;
    private Vibrator mVibrator;
    private Uri mAlert;
    private Ringtone mRinger;
    private Animation mBlink;
    private ArrayDeque<Integer> mRecentActions;
    private ArrayDeque<Long> mTimesRemainingWhenPeriodSkipped;
    private MenuItem mActionUndo;
    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    private RelativeLayout mMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        leftFencer = new Fencer();
        rightFencer = new Fencer();

        mTimer = (TextView) findViewById(R.id.timer);
        mScoreLeftView = (TextView) findViewById(R.id.scoreOne);
        mRightScoreView = (TextView) findViewById(R.id.scoreTwo);
        mPeriodView = (TextView) findViewById(R.id.periodView);
        mYellowIndicatorLeft = (ImageView) findViewById(R.id.yellowCircleViewOne);
        mRedIndicatorLeft = (ImageView) findViewById(R.id.redCircleViewOne);
        mPriorityIndicatorLeft = (ImageView) findViewById(R.id.priorityCircleViewLeft);
        mYellowIndicatorRight = (ImageView) findViewById(R.id.yellowCircleViewTwo);
        mRedIndicatorRight = (ImageView) findViewById(R.id.redCircleViewTwo);
        mPriorityIndicatorRight = (ImageView) findViewById(R.id.priorityCircleViewRight);
        mMainLayout = (RelativeLayout) findViewById(R.id.mainLayout);


        // import previous data if it exists, otherwise use default values on right
        if (savedInstanceState == null) savedInstanceState = new Bundle();
        mPeriodLength = savedInstanceState.getLong("mPeriodLength", 3 * 60 * 1000);
        mBreakLength = savedInstanceState.getLong("mBreakLength", 1 * 60 * 1000);
        mPriorityLength = savedInstanceState.getLong("mPriorityLength", 1 * 60 * 1000);
        mTimeRemaining = savedInstanceState.getLong("mTimeRemaining", mPeriodLength);
        mTimesRemainingWhenPeriodSkippedArray = savedInstanceState.getLongArray("mTimesRemainingWhenPeriodSkipped");
        mTimerRunning = savedInstanceState.getBoolean("mTimerRunning", false);
        mPeriodNumber = savedInstanceState.getInt("mPeriodNumber", 1);
        mMode = savedInstanceState.getInt("mMode", 5);
        mShowDouble = savedInstanceState.getBoolean("mShowDouble", true);
        mBlackBackground = savedInstanceState.getBoolean("mBlackBackground", false);
        mInPeriod = savedInstanceState.getBoolean("mInPeriod", true);
        mInBreak = savedInstanceState.getBoolean("mInBreak", false);
        mInPriority = savedInstanceState.getBoolean("mInPriority", false);
        mRecentActionArray = savedInstanceState.getIntArray("mRecentActionArray");

        updateViews(); // update all views from default strings to real data
        loadSettings(); // load user settings

        if (mRecentActionArray == null) mRecentActions = new ArrayDeque<Integer>(0);
        else for (int action : mRecentActionArray)
            mRecentActions.push(action);

        if(mTimesRemainingWhenPeriodSkippedArray == null) mTimesRemainingWhenPeriodSkipped = new ArrayDeque<Long>(0);
        else for (long time : mTimesRemainingWhenPeriodSkippedArray)
                mTimesRemainingWhenPeriodSkipped.push(time);

        // set-up blinking animation used when mTimer is paused TODO: make animation better (no fade)
        mBlink = new AlphaAnimation(0.0f, 1.0f);
        mBlink.setDuration(1000);
        mBlink.setStartOffset(0);
        mBlink.setRepeatCount(Animation.INFINITE);
        mBlink.setRepeatMode(Animation.START_ON_FIRST_FRAME);

        // used to signal to user that mTimeRemaining has expired
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mStartVibrationPattern = new long[]{0, 50, 100, 50};
        mEndVibrationPattern = new long[]{0, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50/**/};
        mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (mAlert == null) {
            // mAlert is null, using backup
            mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // just in case
            if (mAlert == null) {
                // mAlert backup is null, using 2nd backup
                mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        mRinger = RingtoneManager.getRingtone(getApplicationContext(), mAlert);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecentActionArray == null) {
            mRecentActions = new ArrayDeque<Integer>(0);
        } else for (int action : mRecentActionArray) {
            mRecentActions.push(action);
        }

        loadSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("mTimeRemaining", mTimeRemaining);
        savedInstanceState.putLong("mPeriodLength", mPeriodLength);
        savedInstanceState.putLong("mPriorityLength", mPriorityLength);
        savedInstanceState.putBoolean("mTimerRunning", mTimerRunning);
        savedInstanceState.putInt("mPeriodNumber", mPeriodNumber);
        savedInstanceState.putLong("mBreakLength", mBreakLength);
        savedInstanceState.putInt("mMode", mMode);
        savedInstanceState.putBoolean("mShowDouble", mShowDouble);
        savedInstanceState.putBoolean("mBlackBackground", mBlackBackground);
        savedInstanceState.putBoolean("mInPeriod", mInPeriod);
        savedInstanceState.putBoolean("mInBreak", mInBreak);
        savedInstanceState.putBoolean("mInPriority", mInPriority);
        mRecentActionArray = new int[mRecentActions.size()];
        for (int i = mRecentActions.size() - 1; i >= 0; i--)
            mRecentActionArray[i] = mRecentActions.pop();
        mTimesRemainingWhenPeriodSkippedArray = new long[mTimesRemainingWhenPeriodSkipped.size()];
        for (int i = mTimesRemainingWhenPeriodSkipped.size() -1; i >= 0; i--)
            mTimesRemainingWhenPeriodSkippedArray[i] = mTimesRemainingWhenPeriodSkipped.pop();
        savedInstanceState.putIntArray("mRecentActionArray", mRecentActionArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items, menu);
        mActionUndo = menu.findItem(R.id.action_undo);
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
        updateTimer(mTimeRemaining);
        updatePriorityIndicators();
    }

    public void updateAll() {
        updateViews();
        updateUndoButton();
    }

    public void resetAll(MenuItem menuItem) { // onClick for action_reset
        resetScores();
        if (mTimeRemaining != mPeriodLength) resetTime();
        resetPriority();
        resetCards();
        resetPeriod();
        resetRecentActions();
        updateAll();
        if (mToast != null) mToast.cancel();
    }


    // METHODS FOR TIME & PERIODS
    public void onClickTimer(View v) { // onClick method for mTimer
        if (mInPeriod || mInBreak || mInPriority) // if in a time section, start/stop
            countDown();
        else { // if in between sections, get ready for next one
            mVibrator.cancel();
            mRinger.stop();
            mTimer.setTextColor(Color.WHITE);
            mTimer.clearAnimation();
            if (mNextSectionType == 0) {
                mTimeRemaining = mPeriodLength;
                nextPeriod();
                mInPeriod = true;
            } else if (mNextSectionType == 1) {
                mTimeRemaining = mBreakLength;
                mInBreak = true;
            } else if (mNextSectionType == 2) {
                mTimeRemaining = mPriorityLength;
                mInPriority = true;
                determinePriority();
            }
            updateAll();
        }
    }

    public void countDown() {
        mRinger.stop();
        mVibrator.cancel();
        if (mTimerRunning) pauseTimer();
        else startTimer(mTimeRemaining);
    }

    private void updateTimer(long millisUntilFinished) {
        long minutes = millisUntilFinished / 60000;
        long seconds = millisUntilFinished / 1000 - minutes * 60;
        long milliseconds = millisUntilFinished % 1000 / 10;
        String timeStr = String.format("%1d:%02d.%02d", minutes,
                seconds, milliseconds);
        mTimer.setText(timeStr);
    }

    private void startTimer(long time) {
        mTimer.clearAnimation();
        mTimer.setTextColor(Color.WHITE);
        mVibrator.vibrate(mStartVibrationPattern, -1);
        if (mInPriority) time = mPriorityLength; // do 1 minute priority rather than normal period
        mCountDownTimer = new CountDownTimer(time, 10) {
            public void onTick(long millisUntilFinished) {
                updateTimer(millisUntilFinished);
                mTimeRemaining = millisUntilFinished;
            }

            public void onFinish() {
                endSection();
            }
        }.start();
        mTimerRunning = true;
    }

    private void pauseTimer() {
        mRinger.stop();
        mVibrator.cancel();
        mVibrator.vibrate(100);
        if (mTimerRunning) {
            mCountDownTimer.cancel();
            mTimerRunning = false;
            mTimer.startAnimation(mBlink);
        }
    }

    private void endSection() { // called when time section is over
        mTimer.setText("0:00.00");
        mTimer.setTextColor(Color.argb(180, 255, 20, 20)); // change timer to red
        mTimer.setAnimation(mBlink);
        mVibrator.vibrate(mEndVibrationPattern, -1);
        mRinger.play();
        mTimerRunning = false;

        // Determine if the bout is in regulation time or overtime
        if (mInPriority) { // overtime
            if (leftFencer.getScore() == rightFencer.getScore()) {
                if (leftFencer.hasPriority() && !rightFencer.hasPriority()) { // left fencer won by priority
                    leftFencer.addScore();
                    leftFencer.makeWinner(rightFencer.getScore());
                    rightFencer.makeLoser(leftFencer.getScore());
                } else if (rightFencer.hasPriority() && !leftFencer.hasPriority()) { // right fencer won by priority
                    rightFencer.addScore();
                    rightFencer.makeWinner(leftFencer.getScore());
                    leftFencer.makeLoser(rightFencer.getScore());
                }
            } else {
                if (leftFencer.getScore() > rightFencer.getScore()) { // left fencer won in priority by a touch
                    leftFencer.makeWinner(rightFencer.getScore());
                    rightFencer.makeLoser(leftFencer.getScore());
                } else if (leftFencer.getScore() < rightFencer.getScore()) { // right fencer won in priority by a touch
                    leftFencer.makeLoser(rightFencer.getScore());
                    rightFencer.makeWinner(leftFencer.getScore());
                }
            }
        } else { // regulation time
            if (mPeriodNumber < mMaxPeriods) { // next period will also be regulation time
                if (mInPeriod) {
                    mNextSectionType = 1;
                    mInPeriod = false;
                } else if (mInBreak) {
                    mNextSectionType = 0;
                    mInBreak = false;
                }
            } else if (leftFencer.getScore() > rightFencer.getScore()) { // left fencer won in regulation time
                leftFencer.makeWinner(rightFencer.getScore());
                rightFencer.makeLoser(leftFencer.getScore());

            } else if (leftFencer.getScore() < rightFencer.getScore()) { // right fencer won in regulation time
                leftFencer.makeLoser(rightFencer.getScore());
                rightFencer.makeWinner(leftFencer.getScore());
            } else if (leftFencer.getScore() == rightFencer.getScore()) { // scores tied; go to priority
                mInPeriod = mInBreak = false;
                mNextSectionType = 2;
            }
        }
    }

    public void nextPeriod(MenuItem menuItem) {
        pauseTimer();
        if (mPeriodNumber < mMaxPeriods) {
            nextPeriod();
            showToast(getResources().getString(R.string.toast_skipped), "", getResources().getString(R.string.period), "");
        }
    }

    private void nextPeriod() {
        mPeriodNumber++;
        updatePeriod();
    }

    private void updatePeriod() {
        if (mInPeriod)
            mPeriodView.setText(getResources().getString(R.string.period) + " " + mPeriodNumber);
        else if (mInBreak)
            mPeriodView.setText(getResources().getString(R.string.rest) + " " + mPeriodNumber);
        else if (mInPriority)
            mPeriodView.setText(getResources().getString(R.string.priority));
    }

    public void determinePriority() {
        int rand = (int) (Math.random() * 100);
        if (rand % 2 == 0) {
            leftFencer.givePriority();
            mPriorityIndicatorLeft.setVisibility(View.VISIBLE);
        } else if (rand % 2 == 1) {
            rightFencer.givePriority();
            mPriorityIndicatorRight.setVisibility(View.VISIBLE);
        }
    }

    private void updatePriorityIndicators() {
        if (leftFencer.hasPriority()) mPriorityIndicatorLeft.setVisibility(View.VISIBLE);
        else mPriorityIndicatorLeft.setVisibility(View.INVISIBLE);
        if (rightFencer.hasPriority()) mPriorityIndicatorRight.setVisibility(View.VISIBLE);
        else mPriorityIndicatorRight.setVisibility(View.INVISIBLE);
    }


    private void resetPeriod() {
        mPeriodNumber = 1;
        mInPeriod = true;
        mInPriority = false;
        mInBreak = false;
    }

    private void resetTime() {
        mTimeRemaining = mPeriodLength;
        mTimer.setText("" + mTimeRemaining);
        mTimer.setTextColor(Color.WHITE);
        updateTimer(mTimeRemaining);
        mTimerRunning = false;
        mRinger.stop();
        mVibrator.cancel();
        mTimer.clearAnimation();
    }

    private void resetPriority() {
        mInPriority = false;
        leftFencer.resetPriority();
        rightFencer.resetPriority();
        mPriorityIndicatorLeft.setVisibility(View.INVISIBLE);
        mPriorityIndicatorRight.setVisibility(View.INVISIBLE);
    }

    // METHODS FOR SCORES
    private void updateScores() {
        mScoreLeftView.setText("" + leftFencer.getScore());
        mRightScoreView.setText("" + rightFencer.getScore());
    }

    public void addScore(View view) { //onClick for score textViews
        switch (view.getId()) {
            case R.id.scoreOne:
                leftFencer.addScore();
                mRecentActions.push(0);
                showToast(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_left));
                break;
            case R.id.scoreTwo:
                rightFencer.addScore();
                mRecentActions.push(1);
                showToast(getResources().getString(R.string.toast_gave), "",  getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_right));
                break;
            case R.id.doubleTouchButton:
                leftFencer.addScore();
                rightFencer.addScore();
                mRecentActions.push(2);
                showToast(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_double),
                        getResources().getString(R.string.toast_touch));
                break;
        }
        pauseTimer();
        updateAll();
    }

    private void subScore(Fencer fencer) {
        fencer.subtractScore();
    }

    private void subScore(String both) {
        leftFencer.subtractScore();
        rightFencer.subtractScore();
    }

    private void resetScores() {
        leftFencer.resetScore();
        rightFencer.resetScore();
        if (mTimerRunning)
            mCountDownTimer.cancel();
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
                        if (leftFencer.hasYellowCard()) {
                            alreadyHadYellow = true;
                            mRecentActions.push(4);
                        }
                        leftFencer.giveYellowCard();
                        if (!alreadyHadYellow) {
                            mRecentActions.push(3);
                            showToast(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_yellow),
                                    getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                            break;
                        }
                    case (1):
                        rightFencer.addScore();
                        leftFencer.giveRedCard();
                        mRecentActions.push(4);
                        showToast(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_red),
                                getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                        break;
                }
                if (leftFencer.hasRedCard()) cardIntent.putExtra("red", true);
                startActivity(cardIntent);
                break;
            case (1):
                switch (cardType) {
                    case (0):
                        if (rightFencer.hasYellowCard()) {
                            alreadyHadYellow = true;
                            mRecentActions.push(6);
                        }
                        rightFencer.giveYellowCard();
                        if (!alreadyHadYellow) {
                            mRecentActions.push(5);
                            showToast(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_yellow),
                                    getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                            break;
                        }
                    case (1):
                        leftFencer.addScore();
                        rightFencer.giveRedCard();
                        mRecentActions.push(6);
                        showToast(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_red),
                                getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                        break;
                }
                if (rightFencer.hasRedCard()) cardIntent.putExtra("red", true);
                startActivity(cardIntent); // launch card activity
        }
        updateAll();
        pauseTimer();
    }

    private void resetCards() { // remove all penalties and clear indicator views
        leftFencer.takeYellowCard();
        leftFencer.takeRedCard();
        rightFencer.takeYellowCard();
        rightFencer.takeRedCard();
        updateCardIndicators();
    }

    private void updateCardIndicators() { // update penalty indicator views
        if (leftFencer.hasYellowCard()) mYellowIndicatorLeft.setVisibility(View.VISIBLE);
        else mYellowIndicatorLeft.setVisibility(View.INVISIBLE);
        if (leftFencer.hasRedCard()) mRedIndicatorLeft.setVisibility(View.VISIBLE);
        else mRedIndicatorLeft.setVisibility(View.INVISIBLE);
        if (rightFencer.hasYellowCard()) mYellowIndicatorRight.setVisibility(View.VISIBLE);
        else mYellowIndicatorRight.setVisibility(View.INVISIBLE);
        if (rightFencer.hasRedCard()) mRedIndicatorRight.setVisibility(View.VISIBLE);
        else mRedIndicatorRight.setVisibility(View.INVISIBLE);
    }

    public void showCardDialog(View view) { // onClick for yellowCardButton & redCardButton
        pauseTimer();
        FragmentManager man = this.getFragmentManager();
//        CardAlertFragment dialog = new CardAlertFragment(view);
        CardAlertFragment dialog = CardAlertFragment.newInstance(view);
        dialog.show(man, "Penalty Card");
    }

    // METHODS FOR RECENT ACTIONS & UNDO
    // 0 = touch left, 1 = touch right, 2 = double touch,
    // 3 = yellow to left, 4 = red to left, 5 = yellow to right, 6 = red to right
    // 7 = skip period
    private void resetRecentActions() {
        mRecentActions = new ArrayDeque<Integer>(0);
    }

    private void undoAction(Integer action) {
        switch (action) {
            case 0:
                subScore(leftFencer);
                showToast(getResources().getString(R.string.toast_undid), "",  getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_left));
                break;
            case 1:
                subScore(rightFencer);
                showToast(getResources().getString(R.string.toast_undid), "", getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_right));
                break;
            case 2:
                subScore("both");
                showToast(getResources().getString(R.string.toast_undid), "",  getResources().getString(R.string.toast_double),
                        getResources().getString(R.string.toast_touch));
                break;
            case 3:
                leftFencer.takeYellowCard();
                showToast(getResources().getString(R.string.toast_undid), getResources().getString(R.string.toast_yellow),
                        getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                break;
            case 4:
                leftFencer.takeRedCard();
                subScore(rightFencer);
                showToast(getResources().getString(R.string.toast_undid), getResources().getString(R.string.toast_red),
                        getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                break;
            case 5:
                rightFencer.takeYellowCard();
                showToast(getResources().getString(R.string.toast_undid), getResources().getString(R.string.toast_yellow),
                        getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                break;
            case 6:
                rightFencer.takeRedCard();
                subScore(leftFencer);
                showToast(getResources().getString(R.string.toast_undid), getResources().getString(R.string.toast_red),
                        getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                break;
            case 7:

        }
        mRecentActions.pop();
        updateAll();
    }

    public void undoMostRecent(MenuItem item) {
        undoAction(mRecentActions.peek());
    }

    private void updateUndoButton() {
        if (mRecentActions.isEmpty()) mActionUndo.setVisible(false);
        else mActionUndo.setVisible(true);
    }

    public void openSettings(MenuItem item) {
        Intent settingsIntent = new Intent(this, Settings.class);
        startActivity(settingsIntent);
    }

    private void loadSettings() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // make bout end based on preferences
        mMode = Integer.parseInt(mSharedPreferences.getString("pref_mode", "5"));
        switch (mMode) {
            case (5):
                mMaxPeriods = 1;
                break;
            case (15):
                mMaxPeriods = 3;
                break;
        }

        // show or hide double touch button based on preferences
        mShowDouble = mSharedPreferences.getBoolean("pref_show_double", true);
        if (mShowDouble) findViewById(R.id.doubleTouchButton).setVisibility(View.VISIBLE);
        else findViewById(R.id.doubleTouchButton).setVisibility(View.INVISIBLE);

        // make background color grey or black based on preferences
        mBlackBackground = mSharedPreferences.getBoolean("pref_black", false);
        System.out.println("mBlackBackground = " + mBlackBackground);
        if (mBlackBackground) { mMainLayout.setBackgroundColor(Color.BLACK); System.out.println("Trying to make BG black"); }
        else mMainLayout.setBackgroundColor(Color.rgb(32, 32, 32));
    }

    private void showToast(String verb, String color, String noun, String recipient) {
        Context context = getApplicationContext();

        CharSequence text;
        if ((recipient == null || recipient.equals("")) && (color == null || color.equals("")))    text = verb + " " + noun;
        else if (noun == null || noun.equals(""))   text = verb + " " + recipient;
        else if (color == null || color.equals(""))   text = verb + " " + noun + " " + recipient;
        else text = verb + " " + color + " " + noun + " " + recipient;

        int duration = Toast.LENGTH_SHORT;

        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }

}