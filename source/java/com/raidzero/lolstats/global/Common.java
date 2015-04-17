package com.raidzero.lolstats.global;

import com.raidzero.lolstats.BuildConfig;

import java.util.HashMap;

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

    /**
     * stored match id's :(
     */
    public static long[] getMatchesForRegion(String region) {
        HashMap<String, long[]> regionIds = new HashMap<>();

        regionIds.put("br", new long[] {505492090,505492249,505468221,505492880,505492446});
        regionIds.put("eune", new long[] {1142579547,1142573377,1142573608,1142574083,1142574412});
        regionIds.put("euw", new long[] {1142579547,2060426958,2060428222,2060427892,2060417214,2060417729,2060419532,2060420003});
        regionIds.put("kr", new long[] {1837149221,1837137464,1837137690,1837137770,1837138026,1837138325});
        regionIds.put("lan", new long[] {156655781,156647746,156647892,156647961,156655231,156647794,156647458,156647728});
        regionIds.put("las", new long[] {197717199,197707622,197707331,197717453,197707425});
        regionIds.put("na", new long[] {1790773944,1790759709,1790759438,1790757544,1790759527,1790759714,1790774694,1790759528,1790775656});
        regionIds.put("oce", new long[] {86059424,86067921,86059491,86068044,86059420});
        regionIds.put("ru", new long[] {65417396,65422559,65417384,65417460,65417470,65417327,65422686,65417665,65417455,65417513});
        regionIds.put("tr", new long[] {239953879,239954093,239954374,239948886,239949234,239936195,239947156});

        return regionIds.get(region);
    };

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
    public static final String CHAMPION_PATH = "/static-data/%s/v1.2/champion/";
}
