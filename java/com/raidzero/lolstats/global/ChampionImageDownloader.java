package com.raidzero.lolstats.global;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.RequestCommandListener;
import com.raidzero.lolstats.parsers.ChampionParser;

import org.json.JSONException;

/**
 * Created by posborn on 4/1/15.
 */
public class ChampionImageDownloader implements RequestCommandListener {
    private String tag = "ChampionUtils";

    private Context mContext;
    private Champion[] mChampions = new Champion[10];
    private ChampionImageListener mListener;
    private AppHelper mHelper;

    private int mFilesDownloaded, mNumChampInfo;

    public ChampionImageDownloader(Context context) {
        mContext = context;
        mListener = (ChampionImageListener) context;
        mHelper = ((AppHelper) context.getApplicationContext());
    }

    public void processChampions() {
        Champion[] champs = mHelper.getMatchChampions();

        for (int i = 0; i < champs.length; i++) {
            // get names and stuff
            RequestCommand command = new RequestCommand();
            command.requestType = RequestCommand.RequestType.REST;
            command.listener = this;
            command.requestId = i;
            command.requestUrl = Common.CHAMPION_PATH + champs[i].id;

            RequestProcessor.addRequest(command);
        }
    }


    @Override
    public void onProcessStart(RequestCommand command) {
        Log.d(tag, "Download started: " + command.requestUrl);
    }

    @Override
    public void onProcessComplete(RequestCommand command) {
        Log.d(tag, "onProcessComplete: " + command.requestId);

        switch (command.requestType) {
            case REST: // champ info
                try {
                    ChampionParser parser = new ChampionParser(command.restResponse);
                    mChampions[command.requestId] = parser.getChampionFromParser();

                    // make a request to get the skin and portait images
                    RequestCommand portaitCommand = new RequestCommand();
                    portaitCommand.listener = this;
                    portaitCommand.requestType = RequestCommand.RequestType.FILE;
                    portaitCommand.destDir = mContext.getCacheDir();

                    portaitCommand.requestUrl = Common.CHAMPION_PORTRAIT_URL_PREFIX +
                                    mChampions[command.requestId].getChampionPortaitPath();
                    portaitCommand.requestId = 200 + command.requestId;

                    // add portrait request
                    RequestProcessor.addRequest(portaitCommand);

                    RequestCommand bgCommand = new RequestCommand();
                    bgCommand.listener = this;
                    bgCommand.destDir = mContext.getCacheDir();
                    bgCommand.requestType = RequestCommand.RequestType.FILE;

                    bgCommand.requestUrl = Common.CHAMPION_SKIN_URL_PREFIX +
                            mChampions[command.requestId].getChampionBackgroundPath();
                    bgCommand.requestId = 300 + command.requestId;

                    // add skin request
                    RequestProcessor.addRequest(bgCommand);
                    mNumChampInfo++;

                    if (mNumChampInfo == 10) {
                        // if all done, send this back to helper
                        mHelper.setMatchChampions(mChampions);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case FILE:
                Log.d(tag, "GOT FILE: " + ++mFilesDownloaded);

                if (mFilesDownloaded == 20) {
                    mListener.onChampionImagesDownloaded();
                }
                break;
        }

    }
}
