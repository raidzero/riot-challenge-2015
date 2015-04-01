package com.raidzero.lolstats.parsers;

import android.util.Log;

import com.raidzero.lolstats.data.Champion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by raidzero on 3/31/15.
 */
public class ChampionParser extends JSONParser {
    private String tag = "ChampionParser";

    public int championId;
    public String name, title;

    public ChampionParser(String jsonData) throws JSONException {
        super(jsonData);

        championId = jsonObject.getInt("id");
        name = jsonObject.getString("name");
        title = jsonObject.getString("title");

        Log.d(tag, "parsing finished!");
    }

    public Champion getChampionFromParser() {
        Champion rtnChamp = new Champion(championId);

        rtnChamp.name = name;
        rtnChamp.title = title;

        return rtnChamp;
    }

}
