package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
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

    private ImageView mSettingsView;
    private LinearLayout mLoadingView;
    public static Drawable bgDrawable;
    private ApiUtility mApiUtility;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.match_results_layout);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSettingsView = (ImageView) findViewById(R.id.settings_button);
        mLoadingView = (LinearLayout) findViewById(R.id.loadingView);

        setLoadingScreenDrawable();
    }

    private void doFirstRun() {
        // not anymore...
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("firstRun", false);
        editor.apply();

        // show nice dialog asking if the user would like to configure the app
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.first_run_title));
        builder.setMessage(getString(R.string.first_run_message));

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onFirstRunExit(true);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onFirstRunExit(false);
            }
        });

        builder.show();
    }

    private void onFirstRunExit(boolean goToSettings) {
        if (goToSettings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), Common.REQUEST_CODE_SETTINGS);
        } else {
            startApi();
        }
    }

    public void onSettingsClick(View v) {
        mApiUtility.shutDown();
        startActivityForResult(new Intent(this, SettingsActivity.class), Common.REQUEST_CODE_SETTINGS);
    }

    private void startApi() {
        animateLoading(true);
        mApiUtility = ApiUtility.getInstance(this);
        mApiUtility.startProcessing();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mPrefs.getBoolean("firstRun", true)) {
            mSettingsView.setVisibility(View.GONE);
            doFirstRun();
        } else {
            startApi();
        }
    }

    private Runnable startMatchView = new Runnable() {
        @Override
        public void run() {
            animateLoading(false);
            Intent i = new Intent(MainActivity.this, MatchResultsView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivityForResult(i, Common.REQUEST_CODE_MATCHES);
        }
    };

    @Override
    public void onTimeTravel() {
        // dont care.
    }

    @Override
    public void onFirstMatchProcessed() {
        try {
            // give the user at least one second to get to settings
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            quit();
        }

        runOnUiThread(startMatchView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.REQUEST_CODE_MATCHES:
                quit();
                break;
            case Common.REQUEST_CODE_SETTINGS:
                startApi();
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
