package com.bikeonet.android.dslrbrowser.util;

import java.util.HashMap;

public class CanonDeviceRootNodes {

	private final HashMap<String,String> rootNodes = new HashMap<String,String>();
	private static CanonDeviceRootNodes instance; 
	
	private CanonDeviceRootNodes() {
		/* 7D */
		rootNodes.put("7D", "C/1/100/");
	}
	
	public String getRootNode(String deviceFriendlyName) {
		for(String key: rootNodes.keySet()) {
			if (deviceFriendlyName.contains(key)) {
				return rootNodes.get(key);
			}
		}
		return "0";
	}
	
	public synchronized static CanonDeviceRootNodes getInstance() {
		if ( instance == null ) { 
			instance = new CanonDeviceRootNodes();
		}
		
		return instance;
	}
}
