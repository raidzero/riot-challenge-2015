package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.adapters.ChampionAdapter;
import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.Participant;
import com.raidzero.lolstats.global.AppHelper;

import java.util.ArrayList;
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
    private ChampionAdapter mChampionAdapter;

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

        processChampions();

        addChampionsToView(0);
        addChampionsToView(1);
    }

    private void processChampions() {
        Participant[] participants = mMatch.participants;
        Champion[] matchChampions = mHelper.getMatchChampions();

        for (int i = 0; i < participants.length; i++) {
            Champion c = matchChampions[i];
            c.summonerName = participants[i].summoner.name;
            if (participants[i].teamId == 100) {
                mChampionsTeam1.add(c);
            } else {
                mChampionsTeam2.add(c);
            }
        }
    }

    private void addChampionsToView(int team) {
        ArrayList<Champion> champions;
        LinearLayout teamContainer;
        int champLayout;

        if (team == 0) {
            champions = mChampionsTeam1;
            teamContainer = (LinearLayout) findViewById(R.id.team1Container);
            champLayout = R.layout.champion_layout_top;
        } else {
            champions = mChampionsTeam2;
            teamContainer = (LinearLayout) findViewById(R.id.team2Container);
            champLayout = R.layout.champion_layout_bottom;
        }

        for (Champion c : champions) {
            View v = getLayoutInflater().inflate(champLayout, null);
            TextView cName = (TextView) v.findViewById(R.id.txt_summoner_name);
            ImageView portraitView = (ImageView) v.findViewById(R.id.img_champion_portrait);

            cName.setText(c.summonerName);

            String fgPath = getCacheDir() + c.getChampionPortaitPath();
            Drawable portrait = Drawable.createFromPath(fgPath);

            portraitView.setImageDrawable(portrait);

            Log.d(tag, "Inflated view for " + c.summonerName);

            teamContainer.addView(v);
        }

        // pick a random champion from here to show as the background
        int randomTeam = new Random().nextInt(1);
        int randomChamp = new Random().nextInt(4);

        ArrayList<Champion> teamList;

        if (randomTeam == 0) {
            teamList = mChampionsTeam1;
        } else {
            teamList = mChampionsTeam2;
        }

        String bgPath = getCacheDir() + teamList.get(randomChamp).getChampionBackgroundPath();
        Drawable bgImg = Drawable.createFromPath(bgPath);
        getWindow().getDecorView().setBackground(bgImg);

        Log.d(tag, "done adding champs");
    }
}
