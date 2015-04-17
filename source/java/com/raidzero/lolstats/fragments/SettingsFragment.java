package com.raidzero.lolstats.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.NumberPickerPreference;

/**
 * Created by posborn on 4/14/15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences mPrefs;

    ListPreference mPrefsRegion;
    CheckBoxPreference mPrefAutoAdvance;
    NumberPickerPreference mPrefDelay;
    PreferenceCategory mCategory;


    String mSavedRegionName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPrefsRegion = (ListPreference) findPreference("pref_region");
        mPrefAutoAdvance = (CheckBoxPreference) findPreference("pref_auto_advance");
        mPrefDelay = (NumberPickerPreference) findPreference("pref_mvp_delay");
        mCategory = (PreferenceCategory) findPreference("pref_main_category");

        int savedDelay = mPrefs.getInt("pref_mvp_delay", 4);
        mSavedRegionName = getRegionNameFromCode(mPrefs.getString("pref_region", "na"));

        mPrefsRegion.setSummary(String.format(getActivity().
                getString(R.string.pref_region_summary), mSavedRegionName));

        if (!mPrefAutoAdvance.isChecked()) {
            mPrefAutoAdvance.setSummary(getString(R.string.pref_auto_advance_summary_disabled));
            mCategory.removePreference(mPrefDelay);
        } else {
            mPrefAutoAdvance.setSummary(String.format(getActivity().
                    getString(R.string.pref_auto_advance_summary_enabled), savedDelay));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceManager.
                getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("pref_auto_advance")) {
            if (prefs.getBoolean(key, false)) {
                mCategory.addPreference(mPrefDelay);
                mPrefAutoAdvance.setSummary(String.format(
                        getActivity().getString(R.string.pref_auto_advance_summary_enabled),
                        prefs.getInt("pref_mvp_delay", 4)));
            } else {
                mCategory.removePreference(mPrefDelay);
                mPrefAutoAdvance.setSummary(getActivity().
                        getString(R.string.pref_auto_advance_summary_disabled));
            }
        } else if (key.equals("pref_region")) {
            mSavedRegionName = getRegionNameFromCode(prefs.getString(key, "na"));

            mPrefsRegion.setSummary(String.format(getActivity().
                    getString(R.string.pref_region_summary), mSavedRegionName));
        } else if (key.equals("pref_mvp_delay")) {
            mPrefAutoAdvance.setSummary(String.format(getActivity().
                    getString(R.string.pref_auto_advance_summary_enabled), prefs.getInt(key, 4)));
        }
    }

    private String getRegionNameFromCode(String code) {
        String[] codes = getResources().getStringArray(R.array.regionCodes);
        String[] names = getResources().getStringArray(R.array.regionName);

        int index;
        for(index = 0; index < codes.length; index++) {
            if (codes[index].equals(code)) {
                return names[index];
            }
        }

        return null;
    }
}
