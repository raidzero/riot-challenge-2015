package com.raidzero.lolstats.global;

/**
 * Created by posborn on 3/31/15.
 */
public class Common {
    public static final String API_KEY = "fd2cabac-cd1d-4d80-b3cf-afee646d2283";

    // enum of request types
    public static enum REQUEST_TYPE {
        CHAMPION,
        MATCH,
        SUMMONER
    }

    // enum of rest request message types
    public static enum FILE_MESSAGE_TYPE {
        ON_START,
        ON_COMPLETE
    }

    // rest request codes
    public static final int REQUEST_CODE_MATCH = 1000;

    // static list of 5 match ID's. this will be replaced with random match ID's.
    public static final int[] MATCH_IDS = { 1749315593, 1750322945, 1751326160, 1751406221, 1755642247 };

    // URL prefix for champion skin image
    public static final String CHAMPION_SKIN_URL_PREFIX = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/"; // {CHAMP_NAME}_0.jpg

    // URL prefix for champion portrait image
    public static final String CHAMPION_PORTRAIT_URL_PREFIX = "http://ddragon.leagueoflegends.com/cdn/5.2.2/img/champion/"; // {CHAMP_NAME}.png

    // URL prefix for all REST calls
    public static final String API_PREFIX = "https://na.api.pvp.net/api/lol";

    // REST path for match info
    public static final String MATCH_PATH = "/na/v2.2/match/";

    // REST path for random match ID's
    public static final String RANDOM_MATCH_PATH = "/na/v4.1/game/ids"; // ?beginDate=09:05

    // REST path for champion info
    public static final String CHAMPION_PATH = "/static-data/na/v1.2/champion/";
}
