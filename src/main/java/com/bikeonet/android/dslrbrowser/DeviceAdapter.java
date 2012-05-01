package com.bikeonet.android.dslrbrowser;

import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceAdapter extends ArrayAdapter<DeviceDisplay> {

	private final int viewRootId;

	public DeviceAdapter(Context context, int viewId) {
		super(context, viewId);
		viewRootId = viewId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout deviceView;
		DeviceDisplay deviceDisplay = (DeviceDisplay) getItem(position);

		// Create one list item's view^M
		if (convertView == null) {
			deviceView = new LinearLayout(getContext());
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			li.inflate(viewRootId, deviceView, true);
		} else {
			deviceView = (LinearLayout) convertView;
		}

		// Getting sub-views^M
		TextView txtDeviceManufacturer = (TextView) deviceView
				.findViewById(R.id.txtDeviceManufacturer);
		TextView txtDeviceFriendlyName = (TextView) deviceView
				.findViewById(R.id.txtDeviceFriendlyName);
		ImageView deviceIcon = (ImageView) deviceView.findViewById(R.id.imgDeviceIcon);

		if (deviceDisplay.getDevice().isFullyHydrated()) {

			txtDeviceManufacturer.setText(deviceDisplay.getDevice()
					.getDetails().getManufacturerDetails().getManufacturer());
			txtDeviceFriendlyName.setText(deviceDisplay.getDevice()
					.getDetails().getFriendlyName());
			
			if ( deviceDisplay.isCanon() ) { 
				deviceIcon.setImageResource(R.drawable.icon);
			}

//			if (deviceDisplay.getIcons() != null
//					&& deviceDisplay.getIcons().length > 0) {
//				ImageView deviceIcon = (ImageView) deviceView
//						.findViewById(R.id.imgDeviceIcon);
//				Icon icon = deviceDisplay.getIcons()[0];
//				
//				if (deviceDisplay.getDevice().getDetails().getPresentationURI() != null) {
//					String host = deviceDisplay.getDevice().getDetails()
//							.getPresentationURI().getHost();
//					String port;
//					if ( deviceDisplay.getDevice()
//							.getDetails().getPresentationURI().getPort() > 0 ) { 
//						port = Integer.toString(deviceDisplay.getDevice()
//							.getDetails().getPresentationURI().getPort());
//					}else { 
//						port = "80";
//					}
//					String url = "http://" + host + ":" + port + "/"
//							+ icon.getUri().toString();
//					Drawable drawable = loadImage(url);
//					deviceIcon.setImageDrawable(drawable);
//				}
//				else { 
//					deviceIcon.setImageResource(R.drawable.icon);
//				}
//			}
		} else {

			txtDeviceManufacturer.setText(deviceDisplay.getDevice()
					.getDisplayString());
			txtDeviceFriendlyName.setText("*");
			deviceIcon.setImageResource(R.drawable.icon_green);

		}

		deviceView.setEnabled(false);
		
		return deviceView;
	}

	public static Drawable loadImage(String url) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		} catch (Exception e) {
			return null;
		}
	}

}
