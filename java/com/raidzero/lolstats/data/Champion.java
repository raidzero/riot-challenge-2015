package com.raidzero.lolstats.data;

import com.raidzero.lolstats.global.Common;

/**
 * Created by posborn on 3/31/15.
 */
public class Champion {

    public String title, name, key, summonerName;
    public int id;

    public Champion(int id) {
        this.id = id;
    }

    // some titles don't start with "the", but most do, so...
    public void setTitle(String title) {
        if (!title.startsWith("the")) {
            this.title = "the " + title;
        } else {
            this.title = title;
        }
    }

    public String getChampionPortaitPath() {
        return "/" + key + ".png";
    }

    public String getChampionBackgroundPath() {
        return "/" + key + "_0.jpg";
    }
}
