package com.ethanmad.engarde;

/**
 * Created by ethan on 8/14/14.
 */
public class Fencer {
    private int score;
    private boolean hasYellowCard, hasRedCard;
    private boolean hasPriority;

    public int getScore() {
        return score;
    }
    public void addScore() {
        score++;
    }
    public void subtractScore() {
        score--;
    }

    public boolean hasYellowCard() {
        return hasYellowCard;
    }

    public boolean hasRedCard() {
        return hasRedCard;
    }

    public boolean hasPriority() {
        return hasPriority;
    }
}
