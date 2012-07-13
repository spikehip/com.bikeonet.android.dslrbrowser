package com.bikeonet.android.dslrbrowser.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.bikeonet.android.dslrbrowser.DslrBrowserApplication;
import com.bikeonet.android.dslrbrowser.data.EOSImageContent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

public class DownloadManager extends AsyncTask<EOSImageContent, Long, String> {

	private final String PATH; // =
								// "/data/data/com.bikeonet.android.sampleUpnpBrowser/";
	private final Context context;
	private ProgressDialog progressDialog;
	private String email_to;
	private String size;
	// private String thumbnailSize;
	private int count;
	private final boolean insertGPS;

	public DownloadManager(String path, Context context) {
		PATH = path;
		this.context = context;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		email_to = prefs.getString("default_email", "");
		size = prefs.getString("sizes_list_preference", "640x480");
		insertGPS = prefs.getBoolean("exif_insert_gps", false);

		// thumbnailSize = prefs.getString("sizes_preview_preference",
		// EOSImageContent.SIZE_160x120);
	}

	@Override
	protected void onPreExecute() {

		progressDialog = ProgressDialog.show(context, "Downloading image",
				"Please wait...");
	}

	@Override
	protected String doInBackground(EOSImageContent... params) {
		count = params.length;
		progressDialog.setMax(100);
		String imageURL, fileName, mTempFilePath = null;
		// String thumbnailImageURL,thumbnailFileName = null;

		for (int i = 0; i < params.length; i++) {
			EOSImageContent image = params[i];
			if (image.hasSize(size)) {
				imageURL = image.getSize(size);
			} else if (image.hasSize(EOSImageContent.SIZE_640x480)) {
				imageURL = image.getSize(EOSImageContent.SIZE_640x480);
			} else if (image.getSizes().iterator().hasNext()) {
				imageURL = image.getSize(image.getSizes().iterator().next());
			} else {
				return null;
			}
			fileName = image.getName();

			/*
			 * download thumbnail if available
			 */
			// if (image.hasSize(thumbnailSize)) {
			// thumbnailImageURL = image.getSize(thumbnailSize);
			// thumbnailFileName = "thumb_"+image.getName();
			// DownloadFromUrl(thumbnailImageURL, thumbnailFileName);
			// }

			mTempFilePath = DownloadFromUrl(imageURL, fileName);

			if (insertGPS) {
				try {
					ExifInterface exifInterface = new ExifInterface(
							mTempFilePath);
					
					if ( exifInterface.getAttribute(ExifInterface.TAG_MODEL) == null ) { 
						exifInterface.setAttribute(ExifInterface.TAG_MODEL, image.getModelName());									
					}					
					
					float[] latlong = new float[2];
					if (!exifInterface.getLatLong(latlong)) {
						// TODO: insert gps stuff here
						if (DslrBrowserApplication.getInstance() != null) {
							Location location = DslrBrowserApplication
									.getInstance().getLastLocation();
							
							if (location != null) {
								String lat = decimalToDMS(location
										.getLatitude());
								String lon = decimalToDMS(location
										.getLongitude());
								String latRef = location.getLatitude() > 0 ? "N"
										: "S";
								String lonRef = location.getLongitude() > 0 ? "E"
										: "W";

								exifInterface.setAttribute(
										ExifInterface.TAG_GPS_LATITUDE, lat);
								exifInterface.setAttribute(
										ExifInterface.TAG_GPS_LATITUDE_REF,
										latRef);
								exifInterface.setAttribute(
										ExifInterface.TAG_GPS_LONGITUDE, lon);
								exifInterface.setAttribute(
										ExifInterface.TAG_GPS_LONGITUDE_REF,
										lonRef);
																
							}
						}

					}
					
					exifInterface.saveAttributes();

				} catch (IOException e) {
					// TODO: log error
				}
			}

			Long progress = new Long((int) ((i / (float) count) * 100));
			publishProgress(progress);
		}

		if (count == 1)
			return mTempFilePath;
		else
			return null;
	}

