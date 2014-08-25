package com.ethanmad.engarde;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.ethanmad.engarde.R;

/**
 * Created by ethan on 7/28/14.
 */
public class CardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card);

        View v = findViewById(R.id.yellow_card);

        boolean red = getIntent().getBooleanExtra("red", false);

        if (red) {
            v.setBackgroundColor(Color.RED);
        } else {
            v.setBackgroundColor(Color.YELLOW);
        }
    }

    public void dismiss(View view) {
        finish();
    }
}
