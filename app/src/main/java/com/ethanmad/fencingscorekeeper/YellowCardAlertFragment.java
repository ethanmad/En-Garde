package com.ethanmad.fencingscorekeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by ethan on 7/23/14.
 */
public class YellowCardAlertFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_fencer_yellow_card)
                .setItems(R.array.fencer_names, new DialogInterface.OnClickListener() { //TODO: switch to ListAdapter to allow for dynamic names
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
    }

    public interface YellowCardAlertListener {
        public void onDialogClickTop(DialogFragment dialogFragment);
        public void onDialogClickBottom(DialogFragment dialogFragment);
    }

    YellowCardAlertListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the YellowCardAlertListener so we can send events to the host
            mListener = (YellowCardAlertListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                + " must implement YellowCardAlertListener");
        }
    }

}
