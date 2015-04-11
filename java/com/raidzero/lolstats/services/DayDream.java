package com.raidzero.lolstats.services;

import android.content.Intent;
import android.service.dreams.DreamService;

import com.raidzero.lolstats.activities.MainActivity;

/**
 * Created by raidzero on 4/11/15.
 */
public class DayDream extends DreamService {
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setInteractive(false);
        setFullscreen(true);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        finish();
    }
}
