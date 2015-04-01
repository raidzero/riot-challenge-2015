package com.raidzero.lolstats.global;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.FileRequestListener;
import com.raidzero.lolstats.interfaces.RestRequestListener;
import com.raidzero.lolstats.parsers.ChampionParser;
import com.raidzero.lolstats.tasks.FileRequest;
import com.raidzero.lolstats.tasks.RestRequest;

import org.json.JSONException;

/**
 * Created by posborn on 4/1/15.
 */
public class ChampionImageDownloader implements RestRequestListener, FileRequestListener {
    private String tag = "ChampionUtils";

    private Context mContext;
    private Champion[] mChampions = new Champion[10];
    private ChampionImageListener mListener;
    private int mFilesDownloaded;

    public ChampionImageDownloader(Context context) {
        mContext = context;
        mListener = (ChampionImageListener) context;
    }

    public void processChampions(Champion[] champs) {
        mChampions = champs;

        for (int i = 0; i < champs.length; i++) {
            // get names and stuff
            RestRequest champInfoRequest = new RestRequest(this,
                    Common.REQUEST_TYPE.CHAMPION, i,
                    Common.CHAMPION_PATH + champs[i].id);
            champInfoRequest.startOperation();
        }
    }

    @Override
    public void onRestRequestComplete(Common.REQUEST_TYPE requestType, int requestCode, String jsonData) {
        try {
            ChampionParser parser = new ChampionParser(jsonData);
            mChampions[requestCode] = parser.getChampionFromParser();

            // now that we have a champion with names, start downloading its images
            FileRequest skinRequest = new FileRequest(mContext, this, requestCode,
                    Common.CHAMPION_SKIN_URL_PREFIX + mChampions[requestCode].key + "_0.jpg");
            skinRequest.startOperation();

            FileRequest portraitRequest = new FileRequest(mContext, this, requestCode,
                    Common.CHAMPION_PORTRAIT_URL_PREFIX + mChampions[requestCode].key + ".png");
            portraitRequest.startOperation();

        } catch (JSONException e) {
            Log.e(tag, e.getMessage());
        }
    }

    @Override
    public void onFileDownloadStart(int requestCode) {
        Log.d(tag, "start downloading file #" + requestCode);
    }

    @Override
    public void onFileComplete(int requestCode, Uri fileUri) {
        Log.d(tag, "FILE COMPLETE: " + fileUri.getEncodedPath());

        if (++mFilesDownloaded == 20) {
            mListener.onChampionImagesDownloaded();
        }

        Log.d(tag, "filesDownloaded: " + mFilesDownloaded);
    }
}
