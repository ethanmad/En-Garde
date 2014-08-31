package com.ethanmad.engarde;

/**
 * Created by ethan on 8/14/14.
 */
public class Fencer extends Object {
    private String mName = "", mTeam = "";
    private int mNumber = -1, mScore = 0, mIndicator = 0, mNumWins = 0, mNumLosses = 0;
    private boolean mHasYellowCard = false, mHasRedCard = false, mHasPriority = false, mWinner = false;

    // POOL METHODS
    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setTeam(String team) {
        mTeam = team;
    }

    public String getTeam() {
        return mTeam;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public int getNumber() {
        return mNumber;
    }

    public void updateIndicator(int touchesReceived) {
        mIndicator = mIndicator + this.getScore() - touchesReceived;
    }

    public int getIndicator() {
        return mIndicator;
    }

    // BOUT METHODS
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

    public void resetPriority() {
        mHasPriority = false;
    }

    public void makeWinner(int touchesReceived) {
        mWinner = true;
        mNumWins++;
        updateIndicator(touchesReceived);
    }

    public void takeWinner (int touchesReceived) {
        mWinner = false;
        mNumWins--;
        updateIndicator(touchesReceived - this.getScore());
    }

    public boolean isWinner() {
        return mWinner;
    }

    public void makeLoser(int touchesReceived) {
        mWinner = false;
        mNumLosses++;
        updateIndicator(touchesReceived);
    }
}
