package com.raidzero.lolstats.data;

/**
 * Created by posborn on 3/31/15.
 */
public class Match {

    // array of champions in the match
    public Champion[] champions = new Champion[10];  // 10 champions in a match

    // match mode
    public String matchMode;

    // ID of match
    public int matchId;
}
