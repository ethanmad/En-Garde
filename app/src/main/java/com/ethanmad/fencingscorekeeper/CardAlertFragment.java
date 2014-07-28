package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

/**
 * Created by ethan on 7/23/14.
 */
public class CardAlertFragment extends DialogFragment {
    View view;

    public CardAlertFragment() {
    }

    public CardAlertFragment(View view) {
        this.view = view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int whichArray = -1;
        final String[][] text = new String[][] {new String[] {"yellow_card_dialog ", "0"}, new String[] {"red_card_dialog", "1"}};

        switch(view.getId()) {
            case R.id.yellowCardButton:
                whichArray = 0;
                break;
            case R.id.redCardButton:
                whichArray = 1;
                break;
        }

        final int index = whichArray;
        builder.setTitle(text[whichArray][0])
                .setItems(R.array.fencer_names, new DialogInterface.OnClickListener() { //TODO: switch to ListAdapter to allow for dynamic names
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.giveCard(Integer.parseInt(text[index][1]), which);
                    }
                });

        return builder.create();
    }

    public interface CardAlertListener {
        public void onDialogClick(DialogFragment dialogFragment, int fencer, int cardType);
//        public void onDialogClickBottom(DialogFragment dialogFragment, int fencer, int cardType);
    }

    CardAlertListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the CardAlertListener so we can send events to the host
            mListener = (CardAlertListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                + " must implement CardAlertListener");
        }
    }

    public String getTitle() {
        switch (view.getId()) {
            case R.id.yellowCardButton:
                return "" + R.string.yellow_card_dialog;
            case R.id.redCardButton:
                return "" + R.string.red_card_dialog;
        }

        return "Neither yellow nor red card.";
    }
}
