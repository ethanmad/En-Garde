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

import com.crashlytics.android.Crashlytics;

import java.util.ArrayDeque;

public class MainActivity extends Activity implements CardAlertFragment.CardAlertListener {
    private Fencer leftFencer, rightFencer;
    private long mTimeRemaining, mPeriodLength, mBreakLength, mPriorityLength;
    private long[] mStartVibrationPattern, mEndVibrationPattern, mPreviousTimesArray;
    private int mPeriodNumber, mNextSectionType, mMode, mMaxPeriods;
    private int[] mRecentActionArray, mPreviousPeriodNumbersArray, mPreviousSectionTypesArray;
    private TextView mTimer, mScoreLeftView, mRightScoreView, mPeriodView;
    private ImageView mLeftPenaltyIndicator, mLeftPriorityIndicator, mRightPenaltyIndicator, mRightPriorityIndicator;
    private boolean mTimerRunning, mInPeriod, mInBreak, mInPriority, mShowDouble, mBlackBackground;
    private CountDownTimer mCountDownTimer;
    private Vibrator mVibrator;
    private Uri mAlert;
    private Ringtone mRinger;
    private Animation mBlink;
    private ArrayDeque<Integer> mRecentActions, mPreviousPeriodNumbers, mPreviousSectionTypes;
    private ArrayDeque<Long> mPreviousTimes;
    private MenuItem mActionUndo;
    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    private RelativeLayout mMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.main_activity);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        leftFencer = new Fencer();
        rightFencer = new Fencer();

        mTimer = (TextView) findViewById(R.id.timer);
        mScoreLeftView = (TextView) findViewById(R.id.scoreOne);
        mRightScoreView = (TextView) findViewById(R.id.scoreTwo);
        mPeriodView = (TextView) findViewById(R.id.periodView);
        mLeftPenaltyIndicator = (ImageView) findViewById(R.id.penaltyCircleViewLeft);
        mLeftPriorityIndicator = (ImageView) findViewById(R.id.priorityCircleViewLeft);
        mRightPenaltyIndicator = (ImageView) findViewById(R.id.penaltyCircleViewRight);
        mRightPriorityIndicator = (ImageView) findViewById(R.id.priorityCircleViewRight);
        mMainLayout = (RelativeLayout) findViewById(R.id.mainLayout);


        // import previous data if it exists, otherwise use default values on right
        if (savedInstanceState == null) savedInstanceState = new Bundle();
        mPeriodLength = savedInstanceState.getLong("mPeriodLength", 3 * 60 * 1000);
        mBreakLength = savedInstanceState.getLong("mBreakLength", 1 * 60 * 1000);
        mPriorityLength = savedInstanceState.getLong("mPriorityLength", 1 * 60 * 1000);
        mTimeRemaining = savedInstanceState.getLong("mTimeRemaining", mPeriodLength);
        mPreviousTimesArray = savedInstanceState.getLongArray("mPreviousTimesArray");
        mPreviousPeriodNumbersArray = savedInstanceState.getIntArray("mPreviousPeriodNumbersArray");
        mPreviousSectionTypesArray = savedInstanceState.getIntArray("mPreviousSectionTypesArray");
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

        if (mPreviousTimesArray == null) mPreviousTimes = new ArrayDeque<Long>(0);
        else for (long time : mPreviousTimesArray)
            mPreviousTimes.push(time);

        if (mPreviousPeriodNumbers == null) mPreviousPeriodNumbers = new ArrayDeque<Integer>(0);
        else for (int sectionType : mPreviousPeriodNumbersArray)
            mPreviousPeriodNumbers.push(sectionType);

        if (mPreviousSectionTypesArray == null) mPreviousSectionTypes = new ArrayDeque<Integer>(0);
        else for (int sectionType : mPreviousSectionTypesArray)
            mPreviousSectionTypes.push(sectionType);

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

        // reload recent actions, previous times, etc.
        if (mRecentActionArray == null)
            mRecentActions = new ArrayDeque<Integer>(0);
        else for (int action : mRecentActionArray)
            mRecentActions.push(action);

        if (mPreviousTimesArray == null) mPreviousTimes = new ArrayDeque<Long>(0);
        else for (long time : mPreviousTimesArray)
            mPreviousTimes.push(time);

        if (mPreviousPeriodNumbers == null) mPreviousPeriodNumbers = new ArrayDeque<Integer>(0);
        else for (int sectionType : mPreviousPeriodNumbersArray)
            mPreviousPeriodNumbers.push(sectionType);

        if (mPreviousSectionTypesArray == null) mPreviousSectionTypes = new ArrayDeque<Integer>(0);
        else for (int sectionType : mPreviousSectionTypesArray)
            mPreviousSectionTypes.push(sectionType);

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
        savedInstanceState.putIntArray("mRecentActionArray", mRecentActionArray);

        mPreviousTimesArray = new long[mPreviousTimes.size()];
        if (mPreviousTimes.size() > 0)
            for (int i = mPreviousTimes.size() - 1; i >= 0; i--)
                mPreviousTimesArray[i] = mPreviousTimes.pop();
        savedInstanceState.putLongArray("mPreviousTimesArray", mPreviousTimesArray);

        mPreviousPeriodNumbersArray = new int[mPreviousPeriodNumbers.size()];
        if (mPreviousPeriodNumbers.size() > 0)
            for (int i = mPreviousPeriodNumbers.size() - 1; i >= 0; i--)
                mPreviousPeriodNumbersArray[i] = mPreviousPeriodNumbers.pop();
        savedInstanceState.putIntArray("mPreviousPeriodNumbersArray", mPreviousPeriodNumbersArray);

        mPreviousSectionTypesArray = new int[mPreviousSectionTypes.size()];
        if (mPreviousSectionTypes.size() > 0)
            for (int i = mPreviousSectionTypes.size() - 1; i >= 0; i--)
                mPreviousSectionTypesArray[i] = mPreviousSectionTypes.pop();
        savedInstanceState.putIntArray("mPreviousSectionTypesArray", mPreviousSectionTypesArray);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items, menu);
        mActionUndo = menu.findItem(R.id.action_undo);
        updateUndoButton();
        return super.onCreateOptionsMenu(menu);
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
        updatePenaltyIndicators();
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
    public void onClickTimer(View view) { // onClick method for mTimer
        if (mInPeriod || mInBreak || mInPriority) // if in a time section, start/stop
            countDown();
        else { // if in between sections, get ready for next one
            mVibrator.cancel();
            mRinger.stop();
            mTimer.setTextColor(Color.WHITE);
            mTimer.clearAnimation();
            if (mNextSectionType == 0) { // period
                mTimeRemaining = mPeriodLength;
                nextPeriod();
                mInPeriod = true;
            } else if (mNextSectionType == 1) { // break
                mTimeRemaining = mBreakLength;
                mInBreak = true;
            } else if (mNextSectionType == 2) { // priority
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

    public void pauseTimer(View view) {
        pauseTimer();
    }

    private void pauseTimer() {
        mRinger.stop();
        mVibrator.cancel();
        if (mTimerRunning) {
            mVibrator.vibrate(100);
            mCountDownTimer.cancel();
            mTimerRunning = false;

            //only blink if time remaining < time in section
            if ((mTimeRemaining != 60 * 1000 && mTimeRemaining != 3 * 60 * 1000) ||
                    (mTimeRemaining == 60 * 1000 && !(mInBreak || mInPriority)) ||
                    (mTimeRemaining == 3 * 60 * 1000 && !mInPeriod))
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

    public void skipSection(MenuItem menuItem) {
        pauseTimer();
        if (mInPriority)
            showToast(getResources().getString(R.string.toast_unable), "", getResources().getString(R.string.toast_skip), getResources().getString(R.string.toast_priority));
        else {
            mPreviousTimes.push(mTimeRemaining);
            if (mInPriority) mPreviousSectionTypes.push(2);
            else if (mInBreak) mPreviousSectionTypes.push(1);
            else if (mInPeriod) mPreviousSectionTypes.push(0);
            mPreviousPeriodNumbers.push(mPeriodNumber);
            endSection();
            mRecentActions.push(7);
            if (!mInPeriod)
                showToast(getResources().getString(R.string.toast_skipped), "", getResources().getString(R.string.toast_period), "");
            else
                showToast(getResources().getString(R.string.toast_skipped), "", getResources().getString(R.string.toast_break), "");
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
            mLeftPriorityIndicator.setVisibility(View.VISIBLE);
        } else if (rand % 2 == 1) {
            rightFencer.givePriority();
            mRightPriorityIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void updatePriorityIndicators() {
        if (leftFencer.hasPriority()) mLeftPriorityIndicator.setVisibility(View.VISIBLE);
        else mLeftPriorityIndicator.setVisibility(View.INVISIBLE);
        if (rightFencer.hasPriority()) mRightPriorityIndicator.setVisibility(View.VISIBLE);
        else mRightPriorityIndicator.setVisibility(View.INVISIBLE);
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
        mLeftPriorityIndicator.setVisibility(View.INVISIBLE);
        mRightPriorityIndicator.setVisibility(View.INVISIBLE);
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
                showToast(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_touch),
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
        updatePenaltyIndicators();
    }

    private void updatePenaltyIndicators() { // update penalty indicator views
        if (leftFencer.hasRedCard()) {
            mLeftPenaltyIndicator.setColorFilter(Color.RED);
            mLeftPenaltyIndicator.setVisibility(View.VISIBLE);
        } else if (leftFencer.hasYellowCard()) mLeftPenaltyIndicator.setVisibility(View.VISIBLE);
        else mLeftPenaltyIndicator.setVisibility(View.INVISIBLE);

        if (rightFencer.hasRedCard()) {
            mRightPenaltyIndicator.setColorFilter(Color.RED);
            mRightPenaltyIndicator.setVisibility(View.VISIBLE);
        } else if (rightFencer.hasYellowCard()) mRightPenaltyIndicator.setVisibility(View.VISIBLE);
        else mRightPenaltyIndicator.setVisibility(View.INVISIBLE);
    }

    public void showCardDialog(View view) { // onClick for yellowCardButton & redCardButton
        pauseTimer();
        FragmentManager man = this.getFragmentManager();
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
                showToast(getResources().getString(R.string.toast_undid), "", getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_left));
                break;
            case 1:
                subScore(rightFencer);
                showToast(getResources().getString(R.string.toast_undid), "", getResources().getString(R.string.toast_touch),
                        getResources().getString(R.string.toast_right));
                break;
            case 2:
                subScore("both");
                showToast(getResources().getString(R.string.toast_undid), "", getResources().getString(R.string.toast_double),
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
                pauseTimer();
                mInPriority = false;
                long previousTime = mPreviousTimes.pop();
                startTimer(previousTime);
                mNextSectionType = mPreviousSectionTypes.pop();
                mPeriodNumber = mPreviousPeriodNumbers.pop();

                if (mMaxPeriods == 1) {
                    mPeriodNumber--;
                } else {
                    if (mNextSectionType == 0) {
                        mInPeriod = true;
                        mInBreak = true;
                    } else if (mNextSectionType == 1) {
                        mInPeriod = false;
                        mInBreak = true;
                    }
                }

                onClickTimer(mTimer);
                mTimeRemaining = previousTime;
                updateTimer(mTimeRemaining);
                pauseTimer();
                resetPriority();
                showToast(getResources().getString(R.string.toast_undid), "", getResources().getString(R.string.toast_skip), "");
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
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
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
        if (mBlackBackground) {
            mMainLayout.setBackgroundColor(Color.BLACK);
        } else mMainLayout.setBackgroundColor(Color.rgb(32, 32, 32));
    }

    private void showToast(String verb, String color, String noun, String recipient) {
        Context context = getApplicationContext();

        CharSequence text;
        if ((recipient == null || recipient.equals("")) && (color == null || color.equals("")))
            text = verb + " " + noun;
        else if (noun == null || noun.equals("")) text = verb + " " + recipient;
        else if (color == null || color.equals("")) text = verb + " " + noun + " " + recipient;
        else text = verb + " " + color + " " + noun + " " + recipient;

        int duration = Toast.LENGTH_SHORT;

        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }

}