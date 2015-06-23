package com.ethanmad.engarde;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.ethanmad.engarde.R;

/**
 * Created by ethan on 7/23/14.
 */
public class CardAlertFragment extends DialogFragment {
    int mViewId;
    CardAlertListener mListener;

    public static CardAlertFragment newInstance(View view) {
        CardAlertFragment cardAlertFragment = new CardAlertFragment();

        Bundle args = new Bundle();
        args.putInt("view.getId()", view.getId());
        cardAlertFragment.setArguments(args);

        return cardAlertFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewId = getArguments().getInt("view.getId()", mViewId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int whichArray = -1;
        final String[][] text = new String[][]{new String[] {getResources().getString(
                R.string.yellow_card_dialog), "0"}, new String[]{getResources().getString(R.string.red_card_dialog), "1"}};

        switch (mViewId) {
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
                        mainActivity.giveCard(which, Integer.parseInt(text[index][1]));
                    }
                });

        return builder.create();
    }

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
        switch (mViewId) {
            case R.id.yellowCardButton:
                return "" + R.string.yellow_card_dialog;
            case R.id.redCardButton:
                return "" + R.string.red_card_dialog;
        }

        return "Neither yellow nor red card.";
    }

    public interface CardAlertListener {
        public void onDialogClick(DialogFragment dialogFragment, int fencer, int cardType);
//        public void onDialogClickBottom(DialogFragment dialogFragment, int fencer, int cardType);
    }
}
