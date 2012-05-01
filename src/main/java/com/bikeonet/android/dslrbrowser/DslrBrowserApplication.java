package com.bikeonet.android.dslrbrowser;

import java.util.ArrayList;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.registry.RegistryListener;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DslrBrowserApplication extends Application {
	
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

	private RegistryListener registryListener = new BrowseRegistryListener();
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			upnpService = (AndroidUpnpService) service;
//			listAdapter.clear();
//			for (Device device : upnpService.getRegistry().getDevices()) {
//				((BrowseRegistryListener) registryListener).deviceAdded(device,
//						null);
//			}
			upnpService.getRegistry().addListener(registryListener);
		}

		public void onServiceDisconnected(ComponentName name) {
			upnpService = null;
		}

	};
		
	@Override
	public void onCreate() {
		doBindService();
		
		instance = this;
		
		super.onCreate();
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
	
	
}