	@Override
	protected void onPostExecute(String result) {
		progressDialog.dismiss();
		if (result != null) {
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { email_to });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

			emailIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + result));

			context.startActivity(Intent.createChooser(emailIntent,
					"Send mail..."));
		}
	}

	@Override
	protected void onProgressUpdate(Long... values) {
		progressDialog.setProgress(values[0].intValue());
		progressDialog.setMessage("Done " + values[0].intValue() + "%");
	}

	public String DownloadFromUrl(String imageURL, String fileName) {

		try {
			URL url = new URL(imageURL);
			String absFilename = PATH + "/" + fileName;
			File file = new File(absFilename);

			if (!file.exists()) {
				long startTime = System.currentTimeMillis();
				Log.d("ImageManager", "download begining");
				Log.d("ImageManager", "download url:" + url);
				Log.d("ImageManager", "downloaded file name:" + absFilename);

				/* Open a connection to that URL. */
				URLConnection ucon = url.openConnection();
				/*
				 * Define InputStreams to read from the URLConnection.
				 */
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);

				/*
				 * Read bytes to t he Buffer until there is nothing more to
				 * read(-1).
				 */
				int current = 0;				
				/* Convert the Bytes read to a String. */
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1000];
				while((current = bis.read(buffer))!=-1) {
					fos.write(buffer,0,current);
				}

				fos.close();
				
				String mediaFileName = absFilename;
				
				ExifInterface exifInterface = new ExifInterface(
						absFilename);
				if (exifInterface.hasThumbnail()) { 
					String thumbFile = PATH + "/thumb_" + fileName; 
					FileOutputStream thumbOutputStream = new FileOutputStream(thumbFile);
					thumbOutputStream.write(exifInterface.getThumbnail());
					thumbOutputStream.close();
					mediaFileName = thumbFile;
				}

				
				Log.d("ImageManager",
						"download ready in"
								+ ((System.currentTimeMillis() - startTime) / 1000)
								+ " sec");

				try {
					MediaStore.Images.Media.insertImage(
							context.getContentResolver(), mediaFileName,
							fileName, "CANON");
				} catch (OutOfMemoryError e) {
					//Toast.makeText(context, "Not enough memory to store media",
					//		Toast.LENGTH_LONG).show();
					Log.d("ImageManager", e.getMessage());
				}

			}

			return absFilename;

		} catch (IOException e) {
			Log.d("ImageManager", "Error: " + e);
		}

		return null;
	}

	String decimalToDMS(double coord) {
		String output, degrees, minutes, seconds;

		// gets the modulus the coordinate devided by one (MOD1).
		// in other words gets all the numbers after the decimal point.
		// e.g mod = 87.728056 % 1 == 0.728056
		//
		// next get the integer part of the coord. On other words the whole
		// number part.
		// e.g intPart = 87

		double mod = coord % 1;
		int intPart = (int) coord;

		// set degrees to the value of intPart
		// e.g degrees = "87"

		degrees = String.valueOf(intPart);

		// next times the MOD1 of degrees by 60 so we can find the integer part
		// for minutes.
		// get the MOD1 of the new coord to find the numbers after the decimal
		// point.
		// e.g coord = 0.728056 * 60 == 43.68336
		// mod = 43.68336 % 1 == 0.68336
		//
		// next get the value of the integer part of the coord.
		// e.g intPart = 43

		coord = mod * 60;
		mod = coord % 1;
		intPart = (int) coord;

		// set minutes to the value of intPart.
		// e.g minutes = "43"
		minutes = String.valueOf(intPart);

		// do the same again for minutes
		// e.g coord = 0.68336 * 60 == 40.0016
		// e.g intPart = 40
		coord = mod * 60;
		intPart = (int) coord;

		// set seconds to the value of intPart.
		// e.g seconds = "40"
		seconds = String.valueOf(intPart);

		// I used this format for android but you can change it
		// to return in whatever format you like
		// e.g output = "87/1,43/1,40/1"
		output = degrees + "/1," + minutes + "/1," + seconds + "/1";

		// Standard output of D°M′S″
		// output = degrees + "°" + minutes + "'" + seconds + "\"";

		return output;
	}

}
