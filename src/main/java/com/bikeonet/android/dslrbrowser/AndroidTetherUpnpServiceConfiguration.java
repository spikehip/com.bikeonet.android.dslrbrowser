package com.bikeonet.android.dslrbrowser;

import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.transport.spi.NetworkAddressFactory;

import android.net.wifi.WifiManager;

public class AndroidTetherUpnpServiceConfiguration extends
		AndroidUpnpServiceConfiguration {

	public AndroidTetherUpnpServiceConfiguration(WifiManager wifiManager) {
		super(wifiManager, 0);
	}

	@Override
	protected NetworkAddressFactory createNetworkAddressFactory(
			int streamListenPort) {
		return new AndroidTetherNetworkAddressFactory(wifiManager);
	}
	
	@Override
	public ServiceType[] getExclusiveServiceTypes() {
		
		return new ServiceType[] { 
			new UDAServiceType("ContentDirectory")
		};
		
	} 
	
	
}
