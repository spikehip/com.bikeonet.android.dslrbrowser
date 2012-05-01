package com.bikeonet.android.dslrbrowser;

import java.util.ArrayList;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;

@SuppressWarnings("rawtypes")
public class DeviceDisplay {

	Device device;
	final ArrayList<EOSImageContent> images;
	private Icon[] icons;

	public DeviceDisplay(Device device) {
		this.device = device;
		this.images = new ArrayList<EOSImageContent>();
	}

	public ArrayList<EOSImageContent> getImages() {
		return images;
	}

	public void putImage(EOSImageContent image) {
		if (!images.contains(image))
			images.add(image);
	}

	public Device getDevice() {
		return device;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeviceDisplay that = (DeviceDisplay) o;
		return device.equals(that.device);
	}

	@Override
	public int hashCode() {
		return device.hashCode();
	}

	@Override
	public String toString() {
		// Display a little star while the device is being loaded
		return device.isFullyHydrated() ? device.getDetails().getFriendlyName()
				: device.getDisplayString() + " *";
	}

	public void setIcons(Icon[] icons) {

		this.icons = icons;

	}

	public Icon[] getIcons() {
		return this.icons;
	}

	public boolean isCanon() {
		if (device.isFullyHydrated()) {
			String manufacturer = device.getDetails().getManufacturerDetails()
					.getManufacturer();

			if (manufacturer.toUpperCase().equals("CANON"))
				return true;
		}

		return false;
	}

}
