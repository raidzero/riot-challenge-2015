package com.raidzero.lolstats.data;

/**
 * Created by posborn on 4/1/15.
 */
public class Participant implements Comparable<Participant> {

    public Champion champion; // champion used

    /**
     * the following stuff comes from the stats participant array
     */
    public int teamId; // team played on
    public int doubleKills, tripleKills, quadraKills, pentaKills;
    public int totalKills, deaths, assists;
    public int largestKillingSpree;
    public int largestMultiKill;
    public int goldEarned, goldSpent;
    public int damageTaken, damageDealt;
    public int towerKills, inhibitorKills;

    public boolean winningTeam;
    public boolean firstBlood;

    @Override
    public int compareTo(Participant another) {
        return totalKills - another.totalKills;
    }
}
