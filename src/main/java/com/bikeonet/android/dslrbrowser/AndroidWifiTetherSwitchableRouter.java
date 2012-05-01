package com.bikeonet.android.dslrbrowser;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.protocol.ProtocolFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class AndroidWifiTetherSwitchableRouter extends
		AndroidWifiSwitchableRouter {

	private static final String TAG = AndroidWifiTetherSwitchableRouter.class
			.getName();
	
	protected boolean isTethered = false;

	public AndroidWifiTetherSwitchableRouter(
			UpnpServiceConfiguration configuration,
			ProtocolFactory protocolFactory, WifiManager wifiManager,
			ConnectivityManager connectivityManager) {

		super(configuration, protocolFactory, wifiManager, connectivityManager);

		NetworkInfo wifiInfo = getConnectivityManager().getNetworkInfo(
				ConnectivityManager.TYPE_WIFI);
		if (!wifiInfo.isConnected() && !ModelUtil.ANDROID_EMULATOR) {
			Log.i(TAG, "WiFi not connected, checking for tether mode");

			NetworkInterface wifiInterface = AndroidTetherNetworkAddressFactory
					.getRealWifiNetworkInterface(wifiManager);
			if (wifiInterface != null) {
				Log.i(TAG,
						"Wifi interface name: "
								+ wifiInterface.getDisplayName());
				try {
					if (wifiInterface.isUp()) {
						Log.i(TAG, "WifiInterface is UP");
						isTethered = true;
						enable();
					} else {
						Log.i(TAG, "WifiInterface is DOWN");
					}
				} catch (SocketException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				}
			}
			else { 
				Log.i(TAG, "No wifi interface detected");
			}
		}
	}
	
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) return;
            NetworkInfo wifiInfo = getConnectivityManager().getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            // We can't listen to "is available" or simply "is switched on", we have to make sure it's connected
            if (!wifiInfo.isConnected()) {            	
                if (!isTethered()) {
                    Log.i(TAG,"WiFi state changed, trying to disable router");
                	disable();
                }
                else {
                    Log.i(TAG,"WiFi state changed, but detected tether, not disabling router");
                }
            } else {
                Log.i(TAG,"WiFi state changed, trying to enable router");
                enable();
            }
        }
    };
	
    public boolean isTethered() { 
    	return this.isTethered;
    }

}
