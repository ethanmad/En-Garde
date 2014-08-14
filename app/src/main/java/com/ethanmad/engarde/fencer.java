package com.ethanmad.engarde;

import android.os.Bundle;

/**
 * Created by ethan on 8/14/14.
 */
public class Fencer {
    private int mScore;
    private boolean mHasYellowCard, mHasRedCard;
    private boolean mHasPriority;

    protected void onCreate(Bundle savedInstanceState)  {
        mScore = savedInstanceState.getInt("mScore", 0);
        mHasYellowCard = savedInstanceState.getBoolean("mHasYellowCard", false);
        mHasRedCard = savedInstanceState.getBoolean("mHasRedCard", false);
        mHasPriority = savedInstanceState.getBoolean("mHasPriority", false);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("mScore", mScore);
        savedInstanceState.putBoolean("mHasYellowCard", mHasYellowCard);
        savedInstanceState.putBoolean("mHasRedCard", mHasRedCard);
        savedInstanceState.putBoolean("mHasPriority", mHasPriority);
    }

    public int getScore() {
        return mScore;
    }
    public void addScore() {
        mScore++;
    }
    public void subtractScore() {
        mScore--;
    }
    public void resetScore() {
        mScore = 0;
    }

    public boolean hasYellowCard() {
        return mHasYellowCard;
    }
    public void giveYellowCard() {
        mHasYellowCard = true;
    }
    public void takeYellowCard() {
        mHasYellowCard = false;
    }

    public boolean hasRedCard() {
        return mHasRedCard;
    }
    public void giveRedCard() {
        mHasRedCard = true;
    }
    public void takeRedCard() {
        mHasRedCard = false;
    }

    public boolean hasPriority() {
        return mHasPriority;
    }
    public void givePriority() {
        mHasPriority = true;
    }
}
