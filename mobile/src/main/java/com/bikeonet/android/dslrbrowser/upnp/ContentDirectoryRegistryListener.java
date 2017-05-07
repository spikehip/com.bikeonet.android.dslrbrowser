package com.bikeonet.android.dslrbrowser.upnp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bikeonet.android.dslrbrowser.MainActivity;
import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.CameraList;
import com.bikeonet.android.dslrbrowser.messaging.LocalBroadcastMessageBuilder;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class ContentDirectoryRegistryListener implements RegistryListener {

    private final Context mContext;

    public ContentDirectoryRegistryListener(Context context) {
        this.mContext = context;
    }

    /**
     * Called as soon as possible after a device has been discovered.
     * <p>
     * This method will be called after SSDP notification datagrams of a new alive
     * UPnP device have been received and processed. The announced device XML descriptor
     * will be retrieved and parsed. The given {@link RemoteDevice} metadata
     * is validated and partial {@link Service} metadata is available. The
     * services are unhydrated, they have no actions or state variable metadata because the
     * service descriptors of the device model have not been retrieved at this point.
     * </p>
     * <p>
     * You typically do not use this method on a regular machine, this is an optimization
     * for slower UPnP hosts (such as Android handsets).
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with anemic service metadata.
     */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        Log.d(this.getClass().getName(),"Android UPNP Service remote device discovery started" + device.toString());
    }

    /**
     * Called when service metadata couldn't be initialized.
     * <p>
     * If you override the {@link #remoteDeviceDiscoveryStarted(Registry, RemoteDevice)}
     * method, you might want to override this method as well.
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with anemic service metadata.
     * @param ex       The reason why service metadata could not be initialized, or <code>null</code> if service
     */
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        Log.d(this.getClass().getName(),"Android UPNP Service remote device discovery failed " + device.toString());
    }

    /**
     * Called when complete metadata of a newly discovered device is available.
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with complete service metadata.
     */
    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        boolean showCanonOnly = prefs.getBoolean("show_canon_only", true);
        CameraItem cameraItem = new CameraItem(device);
        if ( showCanonOnly && !cameraItem.isCanon() ) {
            //we are interested only in canon
        }
        else {
            CameraList.ITEMS.add(cameraItem);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(LocalBroadcastMessageBuilder.buildCameraListNewContentMessage());
        }
    }

    /**
     * Called when a discovered device's expiration timestamp is updated.
     * <p>
     * This is a signal that a device is still alive and you typically don't have to react to this
     * event. You will be notified when a device disappears through timeout.
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with complete service metadata.
     */
    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        Log.d(this.getClass().getName(),"Android UPNP Service remote device updated " + device.toString());
    }

    /**
     * Called when a previously discovered device disappears.
     * <p>
     * This method will also be called when a discovered device did not update its expiration timeout
     * and has been been removed automatically by the local registry. This method will not be called
     * when the UPnP stack is shutting down.
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with complete service metadata.
     */
    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        Log.d(this.getClass().getName(),"Android UPNP Service remote device removed " + device.toString());
        CameraItem item = new CameraItem(device, false);
        if ( CameraList.ITEMS.indexOf(item) > -1 ) {
            if ( CameraList.ITEMS.remove(item)) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(LocalBroadcastMessageBuilder.buildCameraListNewContentMessage());
            }
        }
    }

    /**
     * Called after you add your own device to the {@link Registry}.
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   The local device added to the {@link Registry}.
     */
    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        Log.d(this.getClass().getName(),"Android UPNP Service local device added " + device.toString());
    }

    /**
     * Called after you remove your own device from the {@link Registry}.
     * <p>
     * This method will not be called when the UPnP stack is shutting down.
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     * @param device   The local device removed from the {@link Registry}.
     */
    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        Log.d(this.getClass().getName(), "Android UPNP Service local device removed " + device.toString());
    }

    /**
     * Called after registry maintenance stops but before the registry is cleared.
     * <p>
     * This method should typically not block, it executes in the thread that shuts down the UPnP stack.
     * </p>
     *
     * @param registry The Cling registry of all devices and services know to the local UPnP stack.
     */
    @Override
    public void beforeShutdown(Registry registry) {
        Log.d(this.getClass().getName(),"Android UPNP Service about to shut down " + registry.toString());
    }

    /**
     * Called after the registry has been cleared on shutdown.
     * <p>
     * This method should typically not block, it executes in the thread that shuts down the UPnP stack.
     * </p>
     */
    @Override
    public void afterShutdown() {
        Log.d(this.getClass().getName(),"Android UPNP Service shut down.");
    }
}
