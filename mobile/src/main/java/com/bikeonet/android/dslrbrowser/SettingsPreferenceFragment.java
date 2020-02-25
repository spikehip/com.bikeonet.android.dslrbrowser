package com.bikeonet.android.dslrbrowser;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;


/**
 * Created by andrasbekesi on 27/04/17.
 */

public class SettingsPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
