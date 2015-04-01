package com.raidzero.lolstats.parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by posborn on 3/31/15.
 */
public abstract class JSONParser {
    protected JSONObject jsonObject;

    public JSONParser(String jsonData) throws JSONException {
        jsonObject = new JSONObject(jsonData);
    }
}
