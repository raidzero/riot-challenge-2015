package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.global.ApiUtility;

import java.io.IOException;
import java.util.Random;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements ApiUtility.ApiCallback {
    private final String tag = "MainActivity";

    public static Drawable bgDrawable;
    private ApiUtility mApiUtility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.match_results_layout);

        setLoadingScreenDrawable();
    }

    @Override
    public void onResume() {
        super.onResume();

        mApiUtility = ApiUtility.getInstance(this);
        mApiUtility.startProcessing();
    }

    @Override
    public void onFirstMatchProcessed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, MatchResultsView.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivityForResult(i, 0);
            }
        });
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data) {
        mApiUtility.shutDown();
        finish();
    }

    @Override
    public void onBackPressed() {
        mApiUtility.shutDown();
        finish();
    }

    @Override
    public void onAllMatchesProcessed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "ALL PROCESSING COMPLETE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoadingScreenDrawable() {
        int randomNum = new Random().nextInt((4 - 1) + 1) + 1;
        try {
            bgDrawable = Drawable.createFromStream(getAssets().open("loading" + randomNum + ".jpg"), null);
            getWindow().getDecorView().setBackground(bgDrawable);
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }
}
