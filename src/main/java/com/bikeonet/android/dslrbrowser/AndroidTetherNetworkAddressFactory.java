package com.bikeonet.android.dslrbrowser;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.teleal.cling.model.Constants;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.transport.spi.InitializationException;
import org.teleal.cling.transport.spi.NetworkAddressFactory;

import android.net.wifi.WifiManager;
import android.util.Log;

public class AndroidTetherNetworkAddressFactory implements NetworkAddressFactory {
	
	private static final String TAG = AndroidTetherNetworkAddressFactory.class.getName();
    protected NetworkInterface wifiInterface;
    protected List<InetAddress> bindAddresses = new ArrayList<InetAddress>();

	public AndroidTetherNetworkAddressFactory(WifiManager wifiManager)
			throws InitializationException {
        wifiInterface = getWifiNetworkInterface(wifiManager);

        if (wifiInterface == null)
            throw new InitializationException("Could not discover WiFi network interface");
        Log.i(TAG, "Discovered WiFi network interface: " + wifiInterface.getDisplayName());

        discoverBindAddresses();
		
		for (InetAddress address : bindAddresses) { 
			Log.i(TAG, "discovered address: "+address.getHostAddress());
		}
	}
    protected void discoverBindAddresses() throws InitializationException {
        try {

            Log.i(TAG, "Discovering addresses of interface: " + wifiInterface.getDisplayName());
            for (InetAddress inetAddress : getInetAddresses(wifiInterface)) {
                if (inetAddress == null) {
                    Log.w(TAG,"Network has a null address: " + wifiInterface.getDisplayName());
                    continue;
                }

                if (isUsableAddress(inetAddress)) {
                    Log.i(TAG, "Discovered usable network interface address: " + inetAddress.getHostAddress());
                    bindAddresses.add(inetAddress);
                } else {
                    Log.i(TAG,"Ignoring non-usable network interface address: " + inetAddress.getHostAddress());
                }
            }

        } catch (Exception ex) {
            throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
        }
    }
    
    protected boolean isUsableAddress(InetAddress address) {
        if (!(address instanceof Inet4Address)) {
            Log.i(TAG,"Skipping unsupported non-IPv4 address: " + address);
            return false;
        }
        return true;
    }
    
    protected List<InetAddress> getInetAddresses(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses());
    }
    
    public static NetworkInterface getWifiNetworkInterface(WifiManager manager) {
        if (ModelUtil.ANDROID_EMULATOR) {
        	//TODO: not interested in emulator mode yet, get stuff running on real device 
            //return getEmulatorWifiNetworkInterface(manager);
        	return null;
        }
        return AndroidTetherNetworkAddressFactory.getRealWifiNetworkInterface(manager);
    }
	
	public static NetworkInterface getRealWifiNetworkInterface(WifiManager manager) {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            //the WiFi network interface will be one of these.
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Log.i(TAG,"No network interfaces available");
            return null;
        }
        //We'll use the WiFiManager's ConnectionInfo IP address and compare it with
        //the ips of the enumerated NetworkInterfaces to find the WiFi NetworkInterface.

        //Wifi manager gets a ConnectionInfo object that has the ipAdress as an int
        //It's endianness could be different as the one on java.net.InetAddress
        //maybe this varies from device to device, the android API has no documentation on this method.
        int wifiIP = manager.getConnectionInfo().getIpAddress();

        //so I keep the same IP number with the reverse endianness
        int reverseWifiIP = Integer.reverseBytes(wifiIP);

        while (interfaces.hasMoreElements()) {

            NetworkInterface iface = interfaces.nextElement();
            String interfaceName = iface.getDisplayName();
            Log.i(TAG, iface.getDisplayName());

            //since each interface could have many InetAddresses...
            Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress nextElement = inetAddresses.nextElement();
                Log.i(TAG, interfaceName + ": "+nextElement.getHostAddress());
                int byteArrayToInt = byteArrayToInt(nextElement.getAddress(), 0);

                if ( iface.getDisplayName().equals("lo") || ( wifiIP == 0 && reverseWifiIP == 0 && byteArrayToInt == 0) ) {
                	//skip for loopback
                	Log.i(TAG, "Skipping loopback interface");
                	continue;
                }
                
                //grab that IP in byte[] form and convert it to int, then compare it
                //to the IP given by the WifiManager's ConnectionInfo. We compare
                //in both endianness to make sure we get it.
                if (byteArrayToInt == wifiIP || byteArrayToInt == reverseWifiIP) {
                    return iface;
                }
                
                //detect tether
                if (interfaceName.startsWith("wlan") && nextElement.getHostAddress().startsWith("192.168")) {
                	Log.i(TAG, "Detected possible tethered interface");
                	return iface;
                }
            }
        }
        
        
		
		
    	return null;
    }
	
    static int byteArrayToInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 4)
            return -1;

        int r0 = (arr[offset] & 0xFF) << 24;
        int r1 = (arr[offset + 1] & 0xFF) << 16;
        int r2 = (arr[offset + 2] & 0xFF) << 8;
        int r3 = arr[offset + 3] & 0xFF;
        return r0 + r1 + r2 + r3;
    }

	@Override
	public InetAddress getMulticastGroup() {
        try {
            return InetAddress.getByName(Constants.IPV4_UPNP_MULTICAST_GROUP);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
	}

	@Override
	public int getMulticastPort() {
        return Constants.UPNP_MULTICAST_PORT;
	}

	@Override
	public int getStreamListenPort() {
		return 0;
	}

	@Override
	public NetworkInterface[] getNetworkInterfaces() {
		return new NetworkInterface[] { wifiInterface };
	}

	@Override
	public InetAddress[] getBindAddresses() {
		return bindAddresses.toArray(new InetAddress[bindAddresses.size()]);
	}

	@Override
	public byte[] getHardwareAddress(InetAddress inetAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetAddress getBroadcastAddress(InetAddress inetAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetAddress getLocalAddress(NetworkInterface networkInterface,
			boolean isIPv6, InetAddress remoteAddress)
			throws IllegalStateException {
        // TODO: This is totally random because we can't access low level InterfaceAddress on Android!
        for (InetAddress localAddress : getInetAddresses(networkInterface)) {
            if (isIPv6 && localAddress instanceof Inet6Address)
                return localAddress;
            if (!isIPv6 && localAddress instanceof Inet4Address)
                return localAddress;
        }
        throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
	}

}
