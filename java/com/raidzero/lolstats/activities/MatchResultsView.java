package com.raidzero.lolstats.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.raidzero.lolstats.global.AppHelper;
import com.raidzero.lolstats.global.Common;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by raidzero on 4/7/15.
 */
public class MatchResultsView extends Activity {
    private static final String tag = "MatchResultsView";

    private AppHelper mHelper;
    private ArrayList<Champion> mChampionsTeam1 = new ArrayList<>();
    private ArrayList<Champion> mChampionsTeam2 = new ArrayList<>();
    private Match mMatch;
    private int mPortraitsDownloaded;

    private LinearLayout mContainerTeam1, mContainerTeam2;
    private ImageView versusView;
    private TextView mTeam1StatsView, mTeam2StatsView;

    private String mTeam1StatsStr, mTeam2StatsStr;

    private int mWinningTeamId = 0;

    private ImageDownloadListener mImageDownloadListener = new ImageDownloadListener() {
        @Override
        public void onImageDownloaded(final String location, final ImageView imageView,
                                      final TextView textView, final String summonerName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Drawable portrait = Drawable.createFromPath(location);
                    imageView.setImageDrawable(portrait);
                    textView.setText(summonerName);

                    if (++mPortraitsDownloaded == 10) {
                        setBackgroundChampion();
                    }
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = ((AppHelper) getApplicationContext());

        setContentView(R.layout.match_results_layout);
    }

    @Override
    public void onResume() {
        super.onResume();

        mMatch = mHelper.getCurrentMatch();

        mContainerTeam1 = (LinearLayout) findViewById(R.id.team1Container);
        mContainerTeam2 = (LinearLayout) findViewById(R.id.team2Container);
        versusView = (ImageView) findViewById(R.id.versus);

        mTeam1StatsView = (TextView) findViewById(R.id.txt_team1Stats);
        mTeam2StatsView = (TextView) findViewById(R.id.txt_team2Stats);

        processChampions();
    }

    private void processChampions() {
        Champion[] matchChampions = mHelper.getMatchChampions();
        Participant[] participants = mMatch.participants;

        // team stats
        int team1Kills = 0; int team1Deaths = 0; int team1Assists = 0;
        int team2Kills = 0; int team2Deaths = 0; int team2Assists = 0;


        for (int i = 0; i < participants.length; i++) {
            Champion c = matchChampions[i];
            Participant p = participants[i];

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
                        getWindow().getDecorView().setBackground(bgImg);

                        animateVersus();
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
            Log.d(tag, "winning summoner: " + winningChampion.summonerName);

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
                    // dont care really
                }

                animateTeamsOut();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        versusView.startAnimation(animation);
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
        private URL mRequestUrl;
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

            try {
                mRequestUrl = new URL(mFileUrlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

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

            try {
                URLConnection urlConnection = mRequestUrl.openConnection();
                urlConnection.setUseCaches(true);
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(5000);

                int current;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(mDownloadedFile);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();

                return mDownloadedFile.getPath();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mListener.onImageDownloaded(result, mImageView, mSummonerView, mSummonerName);
        }
    }
}
