package com.fede;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.fede.Utilities.GeneralUtils;
import com.fede.Utilities.PrefUtils;

public class HomeAlone extends Activity {
	static final int MENU_OPTIONS = Menu.FIRST;	
	public static final String STATE_CHANGED = "GlobalStateChanged";
	private BroadcastReceiver 	mBroadcastRecv;
	private IntentFilter 		mFilter;
	
	Button 						mActivateButton;
	IncomingCallReceiver 		mReceiver;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setupButtons();
        mFilter = new IntentFilter(HomeAlone.STATE_CHANGED);

        mBroadcastRecv = new BroadcastReceiver(){
    		@Override
    		public void onReceive(Context context, Intent intent) {	
    			setButtonCaption();
    		}
    	};
    }
    
    
	@Override
	protected void onPause() {		
		super.onPause();
		unregisterReceiver(mBroadcastRecv);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mBroadcastRecv, mFilter);
		setButtonCaption();
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		int groupId = 0;
		int menuItemId = MENU_OPTIONS;
		int menuItemOrder = Menu.NONE;	 
		int menuItemText = R.string.options;
		
		menu.add(groupId, menuItemId, menuItemOrder, menuItemText).setIcon(android.R.drawable.ic_menu_preferences);
		
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
			case MENU_OPTIONS:{
				Intent i = new Intent(this, HomeAlonePreferences.class); 
				startActivity(i);
				break;
			}
			
		}
	
		return true;
	}
    
    
    private void setButtonCaption()
    {	
    	if(PrefUtils.homeAloneEnabled(this) == false){
    		mActivateButton.setText(R.string.activate);
    	}else{
    		mActivateButton.setText(R.string.deactivate);
    	}
    }
    
    
    private void setupButtons()
    {

		// BUTTONS
		mActivateButton = (Button) findViewById(R.id.ActivateButton);
		
		mActivateButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
		    	if(PrefUtils.homeAloneEnabled(view.getContext()) == true){
		    		PrefUtils.setStatus(false, view.getContext());
		    	}else{
		    		if(PrefUtils.checkForwardingEnabled(view.getContext())){	
		    			PrefUtils.setStatus(true, view.getContext());
		    		}else{
		    			GeneralUtils.showErrorDialog(view.getContext().getString(R.string.forwarding_not_enabled), view.getContext());
		    		}
		    	}
    			setButtonCaption();
			}
		});
		setButtonCaption();	
    }
}