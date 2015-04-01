package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.interfaces.FileRequestListener;
import com.raidzero.lolstats.interfaces.RestRequestListener;
import com.raidzero.lolstats.parsers.MatchParser;
import com.raidzero.lolstats.tasks.FileRequest;

import org.json.JSONException;

import com.raidzero.lolstats.global.Common.REQUEST_TYPE;
import com.raidzero.lolstats.tasks.RestRequest;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements RestRequestListener, FileRequestListener {
    private final String tag = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        int matchId = Common.MATCH_IDS[0];

        RestRequest matchRequest = new RestRequest(this, REQUEST_TYPE.MATCH, Common.MATCH_PATH + matchId);
        matchRequest.startOperation();

        FileRequest skinRequest = new FileRequest(this, Common.CHAMPION_SKIN_URL_PREFIX + "Malzahar_0.jpg");
        skinRequest.startOperation();
    }

    @Override
    public void onRestRequestComplete(REQUEST_TYPE reqType, String jsonData) {
        Log.d(tag, "HEY I GOT STUFF: " + jsonData.length() + " bytes. " + reqType);

        try {
            MatchParser parser = new MatchParser(jsonData);
            Match m = parser.getMatchFromParser();

            Log.d(tag, "HOORAY! GOT A MATCH OBJECT: " + m.matchMode);
        } catch (JSONException e) {
            Log.e(tag, e.getMessage());
        }
    }

    @Override
    public void onFileDownloadStart() {
        Toast.makeText(this, "Download started.", Toast.LENGTH_SHORT).show();
        Log.d(tag, "download started!");
    }

    @Override
    public void onFileComplete(Uri fileUri) {
        Toast.makeText(this, "Download complete!", Toast.LENGTH_SHORT).show();
        Log.d(tag, "Downlod complete: " + fileUri.toString());

        Drawable img = Drawable.createFromPath(fileUri.getEncodedPath());
        getWindow().getDecorView().setBackground(img);

    }
}
