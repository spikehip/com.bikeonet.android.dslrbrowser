package com.bikeonet.android.dslrbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {

    private OnPreferenceChangeListener updateTextListener = new OnPreferenceChangeListener() {        
        public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((CharSequence) newValue); 
                return true;
        }
    };
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.main_preferences);

		String default_email = getPreferenceManager().getSharedPreferences()
				.getString("default_email", null);
		
		if (default_email != null) {
			EditTextPreference defaultEmailEdit = (EditTextPreference) getPreferenceManager()
					.findPreference("default_email");
			
			defaultEmailEdit.setSummary(default_email);
			defaultEmailEdit.setOnPreferenceChangeListener(updateTextListener);
			
		}
		
		String size = getPreferenceManager().getSharedPreferences().getString("sizes_list_preference", null);
		if ( size != null) { 
			ListPreference sizeList =  (ListPreference) getPreferenceManager().findPreference("sizes_list_preference");
			sizeList.setSummary(size);
			sizeList.setOnPreferenceChangeListener(updateTextListener);
		}
		
		String previewSize = getPreferenceManager().getSharedPreferences().getString("sizes_preview_preference", null);
		if ( previewSize != null) { 
			ListPreference previewSizeList = (ListPreference) getPreferenceManager().findPreference("sizes_preview_preference");
			previewSizeList.setSummary(previewSize);
			previewSizeList.setOnPreferenceChangeListener(updateTextListener);
		}
		
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, DeviceListActivity.class));
	}

}
