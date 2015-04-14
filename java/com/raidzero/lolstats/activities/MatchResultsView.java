package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.Participant;
import com.raidzero.lolstats.global.AnimationUtility;
import com.raidzero.lolstats.global.ApiUtility;
import com.raidzero.lolstats.global.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by raidzero on 4/7/15.
 */
public class MatchResultsView extends Activity implements ApiUtility.ApiCallback, Animation.AnimationListener {
    private static final String tag = "MatchResultsView";

    private Handler mRefreshHandler = new Handler();

    private ApiUtility mApiUtility;
    private ArrayList<Champion> mChampionsTeam1 = new ArrayList<>();
    private ArrayList<Champion> mChampionsTeam2 = new ArrayList<>();
    private Match mMatch;
    private int mPortraitsDownloaded;

    private ImageView mBackground;
    private LinearLayout mLoadingView;
    private LinearLayout mContainerTeam1, mContainerTeam2;
    private ImageView mVersusView;
    private TextView mTeam1StatsView, mTeam2StatsView;

    private boolean mKeepLoading = true;
    private String mTeam1StatsStr, mTeam2StatsStr;

    private int mWinningTeamId = 0;

    // view dimensions
    private int mScreenHeight, mScreenWidth;

    // animations
    private TranslateAnimation mAnimTeam1In, mAnimTeam2In, mAnimTeam1Out , mAnimTeam2Out;
    private TranslateAnimation mAnimTeam1Up, mAnimTeam2Down;
    private TranslateAnimation mAnimMvpIn, mAnimMvpOut;
    private ScaleAnimation mAnimVersusIn, mAnimVersusOut;
    private AlphaAnimation mAnimStatsIn, mAnimStatsOut;
    private AlphaAnimation mAnimBgIn, mAnimBgOut;

    // mvp stat views
    private LinearLayout mMvpView;
    private TextView mvpName, mvpKda, mvpFirstBlood, mvpTowerKills, mvpInhibitorKills,
            mvpDamage, mvpKillingSpree, mvpDoubleKills, mvpTripleKills, mvpQuadraKills, mvpPentaKills,
            mvpGoldView;

    private int mvpDisplayTime;
    private boolean mAutoAdvance;
    private ImageView mSettingsButtonView, mNextButtonView;

