package com.raidzero.lolstats.data;

/**
 * Created by posborn on 3/31/15.
 */
public class Match {

    // array of participants in the match
    public Participant[] participants = new Participant[10];

    // match mode
    public String matchMode;

    // ID of match
    public int matchId;

    public Champion[] getChampionsInMatch() {
        Champion[] rtn = new Champion[10];

        for (int i = 0; i < participants.length; i++) {
            rtn[i] = participants[i].champion;
        }

        return rtn;
    }
}
