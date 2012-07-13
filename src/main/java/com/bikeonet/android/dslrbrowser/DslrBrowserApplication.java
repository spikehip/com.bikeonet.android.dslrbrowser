package com.bikeonet.android.dslrbrowser;

import java.util.ArrayList;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.registry.RegistryListener;

import com.bikeonet.android.dslrbrowser.data.DeviceDisplay;
import com.bikeonet.android.dslrbrowser.upnp.AndroidContentManagerUpnpServiceImpl;
import com.bikeonet.android.dslrbrowser.upnp.BrowseRegistryListener;
import com.bikeonet.android.dslrbrowser.upnp.UpnpBrowseManager;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DslrBrowserApplication extends Application {
	
	private final static String TAG = DslrBrowserApplication.class.getName();
	private static DslrBrowserApplication instance;
	
	private DeviceListActivity deviceListInstance = null;
	
	private ArrayList<DeviceDisplay> devicePool = new ArrayList<DeviceDisplay>();
	
	private boolean mIsBound = false;
	private boolean refreshDeviceList = false;
	private String ssid;
	private AndroidUpnpService upnpService;	
	public AndroidUpnpService getUpnpService() {
		return upnpService;
	}
	
	private Thread browseThread = null;	
	private Location lastLocation;

	private RegistryListener registryListener = new BrowseRegistryListener();
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			upnpService = (AndroidUpnpService) service;
			upnpService.getRegistry().addListener(registryListener);
			browse();
		}

		public void onServiceDisconnected(ComponentName name) {
			upnpService = null;
		}

	};

	private LocationManager locationManager;
		
	@Override
	public void onCreate() {
		doInitializeLocationManager();
		doBindService();
		instance = this;
		super.onCreate();
	}
	
	private void doInitializeLocationManager() {
		
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new LocationListener() {

					@Override
					public void onLocationChanged(Location location) {
						setLastLocation(location);						
					}

					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
						Log.i(TAG, provider + " status changed "+status);

					}
					
					/**
					 * 07-12 10:55:22.208: I/com.bikeonet.android.dslrbrowser.DslrBrowserApplication(1431): gps disabled
					 * 07-12 10:55:33.349: I/com.bikeonet.android.dslrbrowser.DslrBrowserApplication(1431): gps enabled
					 */
					
					@Override
					public void onProviderEnabled(String provider) {
						Log.i(TAG, provider + " enabled");
					}

					@Override
					public void onProviderDisabled(String provider) {
						Log.i(TAG, provider + " disabled");
					}


				});
	}

	@Override
	public void onTerminate() {
		doUnBindService();
		NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(R.string.local_service_started);		
		super.onTerminate();
	}
	
	public static DslrBrowserApplication getInstance() { 
		return instance;
	}

	public void doBindService() { 
		bindService(
				new Intent(this, AndroidContentManagerUpnpServiceImpl.class),
				serviceConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	public void doUnBindService() { 
		if ( mIsBound ) { 
			if ( serviceConnection != null ) { 
			}
			unbindService(serviceConnection);
			mIsBound = false;
		}
	}

	public void setDeviceListInstance(DeviceListActivity deviceListInstance) {
		this.deviceListInstance = deviceListInstance;
	}

	public DeviceListActivity getDeviceListInstance() {
		return deviceListInstance;
	}

	public void setRefreshDeviceList(boolean refreshDeviceList) {
		this.refreshDeviceList = refreshDeviceList;
	}

	public boolean isRefreshDeviceList() {
		return refreshDeviceList;
	}

	public ArrayList<DeviceDisplay> getDevicePool() {
		return devicePool;
	}
	
	public void addToDevicePool(DeviceDisplay d) {
		devicePool.add(d);
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}

	/**
	 * Starts a new browse thread for the UpnpBrowseManager singleton if not already running
	 */
	public void browse() {
		if  ( browseThread==null || !browseThread.isAlive() ) { 
			browseThread = new Thread(UpnpBrowseManager.getInstance());
			browseThread.start();
		}
	}
	
	
}
