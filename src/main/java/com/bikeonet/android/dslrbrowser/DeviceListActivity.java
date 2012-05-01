package com.bikeonet.android.dslrbrowser;

import java.io.File;
import java.io.FilenameFilter;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.message.header.STAllHeader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends ListActivity {

	private static String TAG = DeviceListActivity.class.getSimpleName();
	private DeviceAdapter listAdapter;
	private TextView ssidText;

	public DeviceAdapter getListAdapter() {
		return listAdapter;
	}

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		DslrBrowserApplication.getInstance().setDeviceListInstance(this);
		listAdapter = new DeviceAdapter(this, R.layout.devices_item);

		setListAdapter(listAdapter);

//		getApplicationContext().bindService(
//				new Intent(this, AndroidContentManagerUpnpServiceImpl.class),
//				serviceConnection, Context.BIND_AUTO_CREATE);

		getLayoutInflater().inflate(R.layout.devicelistempty,
				(ViewGroup) getListView().getParent(), true);
		View emptyView = findViewById(R.id.empty);
		ImageButton searchButton = (ImageButton) findViewById(R.id.emptySearchButton1);
		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				launchNewSearch();
			}
		});
		
		findViewById(R.id.emptyShowCacheView).setVisibility(hasCachedContent()?View.VISIBLE:View.GONE);
		ImageButton viewGallery = (ImageButton) findViewById(R.id.emptyDownloadedImagesButton1);
		viewGallery.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if ( hasCachedContent() )
					viewGallery();
					else
						Toast.makeText(DeviceListActivity.this, "No cached content", Toast.LENGTH_SHORT).show();
			}
		});
		ssidText = (TextView) findViewById(R.id.emptyText3);

		getListView().setEmptyView(emptyView);

		boolean refresh = DslrBrowserApplication.getInstance().isRefreshDeviceList();
		if ( refresh ) { 
			insertNewDevices();
		}
		
	}
	
	@Override
	protected void onDestroy() {
//		if (upnpService != null) {
//			upnpService.getRegistry().removeListener(registryListener);
//		}
		
		DslrBrowserApplication.getInstance().setDeviceListInstance(null);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		DslrBrowserApplication.getInstance().setDeviceListInstance(this);
		
		findViewById(R.id.emptyShowCacheView).setVisibility(hasCachedContent()?View.VISIBLE:View.GONE);
		
//		getApplicationContext().bindService(
//				new Intent(this, AndroidContentManagerUpnpServiceImpl.class),
//				serviceConnection, Context.BIND_AUTO_CREATE);
		boolean refresh = DslrBrowserApplication.getInstance().isRefreshDeviceList();
		if ( refresh ) { 
			insertNewDevices();
		}
		
		super.onResume();
	}

	private void insertNewDevices() {
		for( DeviceDisplay d : DslrBrowserApplication.getInstance().getDevicePool()) { 
			listAdapter.add(d);
		}
		DslrBrowserApplication.getInstance().getDevicePool().clear();
	}

	@Override
	protected void onPause() {
		DslrBrowserApplication.getInstance().setDeviceListInstance(null);
		
//		if (upnpService != null) {
//			upnpService.getRegistry().removeListener(registryListener);
//		}
//		getApplicationContext().unbindService(serviceConnection);
		
		super.onPause();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Search").setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 1, 1, "Settings").setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, 2, 2, "Gallery").setIcon(
				android.R.drawable.ic_menu_upload);
		return true;
	}

	private boolean hasCachedContent() {
		File pdir = Environment.getExternalStorageDirectory();
		File dcim = new File(pdir.getAbsolutePath() + "/DCIM/"+ImageGalleryActivity.DOWNLOAD_DIRECTORY);
		if (dcim.isDirectory()) {
			File[] files = dcim.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.toUpperCase().endsWith("JPG")) {
						return true;
					}
					return false;
				}
			});
			if (files.length > 0) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AndroidUpnpService upnpService = null;
		if ( DslrBrowserApplication.getInstance() != null ) {
			upnpService = DslrBrowserApplication.getInstance().getUpnpService();
		}
		
		if (item.getItemId() == 0 && upnpService != null) {
			launchNewSearch();
		}

		if (item.getItemId() == 1) {
			startActivity(new Intent(DeviceListActivity.this,
					PreferencesActivity.class));
		}

		if (item.getItemId() == 2) { 
			if ( hasCachedContent() )
			viewGallery();
			else
				Toast.makeText(this, "No cached content", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private void launchNewSearch() {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Restart service?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {				 			  
			   			Toast.makeText(DeviceListActivity.this,
								"Searching for devices...", Toast.LENGTH_SHORT).show();
			   			
			   			DslrBrowserApplication.getInstance().doUnBindService();					
						listAdapter.clear();
						DslrBrowserApplication.getInstance().getDevicePool().clear();					
						DslrBrowserApplication.getInstance().doBindService();
						setSSID(DslrBrowserApplication.getInstance().getSsid());
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
			   			AndroidUpnpService upnpService = DslrBrowserApplication.getInstance().getUpnpService();
						if ( upnpService != null ) {
							Toast.makeText(DeviceListActivity.this,
									"Searching for devices...", Toast.LENGTH_SHORT).show();
							listAdapter.clear();							
							upnpService.getRegistry().removeAllRemoteDevices();
							upnpService.getControlPoint().search(new STAllHeader());				
				   		} else {
							Toast.makeText(DeviceListActivity.this,
									"Waiting for service to show up...", Toast.LENGTH_SHORT)
									.show();
						}
		           }
		       });
		AlertDialog alert = builder.create();	
		alert.show();
	}

	protected void viewGallery() {
		startActivity(new Intent(DeviceListActivity.this,
				CachedImagesGalleryActivity.class));		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "Clicked on " + position + " : " + id);
		Log.i(TAG, "Listing thumbnail images");
		DeviceDisplay d = (DeviceDisplay) l.getAdapter().getItem(position);

		if (d.isCanon() || browseOther()) {

			for (EOSImageContent image : d.getImages()) {
				Log.i(TAG, image.getName());
				if (image.hasSize(EOSImageContent.SIZE_160x120)) {
					Log.i(TAG, image.getSize(EOSImageContent.SIZE_160x120));
				}
			}

			if (d.getImages().size() > 0) {

				Toast.makeText(
						DeviceListActivity.this,
						"Device "
								+ d.getDevice().getDetails().getFriendlyName()
								+ "has " + d.getImages().size() + " images.",
						Toast.LENGTH_SHORT).show();

				Intent gallery = new Intent(DeviceListActivity.this,
						ImageGalleryActivity.class);
				gallery.putExtra("EOSIMAGES", d.getImages());
				gallery.putExtra("EOSDEVICE", d.getDevice().getDisplayString());
				DeviceListActivity.this.startActivity(gallery);

			} else {

				Toast.makeText(
						DeviceListActivity.this,
						d.getDevice().getDetails().getFriendlyName()
								+ " has no images.", Toast.LENGTH_SHORT).show();

			}
		} else {
			Toast.makeText(
					DeviceListActivity.this,
					d.getDevice().getDetails().getFriendlyName()
							+ " is not a Canon camera device.",
					Toast.LENGTH_SHORT).show();

		}

	}

	private boolean browseOther() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		return prefs.getBoolean("browse_non_canon", false);
	}
	
	public void setSSID(String ssid) { 
		
		ssidText.setText(ssid);		
		
	}

}
