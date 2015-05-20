package com.zebracommerce.zcpaymentapi.zcpaymentapi_testharness_direct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebracommerce.snap.responsebase.DataResponseBase;
import com.zebracommerce.snap.responsebase.ResponseBase;
import com.zebracommerce.snap.responsebase.ResultCodes;
import com.zebracommerce.snap.util.logging.LogFactory;
import com.zebracommerce.snap.util.logging.XmlFileLogMessageInfoWriter;
import com.zebracommerce.zcpaymentapi.Device;
import com.zebracommerce.zcpaymentapi.IPayRequestBase;
import com.zebracommerce.zcpaymentapi.IResultsCallback;
import com.zebracommerce.zcpaymentapi.PayPrintRequestBase;
import com.zebracommerce.zcpaymentapi.PayResultsBase;
import com.zebracommerce.zcpaymentapi.PayTransactionRequestBase;
import com.zebracommerce.zcpaymentapi.commIO.CommIO;
import com.zebracommerce.zcpaymentapi.commIO.ICommIO.ECommType;
import com.zebracommerce.zcpaymentapi.commIO.IConnectedCallback;
import com.zebracommerce.zcpaymentapi.zcpaymentapi_msclib.ZCPaymentAPI_MSCLib;


public class MainActivity extends Activity implements IConnectedCallback, IResultsCallback {

	private OnSharedPreferenceChangeListener prefListener = null;
	private ZCPaymentAPI_MSCLib mPayLib = null;
	private SharedPreferences sharedPrefs = null;
	private List<String> listAdapter = null;
	private ImageView imageViewResult = null;
	private EditText editTextAmount = null;
	private EditText editTextReference = null;
	private TextView textViewProcessResults = null;
	private Boolean payInProgress = false;
	private Button btnDetails = null;
	private Button btnPrint = null;
	private Button btnClear = null;
	private Button btnProcess = null;
	private Bundle prefsBundle = null;
	private Device mPayDevice = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//		android.os.Debug.waitForDebugger();

		//
		// Define and start logging.
		//
		LogFactory.StartLogger(new XmlFileLogMessageInfoWriter(this, "LogConfig.xml"));
		
		//
		// This necessary to avoid some bugs others found in cached settings. 
		//
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefs.edit().clear();
		sharedPrefs.edit().apply();
		
		//
		// Test for ULR scheme.
		//
		String urlLaunchAmount = "";
		String urlLaunchReference = this.getReferenceNumber();
		
