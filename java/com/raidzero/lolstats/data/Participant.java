package com.raidzero.lolstats.data;

/**
 * Created by posborn on 4/1/15.
 */
public class Participant {

    public Champion champion; // champion used
    public Summoner summoner; // who played

    /**
     * the following stuff comes from the stats participant array
     */
    public int teamId; // team played on
    public int doubleKills, tripleKills, quadraKills, pentaKills;
    public int totalKills, deaths; // kills, deaths

}
