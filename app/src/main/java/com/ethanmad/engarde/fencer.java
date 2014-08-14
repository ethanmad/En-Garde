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
    public void giveYellowCard() {
        hasYellowCard = true;
    }
    public void takeYellowCard() {
        hasYellowCard = false;
    }

    public boolean hasRedCard() {
        return hasRedCard;
    }
    public void giveRedCard() {
        hasRedCard = true;
    }
    public void takeRedCard() {
        hasRedCard = false;
    }

    public boolean hasPriority() {
        return hasPriority;
    }
    public void givePriority() {
        hasPriority = true;
    }
}