		try {
			Uri data = getIntent().getData();
			String scheme = data.getScheme();
			
			if (scheme.equalsIgnoreCase("zcpaytestharness")) {
				List<String> params = data.getPathSegments();
				urlLaunchAmount = params.get(0);
				urlLaunchReference = params.get(1);
			}
		}
		catch (Exception ex) {
			urlLaunchAmount = "";
			urlLaunchReference = this.getReferenceNumber();
		}
		
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
				if (arg1.equals("zc_pinpad")) {
					MainActivity.this.InitPayService();
				}
			}
		};
		
		sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);
        editTextAmount = (EditText)findViewById(R.id.editTextAmount);
        editTextReference = (EditText)findViewById(R.id.editTextReference);
        textViewProcessResults = (TextView)findViewById(R.id.processResults);
        imageViewResult = (ImageView)findViewById(R.id.imageViewResult);
        btnDetails = (Button)findViewById(R.id.buttonDetails);
        btnPrint = (Button)findViewById(R.id.printScreen);
        btnClear = (Button)findViewById(R.id.buttonClear);
        btnProcess = (Button)findViewById(R.id.buttonProcess);
        
        listAdapter = new ArrayList<String>();	//ArrayAdapter<String>(this, R.layout.simplerow);
        
        editTextAmount.addTextChangedListener(editTextWatcher);
        editTextReference.addTextChangedListener(editTextWatcher);
        btnDetails.setOnClickListener(onClickListener);
        btnPrint.setOnClickListener(onClickListener);
        btnClear.setOnClickListener(onClickListener);
        btnProcess.setOnClickListener(onClickListener);
		
        //
        // Initialize view.
        //
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        editTextAmount.setText(urlLaunchAmount);
        editTextReference.setText(urlLaunchReference);
        textViewProcessResults.setText("");
        imageViewResult.setImageResource(0);
        imageViewResult.setImageBitmap(null);
        setInputEnabled(true);	// (false);
        
        this.mPayDevice = new Device();
        mPayDevice.CommType = ECommType.BT;
        
        this.mPayLib = new ZCPaymentAPI_MSCLib(mPayDevice, this, this);
        this.InitPayService();
    }

	/**
	 * Listen for button clicks.
	 */
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.buttonDetails) {
				try {
					Intent detailsIntent = MainActivity.this.createIntentFromAdapter();
					if (null != detailsIntent) {
						startActivity(detailsIntent);
					}
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (v.getId() == R.id.printScreen) {
				try {
		    		IPayRequestBase printRequest = new PayPrintRequestBase();
		    		((PayPrintRequestBase)printRequest).setPrintLines("NThis is NORMAL.\nBThis is BOLD\nNAnd back to NORMAL.");
		    		MainActivity.this.mPayLib.Process(printRequest);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (v.getId() == R.id.buttonClear) {
				try {
					MainActivity.this.setInputEnabled(true);
			        imageViewResult.setImageResource(0);
			        imageViewResult.setImageBitmap(null);
					editTextAmount.setText("");
					editTextReference.setText(MainActivity.this.getReferenceNumber());
					textViewProcessResults.setText("");
					listAdapter.clear();
					editTextAmount.requestFocus();
					MainActivity.this.InitPayService();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (v.getId() == R.id.buttonProcess) {
				try {
					do // This is not a loop.
					{
						synchronized (payInProgress) {
							if (false != payInProgress) {
								break;
							}
							
							payInProgress = true;
						}
						
			    		MainActivity.this.setInputEnabled(false);
			    		MainActivity.this.textViewProcessResults.setText(R.string.paystatus_processing);
	
			    		String amount = editTextAmount.getText().toString();
			    		String reference = editTextReference.getText().toString();
			    		IPayRequestBase payRequest = new PayTransactionRequestBase("sale", amount, reference);
			    		MainActivity.this.mPayLib.Process(payRequest);
					}
					while(false);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}; 
	
	/**
	 * Watch the text inputs so we can enable/disable views accordingly.
	 */
	private TextWatcher editTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			MainActivity.this.setInputEnabled(true);
		}
	};
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int iKeycode, KeyEvent oKE) {
    	boolean bRC = false;
    	Intent i = null;
    	try {
    		if (iKeycode == KeyEvent.KEYCODE_MENU) {
    			if (null != this.prefsBundle) {
    				i = new Intent(this, SettingsActivity.class);
    				i.putExtra("zc_preferences", this.prefsBundle);
    				startActivity(i);
    			}
    			bRC = true;
    		}
    		else if (iKeycode == KeyEvent.KEYCODE_BACK) {
    			bRC = true;
    		}
    		else {
    			bRC = super.onKeyDown(iKeycode, oKE);
    		}
    	}
    	catch (Exception x) {
    		x.printStackTrace();
    	}
    		
    	return bRC;
    }

    /**
     * Call to enable/disable the inputs.
     * @param enabled
     */
    private void setInputEnabled(boolean enabled) {
    	boolean enableProcess = false;
    	
        editTextAmount.setEnabled(enabled);
        editTextReference.setEnabled(enabled);
        
        if (false != enabled) {
        	try {
        		enableProcess = (0 < editTextAmount.getText().toString().length());
        		enableProcess = ((false == enableProcess) ? enableProcess : (0 < editTextReference.getText().toString().length()));
        		MainActivity.this.btnProcess.setEnabled(enableProcess);
        	}
        	catch (Exception ex) {
        		enableProcess = false;
        		ex.printStackTrace();
        	}
        }

        btnProcess.setEnabled((false != enableProcess) && (false != enabled));
    }
   
    @Override
    public void onInitComplete(ResponseBase rb) {		// (ParcelableResponseBase initResults) {
    	final ResponseBase rbFinal = rb;
		
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
    			MainActivity.this.processInitComplete(rbFinal);
			}
		});
    }
    
    /**
     * @param rb
     */
    private void processInitComplete(ResponseBase rb) {
    	if (null != rb) {
    		listAdapter.add("Init Complete: " + rb.getResultMessage());
    		
    		if (ResponseBase.EResultType.Success != rb.getResultType()) {
    			MainActivity.this.textViewProcessResults.setText(rb.getResultMessage());
    		}
    		else {
    			synchronized (payInProgress) {
    				if (false == payInProgress) {
    	    			MainActivity.this.textViewProcessResults.setText(getString(R.string.paystatus_ready));
    				}
    			}
    		}
    	}
    }
    
	/**
	 * 
	 */
	private void InitPayService() {
		try {
			Bundle prefs = new Bundle();
        	prefs.putString("zc_pinpad", "MPOS-64003231");
			
			Map<String, ?> prefMap = sharedPrefs.getAll();
			for (Map.Entry<String, ?> entry : prefMap.entrySet()) {
				prefs.putString(entry.getKey(), entry.getValue().toString());
			}
			
	        this.mPayLib.Init(prefs);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	private Intent createIntentFromAdapter() {
		Intent retVal = null;
		
		try {
			String[] listValues = this.listAdapter.toArray(new String[this.listAdapter.size()]);
			retVal = new Intent(this, DetailsActivity.class);
			retVal.putExtra("listValues", listValues);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return retVal;
	}
	
	/**
	 * Dummy reference to always display.
	 * @return
	 */
	private String getReferenceNumber() {
		String retVal = "";
		
		try {
			retVal = "0123456";
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return retVal;
	}

	@Override
	public void onResultsComplete(DataResponseBase<PayResultsBase> resultsResponseBase) {
		final DataResponseBase<PayResultsBase> rbFinal = resultsResponseBase;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
    			MainActivity.this.processResultsComplete(rbFinal);
			}
		});
	}

	/**
	 * @param resultsResponseBase
	 */
	private void processResultsComplete(DataResponseBase<PayResultsBase> resultsResponseBase) {
		try
		{
			do {	// This is not a loop.
				if (null != resultsResponseBase) {
					listAdapter.add("Process Complete: " + resultsResponseBase.getResultMessage());
					
					if (ResponseBase.EResultType.Success != resultsResponseBase.getResultType()) {
			    		this.textViewProcessResults.setText(resultsResponseBase.getResultMessage());
					}
					else {
						PayResultsBase prb = resultsResponseBase.getData();
						String processResults = "";
						
						if (false != prb.getRequestType().equalsIgnoreCase("PRINT")) {
							
						}
						else if (false != prb.getRequestType().equalsIgnoreCase("TRANSACTION")) {
							boolean isAccepted = (Boolean.valueOf(prb.getResultsParam("zc_approved")));
							if (false == isAccepted) {
								this.imageViewResult.setImageResource(R.drawable.no);
								processResults = getString(R.string.paystatus_declined);
							}
							else {
								this.imageViewResult.setImageResource(R.drawable.yes);
								processResults = String.format("%s\n\n", getString(R.string.paystatus_accepted));
								processResults += String.format("Auth Code: %s\n", prb.getResultsParam("zc_authcode"));
								processResults += String.format("Date (UTC): %s\n", prb.getResultsParam("zc_dateTimeUTC"));
								processResults += String.format("PAN: %s\n", prb.getResultsParam("zc_maskedPan"));
								processResults += String.format("Ref: %s", prb.getResultsParam("zc_reference"));
							}
						}
						
						for(String key : prb.getResultsParams().keySet()) {
							String value = prb.getResultsParam(key); // zc_approved
							if (null != value) {
								listAdapter.add("   " + key + " / " + value);
							}
						}

						if ((null != processResults) && (false == processResults.isEmpty())) {
							this.textViewProcessResults.setText(processResults);
						}
					}
				}
			}
			while(false);
		}
		catch(Exception e) {
			this.textViewProcessResults.setText(e.getMessage());
		}
		finally {
			synchronized (payInProgress) {
				payInProgress = false;
			}
		}
	}
	
	@Override
	public void onConnected(CommIO commIO) {
		//
		// Initialization should be complete.
		//
		this.onInitComplete(new ResponseBase(ResultCodes.SUCCESS));
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
    			MainActivity.this.setInputEnabled(true);
			}
		});
	}

	@Override
	public void onDisconnected() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
    			MainActivity.this.setInputEnabled(false);
			}
		});
	}
}
