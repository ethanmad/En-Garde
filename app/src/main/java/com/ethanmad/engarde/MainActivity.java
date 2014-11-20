package com.ethanmad.engarde;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.mrengineer13.snackbar.SnackBar;

import java.util.ArrayDeque;

public class MainActivity extends Activity implements CardAlertFragment.CardAlertListener, SnackBar.OnMessageClickListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 200);
    private Fencer leftFencer, rightFencer;
    private long mTimeRemaining, mPeriodLength, mBreakLength, mPriorityLength;
    private long[] mStartVibrationPattern, mEndVibrationPattern, mPreviousTimesArray;
    private int mPeriodNumber, mNextSectionType, mMode, mMaxPeriods;
    private int[] mRecentActionArray, mPreviousPeriodNumbersArray, mPreviousSectionTypesArray;
    private TextView mTimer, mLeftScoreView, mRightScoreView, mPeriodView, mLeftWinnerView,
            mRightWinnerView;
    private ImageView mLeftPenaltyIndicator, mLeftPriorityIndicator, mRightPenaltyIndicator,
            mRightPriorityIndicator;
    private boolean mTimerRunning, mInPeriod, mInBreak, mInPriority, mShowDouble, mBlackBackground,
            mIsOver;
    private CountDownTimer mCountDownTimer;
    private Vibrator mVibrator;
    private Ringtone mRinger;
    private Animation mBlink;
    private ArrayDeque<Integer> mRecentActions, mPreviousPeriodNumbers, mPreviousSectionTypes;
    private ArrayDeque<Long> mPreviousTimes;
    private MenuItem mActionUndo;
    private RelativeLayout mMainLayout;
    private SnackBar mSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.main_activity);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        leftFencer = new Fencer();
        rightFencer = new Fencer();

        mTimer = (TextView) findViewById(R.id.timer);
        mLeftScoreView = (TextView) findViewById(R.id.scoreOne);
        mRightScoreView = (TextView) findViewById(R.id.scoreTwo);
        mLeftWinnerView = (TextView) findViewById(R.id.winnerViewLeft);
        mLeftWinnerView.setVisibility(View.INVISIBLE);
        mRightWinnerView = (TextView) findViewById(R.id.winnerViewRight);
        mRightWinnerView.setVisibility(View.INVISIBLE);
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
        mIsOver = savedInstanceState.getBoolean("mIsOver", false);

        updateViews(); // update all views from default strings to real data
        loadSettings(); // load user settings

        if (mRecentActionArray == null) mRecentActions = new ArrayDeque<Integer>(0);
        else for (int action : mRecentActionArray)
            mRecentActions.push(action);

        if (mPreviousTimesArray == null) mPreviousTimes = new ArrayDeque<Long>(0);
        else for (long time : mPreviousTimesArray)
            mPreviousTimes.push(time);

        if (mPreviousPeriodNumbersArray == null)
            mPreviousPeriodNumbers = new ArrayDeque<Integer>(0);
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
        mStartVibrationPattern = new long[] {0, 50, 100, 50};
        mEndVibrationPattern = new long[] {0, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50,
                500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50,
                100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50,
                500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50,
                100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50,
                500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50,
                100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50,
                500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50,
                100, 50, 500, 50, 100, 50, 500, 50, 100, 50, 500, 50, 100, 50};
        // TODO change to buzzer rather than ringtone
        Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (mAlert == null) { // mAlert is null, using backup
            mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // just in case
            if (mAlert == null) {
                // mAlert backup is null, using 2nd backup
                mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        mRinger = RingtoneManager.getRingtone(getApplicationContext(), mAlert);
        mSnackBar = new SnackBar(this);
        mSnackBar.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // reload recent actions, previous times, etc.
        if (mRecentActionArray == null) mRecentActions = new ArrayDeque<Integer>(0);
        else for (int action : mRecentActionArray)
            mRecentActions.push(action);

        if (mPreviousTimesArray == null) mPreviousTimes = new ArrayDeque<Long>(0);
        else for (long time : mPreviousTimesArray)
            mPreviousTimes.push(time);

        if (mPreviousPeriodNumbersArray == null)
            mPreviousPeriodNumbers = new ArrayDeque<Integer>(0);
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
        updateOver();
        updateWinner();
    }

    public void resetAll(MenuItem menuItem) { // onClick for action_reset
        resetScores();
        if (mTimeRemaining != mPeriodLength) resetTime();
        resetPriority();
        resetCards();
        resetPeriod();
        resetRecentActions();
        updateAll();
        resetOver();
        resetWinner();
        if (mSnackBar != null) {
            mSnackBar.clear(true);
        }
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
        if (mTimeRemaining != 0) mTimer.setTextColor(Color.WHITE);
        long minutes = millisUntilFinished / 60000;
        long seconds = millisUntilFinished / 1000 - minutes * 60;
        long milliseconds = millisUntilFinished % 1000 / 10;
        String timeStr = String.format("%1d:%02d.%02d", minutes,
                seconds, milliseconds);
        mTimer.setText(timeStr);
    }

    private void startTimer(long time) {
        if (!mIsOver) {
            mTimer.clearAnimation();
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF);
            mTimer.setTextColor(Color.WHITE);
            mVibrator.vibrate(mStartVibrationPattern, -1);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep screen awake when timer is running
            mCountDownTimer = new CountDownTimer(time, 10) {
                public void onTick(long millisUntilFinished) {
                    updateTimer(millisUntilFinished);
                    mTimeRemaining = millisUntilFinished;
                }

                public void onFinish() {
                    endSection();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // turn screen-awake off when timer is expired
                }
            }.start();
            mTimerRunning = true;
        }
    }

    private void pauseTimer() {
        toneGenerator.stopTone();
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

            if (!mTimerRunning)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // disable keep screen awake
        }
    }

    private void endSection() { // called when time section is over
        mTimer.setText("0:00.00");
        mTimeRemaining = 0;
        mTimer.setTextColor(getResources().getColor(R.color.red_timer)); // change timer to red
        mTimer.setAnimation(mBlink);
        mVibrator.vibrate(mEndVibrationPattern, -1);
        mRinger.play();
        mTimerRunning = false;

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // disable keep screen awake

        // Determine if the bout is in regulation time or overtime
        if (mInPriority) { // overtime
            if (leftFencer.getScore() == rightFencer.getScore()) {
                if (leftFencer.hasPriority() && !rightFencer.hasPriority()) { // left fencer won by priority
                    leftFencer.addScore();
                    leftFencer.makeWinner(rightFencer.getScore());
                    rightFencer.makeLoser(leftFencer.getScore());
                    mIsOver = true;
                } else if (rightFencer.hasPriority() && !leftFencer.hasPriority()) { // right fencer won by priority
                    rightFencer.addScore();
                    rightFencer.makeWinner(leftFencer.getScore());
                    leftFencer.makeLoser(rightFencer.getScore());
                    mIsOver = true;
                }
            } else {
                if (leftFencer.getScore() > rightFencer.getScore()) { // left fencer won in priority by a touch
                    leftFencer.makeWinner(rightFencer.getScore());
                    rightFencer.makeLoser(leftFencer.getScore());
                    mIsOver = true;
                } else if (leftFencer.getScore() < rightFencer.getScore()) { // right fencer won in priority by a touch
                    leftFencer.makeLoser(rightFencer.getScore());
                    rightFencer.makeWinner(leftFencer.getScore());
                    mIsOver = true;
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
                mIsOver = true;
            } else if (leftFencer.getScore() < rightFencer.getScore()) { // right fencer won in regulation time
                leftFencer.makeLoser(rightFencer.getScore());
                rightFencer.makeWinner(leftFencer.getScore());
                mIsOver = true;
            } else if (leftFencer.getScore() == rightFencer.getScore()) { // scores tied; go to priority
                mInPeriod = mInBreak = false;
                mNextSectionType = 2;
            }
        }
    }

    public void skipSection(MenuItem menuItem) {
        if (!mIsOver) {
            pauseTimer();
            if (mInPriority)
                showSnackbar(getResources().getString(R.string.toast_unable), "",
                        getResources().getString(R.string.toast_skip), getResources().getString(R.string.toast_priority));
            else {
                mPreviousTimes.push(mTimeRemaining);
                if (mInPriority) mPreviousSectionTypes.push(2);
                else if (mInBreak) mPreviousSectionTypes.push(1);
                else if (mInPeriod) mPreviousSectionTypes.push(0);
                mPreviousPeriodNumbers.push(mPeriodNumber);
                endSection();
                mRinger.stop();
                mVibrator.cancel();
                mRecentActions.push(7);
                if (!mInPeriod)
                    showSnackbar(getResources().getString(R.string.toast_skipped), "", getResources().getString(R.string.toast_period), "");
                else
                    showSnackbar(getResources().getString(R.string.toast_skipped), "", getResources().getString(R.string.toast_break), "");
            }
            updateAll();
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

    // use Math.random() to determine priority
    public void determinePriority() {
        double rand = Math.random();
        if (rand >= .5 ) {
            leftFencer.givePriority();
            mLeftPriorityIndicator.setVisibility(View.VISIBLE);
        } else {
            rightFencer.givePriority();
            mRightPriorityIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void updatePriorityIndicators() {
        mLeftPriorityIndicator.setVisibility(leftFencer.hasPriority() ? View.VISIBLE :
                View.INVISIBLE );
        mRightPriorityIndicator.setVisibility(rightFencer.hasPriority() ? View.VISIBLE :
                View.INVISIBLE);
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

    private void updateOver() { // determine if bout is over and set mIsOver
        mIsOver = leftFencer.getScore() >= mMode || rightFencer.getScore() >= mMode ||
                (mTimeRemaining == 0 && (mInPriority ||
                        leftFencer.getScore() > rightFencer.getScore() ||
                        rightFencer.getScore() < leftFencer.getScore()));
    }

    private void resetOver() {
        mIsOver = false;
    }

    // METHODS FOR SCORES
    private void updateScores() {
        mLeftScoreView.setText("" + leftFencer.getScore());
        mRightScoreView.setText("" + rightFencer.getScore());
    }

    public void addScore(View view) { //onClick for score textViews
        if (mIsOver) {
            showSnackbar(getResources().getString(R.string.toast_unable), getResources().getString(R.string.toast_give), getResources().getString(R.string.toast_touch), getResources().getString(R.string.toast_winner_determined));
        } else {
            pauseTimer();
            switch (view.getId()) {
                case R.id.scoreOne:
                    leftFencer.addScore();
                    mRecentActions.push(0);
                    showSnackbar(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_touch),
                            getResources().getString(R.string.toast_left));
                    if (leftFencer.getScore() >= mMode || mInPriority) {
                        leftFencer.makeWinner(rightFencer.getScore());
                        mIsOver = true;
                    } else mIsOver = false;
                    break;
                case R.id.scoreTwo:
                    rightFencer.addScore();
                    mRecentActions.push(1);
                    showSnackbar(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_touch),
                            getResources().getString(R.string.toast_right));
                    if (rightFencer.getScore() >= mMode || mInPriority) {
                        rightFencer.makeWinner(leftFencer.getScore());
                        mIsOver = true;
                    } else mIsOver = false;
                    break;
                case R.id.doubleTouchButton:
                    if (leftFencer.getScore() == rightFencer.getScore() &&
                            leftFencer.getScore() == mMode - 1) {
                        showSnackbar(getResources().getString(R.string.toast_unable), "", getResources().getString(R.string.toast_give), getResources().getString(R.string.toast_touch));
                    } else {
                        leftFencer.addScore();
                        rightFencer.addScore();
                        mRecentActions.push(2);
                        showSnackbar(getResources().getString(R.string.toast_gave), "", getResources().getString(R.string.toast_double),
                                getResources().getString(R.string.toast_touch));
                        if (leftFencer.getScore() >= mMode) {
                            leftFencer.makeWinner(rightFencer.getScore());
                            mIsOver = true;
                        } else if (rightFencer.getScore() >= mMode) {
                            rightFencer.makeWinner(leftFencer.getScore());
                            mIsOver = true;
                        } else mIsOver = false;
                        break;
                    }
            }
            updateAll();
        }
    }

    private void subScore(Fencer fencer) {
        fencer.subtractScore();
    }

    private void subBothScores() {
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

    private void updateWinner() { // make a TextView with "Winner" appear above winner's score
        if (mIsOver) {
            if (leftFencer.isWinner() && !rightFencer.isWinner()) {
                mLeftWinnerView.setVisibility(View.VISIBLE);
                mRightWinnerView.setVisibility(View.INVISIBLE);
                mIsOver = true;
            } else if (rightFencer.isWinner() && !leftFencer.isWinner()) {
                mRightWinnerView.setVisibility(View.VISIBLE);
                mLeftWinnerView.setVisibility(View.INVISIBLE);
                mIsOver = true;
            } else {
                mLeftWinnerView.setVisibility(View.INVISIBLE);
                mRightWinnerView.setVisibility(View.INVISIBLE);
                mIsOver = false;
            }
        } else {
            mLeftWinnerView.setVisibility(View.INVISIBLE);
            mRightWinnerView.setVisibility(View.INVISIBLE);
            mIsOver = false;
        }
    }

    private void resetWinner() {
        mLeftWinnerView.setVisibility(View.INVISIBLE);
        mRightWinnerView.setVisibility(View.INVISIBLE);
        leftFencer.takeWinner(rightFencer.getScore());
        rightFencer.takeWinner(leftFencer.getScore());
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
                        if (leftFencer.hasYellowCard() || leftFencer.hasRedCard())
                            alreadyHadYellow = true;
                        leftFencer.giveYellowCard();
                        if (!alreadyHadYellow) {
                            mRecentActions.push(3);
                            showSnackbar(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_yellow),
                                    getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                            break;
                        }
                    case (1):
                        if (rightFencer.getScore() < mMode) rightFencer.addScore();
                        leftFencer.giveRedCard();
                        mRecentActions.push(4);
                        showSnackbar(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_red),
                                getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_left));
                        if (rightFencer.getScore() >= mMode) {
                            rightFencer.makeWinner(leftFencer.getScore());
                            mIsOver = true;
                        }
                        break;
                }
                if (leftFencer.hasRedCard()) cardIntent.putExtra("red", true);
                startActivity(cardIntent);
                break;
            case (1):
                switch (cardType) {
                    case (0):
                        if (rightFencer.hasYellowCard() || rightFencer.hasRedCard())
                            alreadyHadYellow = true;
                        rightFencer.giveYellowCard();
                        if (!alreadyHadYellow) {
                            mRecentActions.push(5);
                            showSnackbar(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_yellow),
                                    getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                            break;
                        }
                    case (1):
                        if (leftFencer.getScore() < mMode) leftFencer.addScore();
                        rightFencer.giveRedCard();
                        mRecentActions.push(6);
                        showSnackbar(getResources().getString(R.string.toast_gave), getResources().getString(R.string.toast_red),
                                getResources().getString(R.string.toast_card), getResources().getString(R.string.toast_right));
                        if (leftFencer.getScore() >= mMode) {
                            leftFencer.makeWinner(rightFencer.getScore());
                            mIsOver = true;
                        }
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
        } else if (leftFencer.hasYellowCard()) {
            mLeftPenaltyIndicator.setColorFilter(Color.YELLOW);
            mLeftPenaltyIndicator.setVisibility(View.VISIBLE);
        } else mLeftPenaltyIndicator.setVisibility(View.INVISIBLE);

        if (rightFencer.hasRedCard()) {
            mRightPenaltyIndicator.setColorFilter(Color.RED);
            mRightPenaltyIndicator.setVisibility(View.VISIBLE);
        } else if (rightFencer.hasYellowCard()) {
            mRightPenaltyIndicator.setColorFilter(Color.YELLOW);
            mRightPenaltyIndicator.setVisibility(View.VISIBLE);
        } else mRightPenaltyIndicator.setVisibility(View.INVISIBLE);
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
        pauseTimer();
        switch (action) {
            case 0:
                subScore(leftFencer);
                if (leftFencer.isWinner()) leftFencer.takeWinner(rightFencer.getScore());
                break;
            case 1:
                subScore(rightFencer);
                if (rightFencer.isWinner()) rightFencer.takeWinner(leftFencer.getScore());
                break;
            case 2:
                subBothScores();
                if (leftFencer.isWinner()) leftFencer.takeWinner(rightFencer.getScore());
                else if (rightFencer.isWinner()) rightFencer.takeWinner(leftFencer.getScore());
                break;
            case 3:
                leftFencer.takeYellowCard();
                break;
            case 4:
                leftFencer.takeRedCard();
                subScore(rightFencer);
                break;
            case 5:
                rightFencer.takeYellowCard();
                break;
            case 6:
                rightFencer.takeRedCard();
                subScore(leftFencer);
                break;
            case 7:
                pauseTimer();
                mInPriority = false;
                long previousTime = mPreviousTimes.pop();
                startTimer(previousTime);

                mNextSectionType = mPreviousSectionTypes.pop();
                mPeriodNumber = mPreviousPeriodNumbers.pop();

                if (mMaxPeriods == 1) {
                    mInPeriod = true;
                    mPeriodNumber = 1;
                } else {
                    if (mNextSectionType == 0) {
                        mInPeriod = true;
                        mInBreak = false;
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
                resetOver();
        }
        mRecentActions.pop();
        updateAll();
    }

    public void undoMostRecent(MenuItem item) {
        undoAction(mRecentActions.peek());
    }

    private void undoMostRecent() {
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

    private void loadSettings() { // load user settings and make changes based on them
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        // control click-anywhere-to-start feature
        boolean mAnywhereToStart = mSharedPreferences.getBoolean("pref_anywhere_to_start", true);
        View cardLayout = findViewById(R.id.cardLayout);
        if (mAnywhereToStart) {
            mMainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickTimer(view);
                }
            });
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickTimer(view);
                }
            });

        }
        else {
            mMainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public  void onClick(View view) {
                    pauseTimer();
                }
            });
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pauseTimer();
                }
            });
        }

        // make background color grey or black based on preferences
        mBlackBackground = mSharedPreferences.getBoolean("pref_black", false);
        if (mBlackBackground) mMainLayout.setBackgroundColor(Color.BLACK);
        else mMainLayout.setBackgroundColor(getResources().getColor(R.color.background));
    }

    // SNACKBAR METHODS
    private void showSnackbar(String verb, String color, String noun, String recipient) {
        //display snackbar
        String text;
        if ((recipient == null || recipient.equals("")) && (color == null || color.equals("")))
            text = verb + " " + noun;
        else if (noun == null || noun.equals("")) text = verb + " " + recipient;
        else if (color == null || color.equals("")) text = verb + " " + noun + " " + recipient;
        else text = verb + " " + color + " " + noun + " " + recipient;

        short duration = Toast.LENGTH_SHORT;

        mSnackBar.show(text, "undo", SnackBar.Style.ALERT, duration);
    }

    public void onMessageClick(Parcelable token) {
        undoMostRecent();
    }
}