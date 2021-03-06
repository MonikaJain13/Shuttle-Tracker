/* 
 * Copyright 2011 Austin Wagner
 *     
 * This file is part of Mobile Shuttle Tracker.
 *
 *  Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.abstractedsheep.shuttletracker;

import java.util.ArrayList;

import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.TrackerMapActivity;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.abstractedsheep.shuttletrackerworld.Netlink;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import com.abstractedsheep.shuttletrackerworld.World;

/*
 * !!! All children of this activity MUST implement IShuttleServiceCallback, but the implementation may be empty !!!
 */
public class TrackerTabActivity extends TabActivity implements IShuttleServiceCallback {
	private TabHost tabHost;
	private ShuttleDataService dataService;
	
	@Override
	protected void onPause() {
		super.onPause();
		
		dataService.active.set(false);
		dataService.unregisterCallback(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		dataService.active.set(true);
		dataService.registerCallback(this);
		new Thread(dataService.updateShuttles).start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.tab);
		setProgressBarIndeterminateVisibility(true);
		
		this.tabHost = getTabHost();
		
		// Add the map activity as a tab
		TabSpec tab = this.tabHost.newTabSpec("map");
		Intent i = new Intent(this, TrackerMapActivity.class);
		tab.setContent(i);
		tab.setIndicator("Map", getResources().getDrawable(R.drawable.map));
		this.tabHost.addTab(tab);
		
		if (savedInstanceState != null)
			this.tabHost.setCurrentTab(savedInstanceState.getInt("open_tab", 0));
			
		dataService = ShuttleDataService.getInstance();
		dataService.setApplicationContext(getApplicationContext());
		new Thread(dataService.updateRoutes).start();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("open_tab", this.tabHost.getCurrentTab());
		
		super.onSaveInstanceState(outState);
	}
	
	final Runnable hideIndeterminateProgress = new Runnable() {
		public void run() {
			setProgressBarIndeterminateVisibility(false);
		}
	};

	public void dataUpdated(World world) {
		((IShuttleServiceCallback)getLocalActivityManager().getCurrentActivity()).dataUpdated(world);
		
	}

	public void routesUpdated(World world) {
		((IShuttleServiceCallback)getLocalActivityManager().getCurrentActivity()).routesUpdated(world);
		if (world != null) {
			runOnUiThread(hideIndeterminateProgress);
		}
	}
	
	private class MakeToast implements Runnable {
		private final int errorCode;
		public MakeToast(int errorCode) {
			this.errorCode = errorCode;
		}
		public void run() {
			switch (errorCode) {
			case IShuttleServiceCallback.NO_CONNECTION_ERROR:
				Toast.makeText(TrackerTabActivity.this, R.string.no_conn, 10000).show();
				break;
			}
		}
	}

	public void dataServiceError(int errorCode) {
		runOnUiThread(new MakeToast(errorCode));
		((IShuttleServiceCallback)getLocalActivityManager().getCurrentActivity()).dataServiceError(errorCode);
	}

	
	public void showMap(String stopId) {
		tabHost.setCurrentTab(1);
		((TrackerMapActivity)getLocalActivityManager().getCurrentActivity()).displayStop(stopId);
	}
}
