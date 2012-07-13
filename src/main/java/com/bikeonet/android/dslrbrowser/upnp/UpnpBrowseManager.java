package com.bikeonet.android.dslrbrowser.upnp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;

import android.util.Log;

import com.bikeonet.android.dslrbrowser.DslrBrowserApplication;

public class UpnpBrowseManager implements Runnable {

	private static final int DEFAULT_RUN_CYCLE_COUNT = 10;
	private static final String TAG = UpnpBrowseManager.class.getName();
	private static final ServiceId SERVICEID = new UDAServiceId(
			"ContentDirectory");
	private static UpnpBrowseManager instance = null;
	private HashMap<RemoteDevice, HashSet<String>> queue;
	private boolean loading = false;
	private int runCount = DEFAULT_RUN_CYCLE_COUNT;

	private UpnpBrowseManager() {
		queue = new HashMap<RemoteDevice, HashSet<String>>();
	}

	public synchronized void queueNode(RemoteDevice device, String node) {
		if ( queue.containsKey(device)) {
			queue.get(device).add(node);
		}
		else { 
			String[] nodes = {node};
			queue.put(device, new HashSet<String>(Arrays.asList(nodes)));
		}
	}
	
	public synchronized void removeDeviceQueue(RemoteDevice device) {
		if ( queue.containsKey(device)) {
			queue.remove(device);
			Log.i(TAG, device.getDisplayString()+ " removed from queue");
		}
	}
	
	public static synchronized UpnpBrowseManager getInstance() {
		if (instance == null) {
			setInstance(new UpnpBrowseManager());
		}
		
		return instance;
	}

	private static void setInstance(UpnpBrowseManager instance) {
		UpnpBrowseManager.instance = instance;
	}

	@Override
	public void run() {
		try {
			while (--runCount>=0) {				
				if ( !queue.isEmpty() ) {
					
					HashMap<RemoteDevice, HashSet<String>> pool = new HashMap<RemoteDevice, HashSet<String>>();
					pool.putAll(queue);
					queue.clear();
					
					for (final RemoteDevice device : pool.keySet()) {
						for (final String node : pool.get(device)) {
							Thread browseThread = new Thread(new Runnable(){

								@SuppressWarnings("rawtypes")
								@Override
								public void run() {
									Service contentDirectoryService = device
											.findService(SERVICEID);
									
									setLoading(true);
									DslrBrowserApplication
									.getInstance()
									.getUpnpService()
									.getControlPoint()
									.execute(new BrowseCallback(contentDirectoryService, node, UpnpBrowseManager.this, device));
									
									while( isLoading() ) {
										// dirty dirty
										// TODO: do some fancy ui progress updates here
										// while waiting for updateStatus callback
										try {
											Thread.sleep(10);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
								}
								
							});
							browseThread.setName(node);
							Log.i(TAG, "Starting thread " + browseThread.getName());
							browseThread.start();
							try {
								browseThread.join();
								Log.i(TAG, "Thread " + browseThread.getName() + " joined");								
							} catch (InterruptedException e1) {
								Log.e(TAG, "Thread " + browseThread.getName()
										+ " interrupted");
							}

							Log.i(TAG, "Sleeping for 10000 before next node");
							Thread.sleep(10000);
							
							
						}
						
						Log.i(TAG, "Finished browsing device "+device.getDetails().getFriendlyName());
					}
				}
				else {
					Thread.sleep(10000);
				}
			}
			
			if (!queue.isEmpty()) { 
				Log.i(TAG, "Finished default number of run cycles, giving up on device queues: ");
				for (RemoteDevice device : queue.keySet()) {
					Log.i(TAG, device.getDisplayString() + " "+ device.getDetails().getFriendlyName() + ": "+queue.get(device).size() + " nodes");
				}
			}
			setLoading(false);
			runCount = DEFAULT_RUN_CYCLE_COUNT;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

}
