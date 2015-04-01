package com.raidzero.lolstats.parsers;

import android.util.Log;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.Participant;
import com.raidzero.lolstats.data.Summoner;
import com.raidzero.lolstats.interfaces.RestRequestListener;

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
    public Participant[] participants = new Participant[10];
    public Summoner[] summoners = new Summoner[10];

    public MatchParser(String jsonData) throws JSONException {
        super(jsonData);

        // get match id and mode
        matchId = jsonObject.getInt("matchId");
        matchMode = jsonObject.getString("matchMode");

        // process participants
        JSONArray participantsArray = jsonObject.getJSONArray("participants");

        for (int i = 0; i < participantsArray.length(); i++) {
            try {
                JSONObject obj = participantsArray.getJSONObject(i);

                int champId = obj.getInt("championId");
                championsInMatch[i] = new Champion(champId);

                Participant p = new Participant();
                p.champion = championsInMatch[i];
                p.teamId = obj.getInt("teamId");

                JSONObject statsObj = obj.getJSONObject("stats");

                // kills/deaths
                p.pentaKills = statsObj.getInt("pentaKills");
                p.quadraKills = statsObj.getInt("quadraKills");
                p.tripleKills = statsObj.getInt("tripleKills");
                p.doubleKills = statsObj.getInt("doubleKills");
                p.totalKills = statsObj.getInt("kills");
                p.deaths = statsObj.getInt("deaths");

                participants[i] = p;


            } catch (JSONException e) {
                Log.e(tag, e.getMessage());
            }
        }

        // process summoners
        JSONArray summonersArray = jsonObject.getJSONArray("participantIdentities");

        for (int i = 0; i < summonersArray.length(); i++) {
            try {
                JSONObject obj = summonersArray.getJSONObject(i);
                JSONObject playerObj = obj.getJSONObject("player");

                Summoner s = new Summoner();
                s.name = playerObj.getString("summonerName");

                summoners[i] = s;
            } catch (JSONException e) {
                Log.e(tag, e.getMessage());
            }
        }

        Log.d(tag, "parsing finished!");
    }

    public Match getMatchFromParser() {
        Match m = new Match();

        // add summoner names to participants
        for (int i = 0; i < participants.length; i++) {
            participants[i].summoner = summoners[i];
        }

        m.participants = participants;
        m.matchId = matchId;
        m.matchMode = matchMode;

        return m;
    }

}
