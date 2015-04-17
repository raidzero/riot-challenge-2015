package com.raidzero.lolstats.parsers;

import android.util.Log;

import com.raidzero.lolstats.data.Champion;

import org.json.JSONException;

/**
 * Created by raidzero on 3/31/15.
 */
public class ChampionParser extends JSONParser {
    private String tag = "ChampionParser";

    public int championId;
    public String name, title, key;

    public ChampionParser(String jsonData) throws JSONException {
        super(jsonData);

        championId = jsonObject.getInt("id");
        name = jsonObject.getString("name");
        title = jsonObject.getString("title");
        key = jsonObject.getString("key");
    }

    public Champion getChampionFromParser() {
        Champion rtnChamp = new Champion(championId);

        rtnChamp.name = name;
        rtnChamp.key = key;
        rtnChamp.setTitle(title);

        return rtnChamp;
    }
}
