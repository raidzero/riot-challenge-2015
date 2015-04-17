package com.raidzero.lolstats.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.fragments.SettingsFragment;

/**
 * Created by posborn on 4/13/15.
 */
public class SettingsActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        getFragmentManager().beginTransaction().replace(R.id.frameLayout_settings,
                new SettingsFragment()).commit();
    }
}