    private ImageDownloadListener mImageDownloadListener = new ImageDownloadListener() {
        @Override
        public void onImageDownloaded(final String location, final ImageView imageView,
                                      final TextView textView, final String summonerName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Drawable portrait;

                    if (!location.equals("unknown")) {
                        portrait = Drawable.createFromPath(location);
                    } else {
                        portrait = getResources().getDrawable(R.drawable.unknown);
                    }

                    imageView.setImageDrawable(portrait);
                    textView.setText(summonerName);

                    if (++mPortraitsDownloaded == 10) {
                        animateTeamsInOut(true);

                        // reset or else the background will never change again
                        mPortraitsDownloaded = 0;
                    }
                }
            });
        }
    };

    private Runnable getMatchAndDisplay = new Runnable() {
        @Override
        public void run() {
            mMatch = mApiUtility.getNextMatch();

            mChampionsTeam1.clear();
            mChampionsTeam2.clear();

            mContainerTeam1.removeAllViews();
            mContainerTeam2.removeAllViews();

            processChampions();
        }

    };

    public void onNextButtonClick(View v) {
        mNextButtonView.setVisibility(View.GONE);

        animateMvpInOut(false);
        animateBgInOut(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiUtility = ApiUtility.getInstance(this);
        setContentView(R.layout.match_results_layout);

        Drawable mainBg = MainActivity.bgDrawable;
        getWindow().getDecorView().setBackground(mainBg);

        mBackground = (ImageView) findViewById(R.id.match_background);
        mLoadingView = (LinearLayout) findViewById(R.id.loadingView);
        mContainerTeam1 = (LinearLayout) findViewById(R.id.team1Container);
        mContainerTeam2 = (LinearLayout) findViewById(R.id.team2Container);
        mVersusView = (ImageView) findViewById(R.id.versus);

        mTeam1StatsView = (TextView) findViewById(R.id.txt_team1Stats);
        mTeam2StatsView = (TextView) findViewById(R.id.txt_team2Stats);

        // mvp views
        mMvpView = (LinearLayout) findViewById(R.id.mvpContainer);
        mvpName = (TextView) findViewById(R.id.mvp_name);
        mvpKda = (TextView) findViewById(R.id.mvp_kda);
        mvpFirstBlood = (TextView) findViewById(R.id.mvp_firstBloodView);
        mvpTowerKills = (TextView) findViewById(R.id.mvp_towerKillView);
        mvpInhibitorKills = (TextView) findViewById(R.id.mvp_inhibitorKillView);
        mvpDamage = (TextView) findViewById(R.id.mvp_damageView);
        mvpKillingSpree = (TextView) findViewById(R.id.mvp_killingSpreeView);
        mvpDoubleKills = (TextView) findViewById(R.id.mvp_doubleKillView);
        mvpTripleKills = (TextView) findViewById(R.id.mvp_tripleKillView);
        mvpQuadraKills = (TextView) findViewById(R.id.mvp_quadraKillView);
        mvpPentaKills = (TextView) findViewById(R.id.mvp_pentaKillView);
        mvpGoldView = (TextView) findViewById(R.id.mvp_goldView);

        mSettingsButtonView = (ImageView) findViewById(R.id.settings_button);
        mSettingsButtonView.setVisibility(View.GONE);
        mNextButtonView = (ImageView) findViewById(R.id.nextButton);
        mNextButtonView.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mScreenWidth = getWindow().getDecorView().getWidth();
        mScreenHeight = getWindow().getDecorView().getHeight();

        /**
         * make animations
         */
        // teams in - 500ms
        mAnimTeam1In = AnimationUtility.getTranslation(mScreenWidth, 0, 0, 0, 500, this);
        mAnimTeam2In = AnimationUtility.getTranslation(-mScreenWidth, 0, 0, 0, 500, this);

        // versus in/out - 400/250ms
        mAnimVersusIn = AnimationUtility.getScale(3.0f, 1.0f, 3.0f, 1.0f, 400, this);
        mAnimVersusOut = AnimationUtility.getScale(1.0f, 0.0f, 1.0f, 0.0f, 250, this);

        // teams up/down - 250ms
        mAnimTeam1Up = AnimationUtility.getTranslation(0, 0, 0, -100, 250, this);
        mAnimTeam2Down = AnimationUtility.getTranslation(0, 0, 0, 100, 250, this);

        // stats in - 2000ms
        mAnimStatsIn = AnimationUtility.getAlpha(0.0f, 1.0f, 2000, this);
        mAnimStatsOut = AnimationUtility.getAlpha(1.0f, 0.0f, 2000, this);

        // teams out - 500ms
        mAnimTeam1Out = AnimationUtility.getTranslation(0, -mScreenWidth, -100, -100, 500, this);
        mAnimTeam2Out = AnimationUtility.getTranslation(0, mScreenWidth, 100, 100, 500, this);

        // mvp in/out
        mAnimMvpIn = AnimationUtility.getTranslation(0, 0, mScreenHeight, 0, 500, this);
        mAnimMvpOut = AnimationUtility.getTranslation(0, 0, 0, -mScreenHeight, 500, this);

        // bg in/out = 2000/500ms
        mAnimBgIn = AnimationUtility.getAlpha(0.0f, 1.0f, 2000, this);
        mAnimBgOut = AnimationUtility.getAlpha(1.0f, 0.0f, 500, this);

        animateLoading(false);

        // start the runnable right away
        mRefreshHandler.postDelayed(getMatchAndDisplay, 0);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            mvpDisplayTime = pref.getInt("pref_mvp_delay", 4) * 1000;
        } catch (NumberFormatException e) {
            // just dont crash.
            mvpDisplayTime = 4000;
        }

        mAutoAdvance = pref.getBoolean("pref_auto_advance", true);

        Log.d(tag, "MVP delay: " + mvpDisplayTime);
    }

    private void processChampions() {
        Participant[] participants = mMatch.participants;

        mTeam1StatsView.setVisibility(View.GONE);
        mTeam2StatsView.setVisibility(View.GONE);

        // team stats
        int team1Kills = 0; int team1Deaths = 0; int team1Assists = 0;
        int team2Kills = 0; int team2Deaths = 0; int team2Assists = 0;

        for (int i = 0; i < participants.length; i++) {
            Participant p = participants[i];
            Champion c = p.champion;

            mMatch.participants[i].champion = c;

            if (participants[i].teamId == 100) {
                mChampionsTeam1.add(c);
                team1Deaths += p.deaths;
                team1Kills += p.totalKills;
                team1Assists += p.assists;

                if (p.winningTeam) {
                    mWinningTeamId = 100;
                }
            } else {
                mChampionsTeam2.add(c);
                team2Deaths += p.deaths;
                team2Kills += p.totalKills;
                team2Assists += p.assists;

                if (p.winningTeam) {
                    mWinningTeamId = 200;
                }
            }
        }

        mTeam1StatsStr = String.format("%d/%d/%d", team1Kills, team1Deaths, team1Assists);
        mTeam2StatsStr = String.format("%d/%d/%d", team2Kills, team2Deaths, team2Assists);

        if (mWinningTeamId == 200) {
            mTeam1StatsView.setTextColor(Color.GREEN);
            mTeam2StatsView.setTextColor(Color.RED);
        } else {
            mTeam1StatsView.setTextColor(Color.RED);
            mTeam2StatsView.setTextColor(Color.GREEN);
        }

        mTeam1StatsView.setText(mTeam1StatsStr);
        mTeam2StatsView.setText(mTeam2StatsStr);

        processViews(100);
        processViews(200);
    }

    private void processViews(int teamId) {
        ArrayList<Champion> champions;
        int champLayoutId;
        LinearLayout teamContainer;

        if (teamId == 100) {
            teamContainer = (LinearLayout) findViewById(R.id.team1Container);
            champLayoutId = R.layout.champion_layout_top;
            champions = mChampionsTeam1;
        } else {
            teamContainer = (LinearLayout) findViewById(R.id.team2Container);
            champLayoutId = R.layout.champion_layout_bottom;
            champions = mChampionsTeam2;
        }

        // now that the lists of champions is ready, lets start processing
        for (Champion c : champions) {
            String summonerName = c.name;

            // inflate a view
            View v = View.inflate(this, champLayoutId, null);
            ImageView portraitView = (ImageView) v.findViewById(R.id.img_champion_portrait);
            TextView summonerView = (TextView) v.findViewById(R.id.txt_summoner_name);

            teamContainer.addView(v);

            String portraitUrl = Common.CHAMPION_PORTRAIT_URL_PREFIX + c.getChampionPortaitPath();
            ImageDownloadTask portraitTask =
                    new ImageDownloadTask(portraitUrl, mImageDownloadListener,
                            portraitView, summonerView, summonerName);
            portraitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void setBackgroundChampion() {
        // pick the winner
        List<Participant> participants = Arrays.asList(mMatch.participants);
        Collections.sort(participants);

        ImageDownloadListener bgListener = new ImageDownloadListener() {
            @Override
            public void onImageDownloaded(final String location, ImageView imageView, TextView summonerView, String summonerName) {
                Log.d(tag, "setting background: " + location);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContainerTeam1.setVisibility(View.VISIBLE);
                        mContainerTeam2.setVisibility(View.VISIBLE);

                        Drawable bgImg = Drawable.createFromPath(location);
                        mBackground.setImageDrawable(bgImg);
                    }
                });
            }
        };

        Champion winningChampion = null;
        Participant mvp = null;
        for (int i = participants.size() - 1; i >= 0; i--) {
            if (participants.get(i).winningTeam) {
                mvp = participants.get(i);
                winningChampion = participants.get(i).champion;
                break;
            }
        }

        if (mvp != null) {
            // fill in the mvp view with these stats
            mvpName.setText(
                    String.format("%s, %s",
                            winningChampion.name, winningChampion.title));
            mvpKda.setText(
                    String.format(getResources().getString(R.string.mvp_kda),
                            mvp.totalKills, mvp.deaths, mvp.assists));
            mvpFirstBlood.setText(
                    String.format(getResources().getString(R.string.mvp_firstBlood),
                            Common.bool2str(mvp.firstBlood)));
            mvpTowerKills.setText(
                    String.format(getResources().getString(R.string.mvp_towerKills),
                            mvp.towerKills));
            mvpInhibitorKills.setText(
                    String.format(getResources().getString(R.string.mvp_inhibitorKills),
                            mvp.inhibitorKills));
            mvpDamage.setText(
                    String.format(getResources().getString(R.string.mvp_damageView),
                            new DecimalFormat("#,###").format(mvp.damageDealt),
                            new DecimalFormat("#,###").format(mvp.damageTaken)));
            mvpKillingSpree.setText(
                    String.format(getResources().getString(R.string.mvp_largestKillingSpree),
                            mvp.inhibitorKills));
            mvpDoubleKills.setText(
                    String.format(getResources().getString(R.string.mvp_doubleKills),
                            mvp.doubleKills));
            mvpTripleKills.setText(
                    String.format(getResources().getString(R.string.mvp_tripleKills),
                            mvp.tripleKills));
            mvpQuadraKills.setText(
                    String.format(getResources().getString(R.string.mvp_quadraKills),
                            mvp.quadraKills));
            mvpPentaKills.setText(
                    String.format(getResources().getString(R.string.mvp_pentaKills),
                            mvp.pentaKills));
            mvpGoldView.setText(
                    String.format(getResources().getString(R.string.mvp_goldView),
                            new DecimalFormat("#,###").format(mvp.goldEarned),
                            new DecimalFormat("#,###").format(mvp.goldSpent)));
        }

        if (winningChampion != null) {

            // start an asynctask to download & display this image
            Log.d(tag, "winning summoner: " + winningChampion.name);

            ImageDownloadTask task =
                    new ImageDownloadTask(
                            Common.CHAMPION_SKIN_URL_PREFIX
                                    + winningChampion.getChampionBackgroundPath(),
                            bgListener, null, null, null);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // something wrong with the matrix. every game has a winner
        }
    }

    @Override
    public void onGoBackInTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingView.getVisibility() == View.GONE) {
                    animateLoading(true);
                }
            }
        });
    }

    @Override
    public void onFirstMatchProcessed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingView.getVisibility() == View.VISIBLE) {
                    animateLoading(false);
                }
            }
        });
    }

    @Override
    public void onAllMatchesProcessed() {

    }

    @Override
    public void onError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mKeepLoading = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(MatchResultsView.this);

                builder.setTitle(getResources().getString(R.string.network_error_title));
                builder.setMessage(getResources().getString(R.string.network_error_message));

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setLoadingScreenDrawable() {
        int randomNum = new Random().nextInt((7 - 1) + 1) + 1;
        Log.d(tag, "setting random background image");

        try {
            Drawable bgDrawable = Drawable.createFromStream(getAssets().open("loading" + randomNum + ".jpg"), null);
            getWindow().getDecorView().setBackground(bgDrawable);
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }

    /**
     * animation listener callbacks
     */
    @Override
    public void onAnimationStart(Animation animation) {
        // never used
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == mAnimTeam1In) {
            // start versus animation
            animateVersusInOut(true);
        }

        if (animation == mAnimVersusIn) {
            // start teams up animation
            animateTeamsUp();
            animateVersusInOut(false);
        }

        if (animation == mAnimVersusOut) {
            // start stats in animation
            animateStatsInOut(true);
        }

        if (animation == mAnimStatsIn) {
            setBackgroundChampion();
            // fade out stats while sliding out teams and fading in new background
            animateStatsInOut(false);
            animateBgInOut(true);
        }

        if (animation == mAnimStatsOut) {
            mTeam1StatsView.setVisibility(View.GONE);
            mTeam2StatsView.setVisibility(View.GONE);
        }

        if (animation == mAnimBgIn) {
            animateTeamsInOut(false);
            animateMvpInOut(true);
            setLoadingScreenDrawable();
        }

        if (animation == mAnimMvpIn) {
            if (mAutoAdvance) {
                try {
                    Thread.sleep(mvpDisplayTime);
                } catch (InterruptedException e) {
                    finish();
                }


                animateMvpInOut(false);
                animateBgInOut(false);
            } else {
                mNextButtonView.setVisibility(View.VISIBLE);
            }
        }

        if (animation == mAnimMvpOut) {
            if (mKeepLoading) {
                mRefreshHandler.post(getMatchAndDisplay);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // never used
    }

    /**
     * animation helpers
     */
    private void animateTeamsInOut(boolean in) {
        mContainerTeam1.setVisibility(View.VISIBLE);
        mContainerTeam2.setVisibility(View.VISIBLE);

        if (in) {
            mContainerTeam1.startAnimation(mAnimTeam1In);
            mContainerTeam2.startAnimation(mAnimTeam2In);
        } else {
            mContainerTeam1.startAnimation(mAnimTeam1Out);
            mContainerTeam2.startAnimation(mAnimTeam2Out);
        }
    }

    private void animateTeamsUp() {
        mContainerTeam1.startAnimation(mAnimTeam1Up);
        mContainerTeam2.startAnimation(mAnimTeam2Down);
    }

    private void animateVersusInOut(boolean in) {
        if (in) {
            mVersusView.setVisibility(View.VISIBLE);
            mVersusView.startAnimation(mAnimVersusIn);
        } else {
            mVersusView.startAnimation(mAnimVersusOut);
        }
    }

    private void animateStatsInOut(boolean in) {
        if (in) {
            mTeam1StatsView.setVisibility(View.VISIBLE);
            mTeam2StatsView.setVisibility(View.VISIBLE);

            mTeam1StatsView.startAnimation(mAnimStatsIn);
            mTeam2StatsView.startAnimation(mAnimStatsIn);
        } else {
            mTeam1StatsView.startAnimation(mAnimStatsOut);
            mTeam2StatsView.startAnimation(mAnimStatsOut);
        }
    }

    private void animateBgInOut(boolean in) {
        mBackground.setVisibility(View.VISIBLE);
        if (in) {
            mBackground.startAnimation(mAnimBgIn);
        } else {
            mBackground.startAnimation(mAnimBgOut);
        }
    }

    private void animateMvpInOut(boolean in) {
        if (in) {
            mMvpView.setVisibility(View.VISIBLE);
            mMvpView.startAnimation(mAnimMvpIn);
        } else {
            mMvpView.startAnimation(mAnimMvpOut);
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

    /**
     * ImageDownloadListener
     */
    public interface ImageDownloadListener {
        void onImageDownloaded(
                final String location, final ImageView imageView,
                final TextView summonerView, final String summonerName);
    }

    /**
     * AsyncTask for downloading images
     */
    private class ImageDownloadTask extends AsyncTask<Void, Void, String> {

        private ImageDownloadListener mListener;
        private String mFileUrlString;
        private File mDownloadedFile;

        private TextView mSummonerView;
        private ImageView mImageView;
        private String mSummonerName;

        public ImageDownloadTask(
                String strFileUrl, ImageDownloadListener listener,
                ImageView imageView, TextView summonerView, String summonerName) {
            mListener = listener;
            mFileUrlString = strFileUrl;
            mImageView = imageView;
            mSummonerView = summonerView;
            mSummonerName = summonerName;

            // pull off filename
            String fileName =
                    mFileUrlString.substring(
                            mFileUrlString.lastIndexOf('/') + 1, mFileUrlString.length());
            File destDir = getCacheDir();
            mDownloadedFile = new File(destDir, fileName);
        }

        @Override
        protected String doInBackground(Void... params) {
            //Log.d(tag, "doInBackground(" + mFileUrlString + ")");

            if (!mDownloadedFile.exists()) {
                try {
                    InputStream is = (InputStream) new URL(mFileUrlString).getContent();

                    FileOutputStream fos = new FileOutputStream(mDownloadedFile);

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }

                    return mDownloadedFile.getPath();
                } catch (FileNotFoundException e) {
                    return "unknown";
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                // already have this file.
                return mDownloadedFile.getPath();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mListener.onImageDownloaded(result, mImageView, mSummonerView, mSummonerName);
        }
    }
}
