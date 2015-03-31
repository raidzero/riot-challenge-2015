package com.raidzero.lolstats.parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by posborn on 3/31/15.
 */
public class JSONParser {
    private JSONObject jsonObject;

    public JSONParser(String jsonData) throws JSONException {
        jsonObject = new JSONObject(jsonData);
    }

    public JSONArray getArray(String arrayName) throws JSONException {
        return jsonObject.getJSONArray(arrayName);
    }

    public String getString(String stringName) throws JSONException {
        return jsonObject.getString(stringName);
    }

    public int getInt(String intName) throws JSONException {
        return jsonObject.getInt(intName);
    }

}
