package com.fede;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.fede.Utilities.GeneralUtils;
import com.fede.Utilities.PrefUtils;
import com.fede.wizard.StartWizard;

import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 10/25/12
 * Time: 11:24 PM
 */
public class FirstActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private class EventsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(HomeAloneService.STATE_CHANGED)){
                invalidateOptionsMenu();
            }
            /*
            if(intent.getAction().equals(HomeAloneService.HOMEALONE_EVENT_PROCESSED)){
                mEventCursor.requery(); // TODO Cursor deprecated
            }*/
        }
    }

    private Cursor mEventCursor;
	private java.text.DateFormat mDateFormat;
	private java.text.DateFormat mTimeFormat;

	private IntentFilter mEventFilter;
    private IntentFilter mStatusFilter;
    private EventsReceiver mReceiver;
    private EventListAdapter mEventsAdapter;
    private ListView mList;

	private static final int MENU_DELETE = Menu.FIRST;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event_list);

        mDateFormat = android.text.format.DateFormat.getDateFormat(this);    // short date
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);    // 12/24 time

        mEventFilter = new IntentFilter(HomeAloneService.HOMEALONE_EVENT_PROCESSED);
        mStatusFilter = new IntentFilter(HomeAloneService.STATE_CHANGED);
        mReceiver = new EventsReceiver();

        getSupportLoaderManager().initLoader(0, null, this);

        mEventsAdapter = new EventListAdapter(this, null);

        mList = (ListView) findViewById(R.id.event_list);
        mList.setAdapter(mEventsAdapter);
    }

	@Override
	protected void onPause() {
		super.onPause();
        unregisterReceiver(mReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// REMOVE ME registerReceiver(mReceiver, mEventFilter);
        registerReceiver(mReceiver, mStatusFilter);

		// mEventCursor.requery();
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if(PrefUtils.homeAloneEnabled(this)){
            menu.findItem(R.id.main_menu_toggle).setIcon(R.drawable.ic_button_on);
        }else{
            menu.findItem(R.id.main_menu_toggle).setIcon(R.drawable.ic_button_off);
        }

        return true;
    }

    /**
     * Gets called when the user presses the toggle button
     */
    private void onStatusToggled(){
        if(PrefUtils.homeAloneEnabled(this)){
            PrefUtils.setStatus(false, this);
        }else{
            if(PrefUtils.checkForwardingEnabled(this)){
                PrefUtils.setStatus(true, this);
            }else{
                GeneralUtils.showErrorDialog(getString(R.string.forwarding_not_enabled), this);
            }
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
            case R.id.main_menu_clean:
                DroidContentProviderClient.removeAllEvent(this);
				// REMOVE ME mEventCursor.requery(); // TODO New check
            break;
            case R.id.main_menu_help:
                Intent i = new Intent(this, HomeAloneHelp.class);
                startActivity(i);
            break;
            case R.id.main_menu_option:
                Intent i1 = new Intent(this, HomeAlonePreferences.class);
                startActivity(i1);
            break;
            case R.id.main_menu_toggle:
                onStatusToggled();
                break;
            case R.id.main_menu_wizard:
                launchWizard();
            break;
		}

		return true;
	}


    private void launchWizard(){	// Will this throw a magician?
        Intent i = new Intent(this, StartWizard.class);
        startActivity(i);
    }


	public void showDialog(String message, String title)
	{
    	String button1String = getString(R.string.ok_name);
    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
    	ad.setTitle(title);
    	ad.setMessage(message);
    	ad.setPositiveButton(button1String,
    						 new DialogInterface.OnClickListener() {
	    						public void onClick(DialogInterface dialog, int arg1) {
	    							// do nothing
	    						} });
    	ad.show();
    	return;
	}



    /*   TODO
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c = DroidContentProviderClient.getEvent(id, this);
		c.moveToFirst();
		String fullStatus = c.getString(DroidContentProvider.EVENT_DESCRIPTION_COLUMN_POSITION);
		String shortStatus = c.getString(DroidContentProvider.EVENT_SHORTDESC_COLUMN_POSITION);
		long time = c.getLong(DroidContentProvider.EVENT_TIME_COLUMN_POSITION);
		String timeString = mTimeFormat.format(time);
		String dateString = mDateFormat.format(time);

		String message = String.format("%s\n%s\n%s", dateString, timeString, fullStatus);
		showDialog(message, shortStatus);
	}*/


    class EventListAdapter extends CursorAdapter {

        Context mContext;
        private LayoutInflater mInflater;

        public EventListAdapter(Context context, Cursor c) {
            super(context, c, true);
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }


        @Override
        protected synchronized void onContentChanged() {
        	super.onContentChanged();
        }

        private boolean isDateToday(Date date) {

            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            Calendar today = Calendar.getInstance();
            if (
                    today.get(Calendar.YEAR) - 1900 == dateCalendar.get(Calendar.YEAR)&&
                    today.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH)&&
                    today.get(Calendar.DAY_OF_MONTH) == dateCalendar.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
            return false;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Reset the view (in case it was recycled) and prepare for binding

            TextView dateView = (TextView) view.findViewById(R.id.event_elem_time);
            long timestamp = cursor.getLong(DroidContentProvider.EVENT_TIME_COLUMN_POSITION);
            Date date = new Date(timestamp);
            String text = "";
            if (isDateToday(date)) {
                text = mTimeFormat.format(date);
            } else {
                text = mDateFormat.format(date);
            }
            dateView.setText(text);

            TextView eventDescView = (TextView) view.findViewById(R.id.event_elem_desc);
            String desc = cursor.getString(DroidContentProvider.EVENT_SHORTDESC_COLUMN_POSITION);

            eventDescView.setText(desc);

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.event_list_elem, parent, false);
        }

    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = null;
        String where = null;
        String[] whereArgs = null;
        String sortOrder = null;

        // Query URI
        Uri queryUri = DroidContentProvider.EVENT_URI;

        return new android.support.v4.content.CursorLoader(FirstActivity.this, queryUri, projection, where, whereArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> objectLoader, Cursor c) {
        mEventsAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> objectLoader) {
        mEventsAdapter.swapCursor(null);
    }


}