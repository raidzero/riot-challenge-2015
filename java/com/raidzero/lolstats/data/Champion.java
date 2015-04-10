package com.raidzero.lolstats.data;

/**
 * Created by posborn on 3/31/15.
 */
public class Champion {

    public String title, name, key;
    public int id;

    public String portraitPath;
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
