package com.raidzero.lolstats.global;

import android.app.Application;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.interfaces.RequestCommandListener;

/**
 * Created by raidzero on 4/7/15.
 */
public class AppHelper extends Application implements RequestCommandListener {
    private static final String tag = "AppHelper";

    private Match mCurrentMatch;
    private Champion[] mMatchChampions = new Champion[10];

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setCurrentMatch(Match m) {
        mCurrentMatch = m;
    }

    public void setMatchChampions(Champion[] champions) {
        mMatchChampions = champions;
    }

    public Champion[] getMatchChampions() {
        return mMatchChampions;
    }

    public Match getCurrentMatch() {
        return mCurrentMatch;
    }

    @Override
    public void onProcessStart(RequestCommand command) {

    }

    @Override
    public void onProcessComplete(RequestCommand command) {

    }
}
