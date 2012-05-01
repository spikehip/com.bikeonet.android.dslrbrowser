package com.bikeonet.android.dslrbrowser;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BrowseRegistryListener extends DefaultRegistryListener {

	private static final String TAG = BrowseRegistryListener.class
			.getSimpleName();
	static int NOTIFICATION = R.string.local_service_started;

	@Override
	public void remoteDeviceUpdated(Registry registry, final RemoteDevice device) {
		final DeviceListActivity listActivity = DslrBrowserApplication
				.getInstance().getDeviceListInstance();
		if (listActivity != null) {
			listActivity.runOnUiThread(new Runnable() {
				public void run() {
					DeviceDisplay d = new DeviceDisplay(device);
					if (d.isCanon()) {
						int position = listActivity.getListAdapter()
								.getPosition(d);
						if (position >= 0) {
							d = listActivity.getListAdapter().getItem(position);
							d.getImages().clear();
							getContainerContents(DslrBrowserApplication
									.getInstance().getUpnpService(), device
									.findService(new UDAServiceId(
											"ContentDirectory")), "0", device);
						}
					}
				}
			});
		}
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice device) {
		deviceAdded(device, null);
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry,
			final RemoteDevice device, final Exception ex) {
		final DeviceListActivity listActivity = DslrBrowserApplication
				.getInstance().getDeviceListInstance();
		if (listActivity != null) {
			listActivity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(
							listActivity,
							"Discovery failed of '"
									+ device.getDisplayString()
									+ "': "
									+ (ex != null ? ex.toString()
											: "Couldn't retrieve device/service descriptors"),
							Toast.LENGTH_LONG).show();
				}
			});
			deviceRemoved(device);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		String manufacturer = device.getDetails().getManufacturerDetails()
				.getManufacturer();
		Service contentDirectory;
		ServiceId serviceId = new UDAServiceId("ContentDirectory");

		if (((browseOther() && showOther()) || manufacturer.toUpperCase()
				.equals("CANON"))
				&& (contentDirectory = device.findService(serviceId)) != null) {

			Icon[] icons = device.findIcons();
			for (int i = 0; i < icons.length; i++) {
				Icon icon = icons[i];
				Log.i(TAG, "icon found: " + icon.getUri().toString());
			}

			deviceAdded(device, icons);

			Log.i(TAG, "Canon media service discovered");
			getContainerContents(DslrBrowserApplication.getInstance()
					.getUpnpService(), contentDirectory, "0", device);
		}
	}

	@SuppressWarnings("rawtypes")
	void getContainerContents(final AndroidUpnpService upnpService,
			final Service contentDirectoryService, String node,
			final Device device) {

		final DeviceListActivity listActivity = DslrBrowserApplication
				.getInstance().getDeviceListInstance();
		if (contentDirectoryService != null && listActivity != null) 
			DslrBrowserApplication
					.getInstance()
					.getUpnpService()
					.getControlPoint()
					.execute(
							new Browse(contentDirectoryService, node,
									BrowseFlag.DIRECT_CHILDREN, "*", 0,
									new Long(150), new SortCriterion(true,
											"dc:title")) {

								@Override
								public void run() { 
									try { 
										super.run();
									}
									catch( OutOfMemoryError e) { 
										//TODO: handle big xml 
										Log.i(TAG,"Content XML too big");
									}
								}
								
								@Override
								public void received(
										ActionInvocation actionInvocation,
										DIDLContent didl) {
									/*
									 * recurse into containers
									 */
									for (Container container : didl
											.getContainers()) {
										getContainerContents(upnpService,
												contentDirectoryService,
												container.getId(), device);
									}
									/*
									 * collect photo resource urls of any size
									 */
									int photocount = 0;
									DeviceDisplay deviceDisplay = null;
									int pos = listActivity.getListAdapter()
											.getPosition(
													new DeviceDisplay(device));
									if (pos >= 0) {
										deviceDisplay = listActivity
												.getListAdapter().getItem(pos);
									} else
										deviceDisplay = null;

									for (Item item : didl.getItems()) {
										Log.i(TAG,
												"Discovered item : "
														+ item.getTitle());
										if (item instanceof Photo) {
											EOSImageContent eosImage = new EOSImageContent(
													item.getTitle());
											for (Res res : item.getResources()) {
												if (res.getResolution() != null) {
													eosImage.putSize(
															res.getResolution(),
															res.getValue());
													Log.i(TAG,
															res.getResolution());
												} else {
													eosImage.putSize(
															EOSImageContent.SIZE_UNDEF,
															res.getValue());
												}
												Log.i(TAG, res.getValue());
											}

											if (deviceDisplay != null) {
												deviceDisplay
														.putImage(eosImage);
											}
											photocount++;
										}
									}

									if (photocount > 0) {
										String deviceName = device
												.isFullyHydrated() ? device
												.getDetails().getFriendlyName()
												: device.getDisplayString()
														+ " *";

										sendNotificationAboutNewPhotos(
												deviceDisplay, deviceName,
												photocount);
									}

								}

								@Override
								public void updateStatus(Status status) {
									// TODO Auto-generated method stub
									// Called before and after loading the DIDL
									// content
									Log.i(TAG,
											"UpdateStatus is : "
													+ status.toString());
								}

								@Override
								public void failure(
										ActionInvocation invocation,
										UpnpResponse operation,
										String defaultMsg) {
									Log.e(TAG, defaultMsg);
								}

							});

	}


	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		deviceRemoved(device);
	}

	@SuppressWarnings("rawtypes")
	public void deviceAdded(final Device device, final Icon[] icons) {
		final DeviceListActivity listActivity = DslrBrowserApplication
				.getInstance().getDeviceListInstance();

		final DeviceDisplay d = new DeviceDisplay(device);
		d.setIcons(icons);

		if (listActivity != null) {
			listActivity.runOnUiThread(new Runnable() {
				public void run() {
					if (!showOther() && !d.isCanon())
						return;

					int position = listActivity.getListAdapter().getPosition(d);
					if (position >= 0) {
						// Device already in the list, re-set new value at same
						// position
						listActivity.getListAdapter().remove(d);
						listActivity.getListAdapter().insert(d, position);
					} else {
						listActivity.getListAdapter().add(d);
					}
				}
			});
			if (d.isCanon())
				sendNotificationAboutDevice(false);
		} else {
			DslrBrowserApplication.getInstance().addToDevicePool(d);
			if (d.isCanon())
				sendNotificationAboutDevice(true);
			else
				// intent extras dont seem to work from notification
				DslrBrowserApplication.getInstance().setRefreshDeviceList(true);
		}
	}

	@SuppressWarnings("rawtypes")
	public void deviceRemoved(final Device device) {
		final DeviceListActivity listActivity = DslrBrowserApplication
				.getInstance().getDeviceListInstance();
		if (listActivity != null) {
			listActivity.runOnUiThread(new Runnable() {
				public void run() {
					int pos = listActivity.getListAdapter().getPosition(
							new DeviceDisplay(device));
					if (pos >= 0) {
						DeviceDisplay deviceDisplay = listActivity
								.getListAdapter().getItem(pos);
						if (deviceDisplay.isCanon()) {
							NotificationManager mNM = (NotificationManager) DslrBrowserApplication
									.getInstance()
									.getSystemService(
											android.content.Context.NOTIFICATION_SERVICE);
							;
							mNM.cancel(NOTIFICATION);
						}
						listActivity.getListAdapter().remove(deviceDisplay);
					}
				}
			});
		}
	}

	private boolean showOther() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("show_non_canon", false);
	}

	private boolean browseOther() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("browse_non_canon", false);
	}

	static boolean alertOnNewDevice() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("alert_on_new_device", false);
	}

	static boolean alertOnContent() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("alert_on_content", false);
	}

	static boolean alertSound() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("alert_sound", false);

	}

	static boolean alertVibrate() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DslrBrowserApplication
						.getInstance());

		return prefs.getBoolean("alert_vibrate", false);

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

	private void sendNotificationAboutDevice(boolean refresh) {
		if (!alertOnNewDevice())
			return;

		NotificationManager mNM = (NotificationManager) DslrBrowserApplication
				.getInstance().getSystemService(
						android.content.Context.NOTIFICATION_SERVICE);
		;
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = "New Canon device found";

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(DslrBrowserApplication.getInstance(),
				DeviceListActivity.class);
		intent.putExtra("REFRESHLIST", refresh);
		PendingIntent contentIntent = PendingIntent.getActivity(
				DslrBrowserApplication.getInstance(), 0, intent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(DslrBrowserApplication.getInstance(),
				"DSLR Browser service", text, contentIntent);

		if (alertSound())
			notification.defaults |= Notification.DEFAULT_SOUND;
		if (alertVibrate())
			notification.defaults |= Notification.DEFAULT_VIBRATE;

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// intent extras dont seem to work from notification
		DslrBrowserApplication.getInstance().setRefreshDeviceList(refresh);
		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}

}
