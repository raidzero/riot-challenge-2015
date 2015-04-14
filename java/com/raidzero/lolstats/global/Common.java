package com.raidzero.lolstats.global;

import com.raidzero.lolstats.BuildConfig;

/**
 * Created by posborn on 3/31/15.
 */
public class Common {

    // old api key is no longer valid. Should not keep api keys in codebase.
    public static String getApiKey() {
        return BuildConfig.API_KEY;
    }

    // converts bool to "Yes", "No"
    public static String bool2str(boolean b) {
        if (b) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public static final int REQUEST_CODE_MATCHES = 1000;
    public static final int REQUEST_CODE_SETTINGS = 1001;

    // URL prefix for champion skin image
    public static final String CHAMPION_SKIN_URL_PREFIX = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/"; // {CHAMP_NAME}_0.jpg

    // URL prefix for champion portrait image
    public static final String CHAMPION_PORTRAIT_URL_PREFIX = "http://ddragon.leagueoflegends.com/cdn/5.6.2/img/champion/"; // {CHAMP_NAME}.png

    // URL prefix for all REST calls
    public static final String API_PREFIX = "https://%s.api.pvp.net/api/lol";

    // REST path for match info
    public static final String MATCH_PATH = "/v2.2/match/"; // prefix with /region

    // REST path for random match ID's
    public static final String RANDOM_MATCH_PATH = "/v4.1/game/ids"; // ?beginDate=1428687300

    // REST path for champion info
    public static final String CHAMPION_PATH = "/static-data/na/v1.2/champion/";
}
