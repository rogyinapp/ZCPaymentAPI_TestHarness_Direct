package com.zebracommerce.zcpaymentapi.zcpaymentapi_testharness_direct;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	private Bundle prefs = null;
	private String prefsName = "";
	
    public SettingsFragment(Bundle prefsBundle) {
    	if (null != prefsBundle) {
    		prefs = prefsBundle;
    		prefsName = prefsBundle.getString("name");
    	}
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
        	PreferenceScreen p = createPreferences();
        	this.setPreferenceScreen(p);
        }
        catch (Exception x) {
        }
    }
    
    public void onResume(){
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        if (null != this.prefs) {
        	for (String k : prefs.keySet()) {
                updatePreference(k);
        	}
        }
    }
    
    private PreferenceScreen createPreferences()
    {
    	PreferenceScreen p = getPreferenceManager().createPreferenceScreen(getActivity());
    	PreferenceCategory pc = new PreferenceCategory(getActivity());

    	pc.setTitle(this.prefsName);
    	p.addPreference(pc);
    	
    	//
    	// Iterate through our bundles.  The head bundle isn't used for preference screen.
    	//
    	Bundle current = prefs;
    	do {	// This is not a loop.
    		if (false == current.containsKey("next")) {
    			break;
    		}
    		
    		try {
    			current = current.getBundle("next");
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}

    		EditTextPreference editPref = new EditTextPreference(getActivity());
            editPref.setKey(current.getString("key"));
    		
            if (false != current.containsKey("defaultValue")) {
            	editPref.setDefaultValue(current.getString("defaultValue"));
            }

            if (false != current.containsKey("title")) {
            	editPref.setTitle(current.getString("title"));
            	editPref.setDialogTitle(current.getString("title"));
            }

            if (false != current.containsKey("summary")) {
            	editPref.setSummary(current.getString("summary"));
            }
            
            if (false != current.containsKey("enabled")) {
            	String enabled = current.getString("enabled");
                editPref.setEnabled(Boolean.valueOf(enabled));
            }

            pc.addPreference(editPref);
        }
    	while(true);
    	
        return p;
    }    
    
    @Override
	public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePreference(key);		
	}
	
	private void updatePreference(String key) {
		try {
			Preference preference = findPreference(key);
			if (preference instanceof EditTextPreference) {
				EditTextPreference editTextPreference = (EditTextPreference)preference;
				
				if (editTextPreference.getText().trim().length() > 0) {
					editTextPreference.setSummary(editTextPreference.getText());
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
