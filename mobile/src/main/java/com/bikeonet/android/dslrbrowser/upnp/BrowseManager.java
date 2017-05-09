package com.bikeonet.android.dslrbrowser.upnp;

import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class BrowseManager implements Runnable {


    private static final int DEFAULT_RUN_CYCLE_COUNT = 10;
    private static final String TAG = BrowseManager.class.getName();
    private static final ServiceId SERVICEID = new UDAServiceId(
            "ContentDirectory");
    private static BrowseManager instance = null;
    private HashMap<RemoteDevice, HashSet<String>> queue;
    private boolean loading = false;
    private int runCount = DEFAULT_RUN_CYCLE_COUNT;
    private final AndroidUpnpService upnpService;

    private BrowseManager(AndroidUpnpService upnpService) {
        this.upnpService = upnpService;
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

    public static void initializeInstance(AndroidUpnpService upnpService) {
        instance = new BrowseManager(upnpService);
    }

    public static BrowseManager getInstance() {
        return new BrowseManager(instance.upnpService);
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

                                    if (contentDirectoryService != null) {
                                        BrowseManager.this.upnpService
                                                .getControlPoint()
                                                .execute(new BrowseCallback(contentDirectoryService, node, BrowseManager.this, device));
                                    }
                                    else {
                                        setLoading(false);
                                    }

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

//                            Log.i(TAG, "Sleeping for 10000 before next node");
//                            Thread.sleep(10000);
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
