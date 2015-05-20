package com.zebracommerce.zcpaymentapi.zcpaymentapi_testharness_direct;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class SettingsActivity extends Activity {
	private Bundle prefsBundle = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getActionBar().hide();
		super.onCreate(savedInstanceState);

		Intent i = this.getIntent();
		prefsBundle = i.getBundleExtra("zc_preferences");
		
		FragmentManager FM = getFragmentManager();
		FragmentTransaction FT = FM.beginTransaction();
		FT.replace(android.R.id.content, new SettingsFragment(prefsBundle));
		FT.commit();
	}
	
    @Override
    public boolean onKeyDown(int iKeycode, KeyEvent oKE) {
    	boolean bRC = false;
    	if (iKeycode == KeyEvent.KEYCODE_MENU)
    		bRC = true;
    	else
    		bRC = super.onKeyDown(iKeycode, oKE);
    	
    	return bRC;
    }
}
