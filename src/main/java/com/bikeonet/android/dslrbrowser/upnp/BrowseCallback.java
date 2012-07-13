package com.bikeonet.android.dslrbrowser.upnp;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.Photo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.bikeonet.android.dslrbrowser.DeviceListActivity;
import com.bikeonet.android.dslrbrowser.DslrBrowserApplication;
import com.bikeonet.android.dslrbrowser.ImageGalleryActivity;
import com.bikeonet.android.dslrbrowser.R;
import com.bikeonet.android.dslrbrowser.data.DeviceDisplay;
import com.bikeonet.android.dslrbrowser.data.EOSImageContent;

@SuppressWarnings("rawtypes")
public class BrowseCallback extends Browse {

	private static final String TAG = BrowseCallback.class.getName();

	private String currentNode;
	private DIDLContent receivedDIDL = null;
	private UpnpBrowseManager manager;
	private RemoteDevice device;
	private DeviceDisplay deviceDisplay = null;
	private DeviceListActivity listActivity = null;

	public BrowseCallback(Service service, String containerId,
			UpnpBrowseManager manager, RemoteDevice device) {
		this(service, containerId, BrowseFlag.DIRECT_CHILDREN);
		currentNode = containerId;
		this.manager = manager;
		this.device = device;

		listActivity = DslrBrowserApplication.getInstance()
				.getDeviceListInstance();
		int pos = -1;
		if (listActivity != null && listActivity.getListAdapter() != null) {
			pos = listActivity.getListAdapter().getPosition(
					new DeviceDisplay(device));
		}
		if (pos >= 0) {
			deviceDisplay = listActivity.getListAdapter().getItem(pos);
		} else
			deviceDisplay = null;

	}

	private BrowseCallback(Service service, String containerId, BrowseFlag flag) {
		super(service, containerId, flag, "*", 0, new Long(100),
				new SortCriterion(true, "dc:title"));
	}

	@Override
	public void received(ActionInvocation actionInvocation, DIDLContent didl) {

		Log.d(TAG, actionInvocation.getFailure() != null ? actionInvocation
				.getFailure().getMessage()
				: "Received action invocation no failure");
		receivedDIDL = didl;

	}

	@Override
	public void updateStatus(Status status) {
		// Called before and after loading the DIDL content
		Log.i(TAG, "UpdateStatus for container " + currentNode + " is : "
				+ status.toString());

		/*
		 * Loading starts
		 */
		if (status.toString().equals("LOADING")) {
			manager.setLoading(true);
		}

		/*
		 * Loading finished. If we have a device list item to append images to,
		 * do it. if not, skip silently
		 */
		if (status.toString().equals("OK") ) {
			if (deviceDisplay != null) {
			int photocount = doHydrate();
			/* browse completed */

			if (photocount > 0) {
				String deviceName = device.isFullyHydrated() ? device
						.getDetails().getFriendlyName() : device
						.getDisplayString() + " *";

				sendNotificationAboutNewPhotos(deviceDisplay, deviceName,
						photocount);
			}
			}
			else {
				/* failed to insert records, re-query */
				manager.queueNode(device, currentNode);
			}

			manager.setLoading(false);
		}
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation,
			String defaultMsg) {
		Log.e(TAG, "FAILURE for container " + currentNode + " Action "
				+ invocation.getAction().getName());
		Log.e(TAG, "FAILURE: Operation "
				+ (operation == null ? "null" : operation.getResponseDetails()));
		Log.e(TAG, "FAILURE: Message: " + defaultMsg);
		Log.e(TAG, "Retry "+currentNode);
		manager.queueNode(device, currentNode);
		manager.setLoading(false);
	}

	private int doHydrate() {
		/*
		 * recurse into containers
		 */
		for (Container container : receivedDIDL.getContainers()) {
			Log.i(TAG, "Queue container id " + container.getId() + " parent "
					+ container.getParentID());
			manager.queueNode(device, container.getId());
		}
		/*
		 * collect photo resource urls of any size
		 */
		int photocount = 0;

		for (Item item : receivedDIDL.getItems()) {
			Log.i(TAG, "Discovered item : " + item.getTitle());
			if (item instanceof Photo) {
				EOSImageContent eosImage = new EOSImageContent(item.getTitle());

				// should be fully
				// hidrated at this
				// point
				eosImage.setModelName(deviceDisplay.getDevice().getDetails()
						.getFriendlyName());

				for (Res res : item.getResources()) {
					if (res.getResolution() != null) {
						eosImage.putSize(res.getResolution(), res.getValue());
						Log.i(TAG, res.getResolution());
					} else {
						eosImage.putSize(EOSImageContent.SIZE_UNDEF,
								res.getValue());
					}
					Log.i(TAG, res.getValue());
				}

				if (deviceDisplay != null) {
					deviceDisplay.putImage(eosImage);
				}
				photocount++;
			}
		}

		return photocount;
	}

	static void sendNotificationAboutNewPhotos(DeviceDisplay d,
			String deviceName, int photocount) {
		if (!BrowseRegistryListener.alertOnContent())
			return;

		NotificationManager mNM = (NotificationManager) DslrBrowserApplication
				.getInstance().getSystemService(
						android.content.Context.NOTIFICATION_SERVICE);
		;
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = "Found " + photocount + " images";

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(DslrBrowserApplication.getInstance(),
				ImageGalleryActivity.class);
		intent.putExtra("EOSIMAGES", d.getImages());
		intent.putExtra("EOSDEVICE", d.getDevice().getDisplayString());
		PendingIntent contentIntent = PendingIntent.getActivity(
				DslrBrowserApplication.getInstance(), 0, intent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(DslrBrowserApplication.getInstance(),
				deviceName, text, contentIntent);

		if (BrowseRegistryListener.alertSound())
			notification.defaults |= Notification.DEFAULT_SOUND;
		if (BrowseRegistryListener.alertVibrate())
			notification.defaults |= Notification.DEFAULT_VIBRATE;

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Send the notification.
		mNM.notify(BrowseRegistryListener.NOTIFICATION, notification);
	}

}
