package com.raidzero.lolstats.data;

import android.net.Uri;

/**
 * Created by posborn on 3/31/15.
 */
public class Champion {

    public String title, name;
    public int id;

    public Champion(int id) {
        this.id = id;
    }

    public Uri getChampionPortait() {
        return null;
    }

    public Uri getChampionBackground() {
        return null;
    }
}
