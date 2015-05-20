package com.zebracommerce.zcpaymentapi.zcpaymentapi_testharness_direct;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DetailsActivity extends Activity {

	private ArrayAdapter<String> listAdapter = null;
	private ListView mainListView = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

	    try {
			mainListView = (ListView)findViewById(R.id.listViewStatus);
	        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
	        
	        Intent listValuesIntent = getIntent();
	        if (null != listValuesIntent) {
	        	String[] listValues = listValuesIntent.getStringArrayExtra("listValues");
	        	if (null != listValues) {
	        		listAdapter.addAll(listValues);
	        	}
	        }
	        
	        mainListView.setAdapter(listAdapter);
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	}

}
