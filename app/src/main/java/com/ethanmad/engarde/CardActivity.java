package com.ethanmad.engarde;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.ethanmad.engarde.R;

/**
 * Created by ethan on 7/28/14.
 */
public class CardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int penaltyColor = getIntent().getBooleanExtra("red", false) ? Color.RED : Color.YELLOW;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(penaltyColor);
        setContentView(R.layout.card);

        View card = findViewById(R.id.yellow_card);
        card.setBackgroundColor(penaltyColor);

        }
    }

    public void dismiss(View view) {
        finish();
    }
}
