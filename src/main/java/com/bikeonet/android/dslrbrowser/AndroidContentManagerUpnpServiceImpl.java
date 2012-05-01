package com.bikeonet.android.dslrbrowser;

import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class AndroidContentManagerUpnpServiceImpl extends
		AndroidUpnpServiceImpl {
	
	private NotificationManager mNM;
	private int NOTIFICATION = R.string.local_service_started;
	private boolean isReceiverRegistered = false;

	
	
 	@Override
	protected AndroidUpnpServiceConfiguration createConfiguration(
			WifiManager wifiManager) {
		
 		DslrBrowserApplication app  = DslrBrowserApplication.getInstance();
 		
 		if (app!=null && wifiManager != null && wifiManager.getConnectionInfo()!=null && app.getDeviceListInstance()!=null) { 
 			app.getDeviceListInstance().setSSID(wifiManager.getConnectionInfo().getSSID());
 		}
 		
		return new AndroidTetherUpnpServiceConfiguration(wifiManager);
		
	}

 	
 	@Override
 	public void onCreate() {
 		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		showNotification();
// 		super.onCreate();
 		
        final WifiManager wifiManager =
            (WifiManager) getSystemService(Context.WIFI_SERVICE);

        final ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 		upnpService = new UpnpServiceImpl(createConfiguration(wifiManager)) {

			@Override
			protected Router createRouter(ProtocolFactory protocolFactory,
					Registry registry) {
				AndroidWifiTetherSwitchableRouter router = 
					new AndroidWifiTetherSwitchableRouter(getConfiguration(), protocolFactory, wifiManager, connectivityManager);
								
				if (router.isTethered()) {
					DslrBrowserApplication app  = DslrBrowserApplication.getInstance();
					if (app!=null && app.getDeviceListInstance()!=null) {						
						app.getDeviceListInstance().setSSID("Connect to your phone's shared WiFi!");
					}
				}
				
                if (!router.isTethered && !ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges()) {
                    // Only register for network connectivity changes if we are not running on emulator or not running tethered
                    registerReceiver(
                            router.getBroadcastReceiver(),
                            new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
                    );
                    isReceiverRegistered = true;
                }
                
				return router;
			}
 			
 		};
 	}
 	
    @Override
    public void onDestroy() {
        if (isReceiverRegistered && !ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges())
            unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());
        upnpService.shutdown();
    }
 	
    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Browsing for Canon DSLR cameras on your network...";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DeviceListActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "DSLR Browser service",
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
	
	
}
