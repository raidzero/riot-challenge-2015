package com.raidzero.lolstats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.raidzero.lolstats.R;

/**
 * Created by posborn on 4/13/15.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private boolean mShouldRestartApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_region")) {
            mShouldRestartApi = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        // put the restart api flag in an intent
        Intent intent = new Intent();
        intent.putExtra("restartApi", mShouldRestartApi);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
