package com.ethanmad.engarde;

/**
 * Created by ethan on 8/14/14.
 */
public class Pool extends Object {
    private int numFencers;
    private fencer[] pool = new fencer[numFencers];
    private String teams = new String[numFencers];
    private int[] order  = new int[numFencers];

    private void findTeammates() {
        if (numFencers > 0) {
            currentTeam = teams[0];
            for(team : teams) {
            
            }
        }
    public void createPool(int numFencers) {
        switch (numFencers) {
            case 5:
	    	   
                break;
            case 6:
                break;
            case 7:
                break;
	
        }
    }
}
