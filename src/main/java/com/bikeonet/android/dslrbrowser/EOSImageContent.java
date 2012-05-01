package com.bikeonet.android.dslrbrowser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class EOSImageContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1292081566191145573L;
	private final String name;
	private final HashMap<String, String> imageList;
	
	public static final String SIZE_5184x3456 = "5184x3456";
	public static final String SIZE_160x120 = "160x120";
	public static final String SIZE_640x480 = "640x480";
	public static final String SIZE_UNDEF = "undefined";
	
	public EOSImageContent(String name) {
		this.name = name;
		imageList = new HashMap<String, String>();
	}
	
	public String getName() {
		return name;
	}
	
	public Set<String> getSizes() { 
		return imageList.keySet();
	}
	
	public boolean hasSize(String size) { 
		return imageList.containsKey(size);
	}
	
	public String getSize(String size) {
		return imageList.get(size);
	}
	
	public void putSize(String size, String image) {  
		imageList.put(size, image);		
	}

	@Override
	public boolean equals(Object obj) {
		
		if ( this == obj )
			return true;
		
		if ( obj == null || getClass() != obj.getClass())
			return false;
		
		EOSImageContent other = (EOSImageContent) obj;
		
		return getName().equals(other.getName());
		
	}
	
	
	
}
