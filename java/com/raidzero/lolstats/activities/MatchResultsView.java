package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import com.raidzero.lolstats.global.ApiUtility;
import com.raidzero.lolstats.global.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by raidzero on 4/7/15.
 */
public class MatchResultsView extends Activity implements ApiUtility.ApiCallback {
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
    private ImageView versusView;
    private TextView mTeam1StatsView, mTeam2StatsView;

    private boolean mKeepLoading = true;
    private String mTeam1StatsStr, mTeam2StatsStr;


    private int mWinningTeamId = 0;

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
                        animateTeamsIn();
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
        versusView = (ImageView) findViewById(R.id.versus);

        mTeam1StatsView = (TextView) findViewById(R.id.txt_team1Stats);
        mTeam2StatsView = (TextView) findViewById(R.id.txt_team2Stats);

        animateLoading(false);

        // start the runnable right away
        mRefreshHandler.postDelayed(getMatchAndDisplay, 0);
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

        if (mWinningTeamId == 100) {
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

        for (int i = participants.size() - 1; i >= 0; i--) {
            if (participants.get(i).winningTeam) {
                winningChampion = participants.get(i).champion;
                break;
            }
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
     * animates the versus view in
     */
    private void animateVersus() {
        versusView.setVisibility(View.VISIBLE);

        ScaleAnimation animation = new ScaleAnimation(3.0f, 1.0f, 3.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(400);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore
                }

                animateTeamsOut();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        versusView.startAnimation(animation);
    }

    /**
     * animates team containers in from off the screen
     */
    private void animateTeamsIn() {
        int screenWidth = getWindow().getDecorView().getWidth();

        TranslateAnimation animFromRight = new TranslateAnimation(screenWidth, 0, 0, 0);
        TranslateAnimation animFromLeft = new TranslateAnimation(-screenWidth, 0, 0, 0);

        animFromRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animateVersus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animFromLeft.setDuration(500);
        animFromRight.setDuration(500);

        animFromLeft.setFillAfter(true);
        animFromRight.setFillAfter(true);

        mContainerTeam1.setVisibility(View.VISIBLE);
        mContainerTeam1.startAnimation(animFromRight);

        mContainerTeam2.setVisibility(View.VISIBLE);
        mContainerTeam2.startAnimation(animFromLeft);
    }

    /**
     * animates team containers off the screen
     */
    private void animateTeamsOff() {
        int screenWidth = getWindow().getDecorView().getWidth();

        TranslateAnimation animOffRight = new TranslateAnimation(0, -screenWidth, -100, -100);
        TranslateAnimation animOffLeft = new TranslateAnimation(0, screenWidth, 100, 100);

        animOffRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(tag, "animations over");

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animOffLeft.setDuration(500);
        animOffRight.setDuration(500);

        animOffLeft.setFillAfter(true);
        animOffRight.setFillAfter(true);

        mContainerTeam1.setVisibility(View.VISIBLE);
        mContainerTeam1.startAnimation(animOffRight);

        mContainerTeam2.setVisibility(View.VISIBLE);
        mContainerTeam2.startAnimation(animOffLeft);
    }

    private void animateBackgroundOut() {
        AlphaAnimation bgOut = new AlphaAnimation(1.0f, 0.0f);
        bgOut.setFillAfter(true);
        bgOut.setDuration(500);

        bgOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mKeepLoading) {
                    mRefreshHandler.post(getMatchAndDisplay); // get a new match to show once all animations have finished
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBackground.startAnimation(bgOut);
    }

    private void animateBackgroundIn() {
        AlphaAnimation bgOut = new AlphaAnimation(0.0f, 1.0f);
        bgOut.setFillAfter(true);
        bgOut.setDuration(2000);

        mBackground.startAnimation(bgOut);
    }

    private void animateStatsOff() {
        AlphaAnimation statsAnimation = new AlphaAnimation(1.0f, 0.0f);
        statsAnimation.setDuration(500);
        statsAnimation.setFillAfter(true);

        mTeam1StatsView.startAnimation(statsAnimation);
        mTeam2StatsView.startAnimation(statsAnimation);
    }
    private void animateTeamsOut() {
        TranslateAnimation animationTop = new TranslateAnimation(0, 0, 0, -100);
        TranslateAnimation animationBottom = new TranslateAnimation(0, 0, 0, 100);

        ScaleAnimation versusAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        mTeam1StatsView.setVisibility(View.VISIBLE);
        mTeam2StatsView.setVisibility(View.VISIBLE);

        AlphaAnimation statsAnimation = new AlphaAnimation(0.0f, 1.0f);
        statsAnimation.setDuration(2000);
        statsAnimation.setFillAfter(true);

        statsAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setBackgroundChampion();
                animateBackgroundIn();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setLoadingScreenDrawable();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // dont care
                }

                animateBackgroundOut();
                animateTeamsOff();
                animateStatsOff();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        versusAnimation.setDuration(250);
        versusAnimation.setFillAfter(true);

        animationBottom.setDuration(250);
        animationTop.setDuration(250);

        versusView.startAnimation(versusAnimation);
        animationBottom.setFillAfter(true);
        animationTop.setFillAfter(true);

        mTeam1StatsView.startAnimation(statsAnimation);
        mTeam2StatsView.startAnimation(statsAnimation);

        mContainerTeam1.startAnimation(animationTop);
        mContainerTeam2.startAnimation(animationBottom);
    }

    @Override
    public void onFirstMatchProcessed() {

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

                builder.setTitle("Network Error");
                builder.setMessage("An error has occurred. Please verify you are connected to the internet and try again.");
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
