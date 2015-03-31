package com.raidzero.lolstats.data;

import android.net.Uri;

/**
 * Created by posborn on 3/31/15.
 */
public class Champion {

    public String title, name;
    public int id;

    public Champion(int id, String name, String title) {
        this.id = id;
        this.name = name;
        this.title = title;
    }

    public Uri getChampionPortait() {
        return null;
    }

    public Uri getChampionBackground() {
        return null;
    }
}
