package com.raidzero.lolstats.parsers;

import android.util.Log;

import com.raidzero.lolstats.data.Champion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by raidzero on 3/31/15.
 */
public class MatchParser extends JSONParser {
    private String tag = "MatchParser";

    public int matchId;
    public String matchMode;

    public Champion[] championsInMatch = new Champion[10];

    public MatchParser(String jsonData) throws JSONException {
        super(jsonData);

        // get match id and mode
        matchId = jsonObject.getInt("matchId");
        matchMode = jsonObject.getString("matchMode");

        // get list of participants
        JSONArray participantsArray = jsonObject.getJSONArray("participants");
        for (int i = 0; i < participantsArray.length(); i++) {
            try {
                JSONObject obj = participantsArray.getJSONObject(i);
                int champId = obj.getInt("championId");
                championsInMatch[i] = new Champion(champId);
            } catch (JSONException e) {
                Log.e(tag, e.getMessage());
            }
        }

        Log.d(tag, "parsing finished!");
    }


}
