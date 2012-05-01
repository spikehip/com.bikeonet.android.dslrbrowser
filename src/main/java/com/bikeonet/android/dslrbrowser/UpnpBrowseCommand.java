package com.bikeonet.android.dslrbrowser;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.Photo;

import android.util.Log;

@SuppressWarnings("rawtypes")
public class UpnpBrowseCommand extends Browse {
	
	private static final String TAG = UpnpBrowseCommand.class
	.getSimpleName();
		
	private Device device;
	private DeviceListActivity listActivity;

	public UpnpBrowseCommand(Service contentDirectoryService, String node,
			BrowseFlag directChildren, String string, int i, Long long1,
			SortCriterion sortCriterion) {
		
		super(contentDirectoryService, node, directChildren, string, i, long1, sortCriterion);
	}

	@Override
	public void received(ActionInvocation actionInvocation, DIDLContent didl) {
		/*
		 * collect photo resource urls of any
		 * size
		 */
		int photocount = 0;
		DeviceDisplay deviceDisplay = null;
		int pos = listActivity.getListAdapter()
				.getPosition(
						new DeviceDisplay(
								device));
		if (pos >= 0) {
			deviceDisplay = listActivity
					.getListAdapter().getItem(
							pos);
		} else
			deviceDisplay = null;

		for (Item item : didl.getItems()) {
			Log.i(TAG, "Discovered item : "
					+ item.getTitle());

			// TODO: avoid OutOfMemory issues
			if (photocount > 150)
				break;

			if (item instanceof Photo) {
				EOSImageContent eosImage = new EOSImageContent(
						item.getTitle());
				for (Res res : item
						.getResources()) {
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
					.getDetails()
					.getFriendlyName() : device
					.getDisplayString() + " *";

			BrowseRegistryListener.sendNotificationAboutNewPhotos(
					deviceDisplay, deviceName,
					photocount);
		}
	}

	@Override
	public void updateStatus(Status status) {
		// TODO Auto-generated method stub
		// Called before and after loading the
		// DIDL
		// content
		Log.i(TAG, "UpdateStatus is : "
				+ status.toString());
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String defaultMsg) {
		Log.i(TAG, defaultMsg);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Log.i(TAG,"Running");
			super.run();
		}
		catch (OutOfMemoryError e) { 
			//TODO: handle browse failure due to excessive XML input
			System.gc();
		}
	}

	public void setDevice(Device device) {
		this.device = device;
		
	}

	public void setListActivity(DeviceListActivity listActivity) {
		this.listActivity = listActivity;
		
	}	

}
