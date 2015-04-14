package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.global.ApiUtility;
import com.raidzero.lolstats.global.Common;

import java.io.IOException;
import java.util.Random;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements ApiUtility.ApiCallback {
    private final String tag = "MainActivity";

    private LinearLayout mLoadingView;
    public static Drawable bgDrawable;
    private ApiUtility mApiUtility;
    private boolean mDisplayMatch = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.match_results_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLoadingView = (LinearLayout) findViewById(R.id.loadingView);

        setLoadingScreenDrawable();
        animateLoading(true);
    }

    public void onSettingsClick(View v) {
        mDisplayMatch = false;
        startActivityForResult(new Intent(this, SettingsActivity.class), Common.REQUEST_CODE_SETTINGS);
    }

    @Override
    public void onResume() {
        super.onResume();

        mApiUtility = ApiUtility.getInstance(this);
        mApiUtility.startProcessing();
    }

    private Runnable startMatchView = new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent(MainActivity.this, MatchResultsView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivityForResult(i, Common.REQUEST_CODE_MATCHES);
        }
    };

    @Override
    public void onGoBackInTime() {
        // dont care.
    }

    @Override
    public void onFirstMatchProcessed() {
        if (mDisplayMatch) {
            runOnUiThread(startMatchView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.REQUEST_CODE_MATCHES:
                quit();
                break;
            case Common.REQUEST_CODE_SETTINGS:
                if (data.getBooleanExtra("restartApi", false)) {
                    mApiUtility.shutDown();
                    mApiUtility.startProcessing();
                }

                mDisplayMatch = true;
                onFirstMatchProcessed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    private void quit() {
        if (mApiUtility != null) {
            mApiUtility.shutDown();
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
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

    @Override
    public void onError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(getResources().getString(R.string.network_error_title));
                builder.setMessage(getResources().getString(R.string.network_error_message));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quit();
                    }
                });

                builder.show();
            }
        });
    }

    private void setLoadingScreenDrawable() {
        int randomNum = new Random().nextInt((7 - 1) + 1) + 1;
        try {
            bgDrawable = Drawable.createFromStream(getAssets().open("loading" + randomNum + ".jpg"), null);
            getWindow().getDecorView().setBackground(bgDrawable);
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }

    private void animateLoading(final boolean in) {
        float from, to;

        if (in) {
            mLoadingView.setVisibility(View.VISIBLE);
            from = 0.0f; to = 1.0f;
        } else {
            from = 1.0f; to = 0.0f;
        }

        AlphaAnimation anim = new AlphaAnimation(from, to);
        anim.setDuration(750);
        anim.setFillAfter(true);

        mLoadingView.startAnimation(anim);
    }
}
