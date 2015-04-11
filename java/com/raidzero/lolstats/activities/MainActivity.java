package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
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

    private LinearLayout mLoadingView;
    public static Drawable bgDrawable;
    private ApiUtility mApiUtility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.match_results_layout);

        mLoadingView = (LinearLayout) findViewById(R.id.loadingView);
        setLoadingScreenDrawable();
        animateLoading(true);
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

            startActivityForResult(i, 0);
        }
    };

    @Override
    public void onFirstMatchProcessed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(startMatchView);
            }
        });
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data) {
        quit();
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    private void quit() {
        mApiUtility.shutDown();
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

                builder.setTitle("Network Error");
                builder.setMessage("An error has occurred. Please verify you are connected to the internet and try again.");
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
