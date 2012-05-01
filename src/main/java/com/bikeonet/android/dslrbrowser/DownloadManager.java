package com.bikeonet.android.dslrbrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class DownloadManager extends AsyncTask<EOSImageContent, Long, String> {

	private final String PATH; // =
								// "/data/data/com.bikeonet.android.sampleUpnpBrowser/";
	private final Context context;
	private ProgressDialog progressDialog;
	private String email_to;
	private String size;
//	private String thumbnailSize;
	private int count;

	public DownloadManager(String path, Context context) {
		PATH = path;
		this.context = context;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		email_to = prefs.getString("default_email", "");
		size = prefs.getString("sizes_list_preference", "640x480");
//		thumbnailSize = prefs.getString("sizes_preview_preference", EOSImageContent.SIZE_160x120);
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
//		String thumbnailImageURL,thumbnailFileName = null;
		
		for (int i = 0; i < params.length; i++) {
			EOSImageContent image = params[i];
			if (image.hasSize(size)) {
				imageURL = image.getSize(size);
			} else if (image.hasSize(EOSImageContent.SIZE_640x480)) {
				imageURL = image.getSize(EOSImageContent.SIZE_640x480);
			} else if (image.getSizes().iterator().hasNext() ) {
				imageURL = image.getSize(image.getSizes().iterator().next());
			}
			else { 
				return null;
			}
			fileName = image.getName();
			
			/*
			 * download thumbnail if available
			 */
//			if (image.hasSize(thumbnailSize)) {
//				thumbnailImageURL = image.getSize(thumbnailSize);
//				thumbnailFileName = "thumb_"+image.getName();
//				DownloadFromUrl(thumbnailImageURL, thumbnailFileName);
//			}			

			mTempFilePath = DownloadFromUrl(imageURL, fileName);		
			Long progress = new Long((int) ((i / (float) count) * 100));
			publishProgress(progress);
		}

		if ( count == 1 )
			return mTempFilePath;
		else 
			return null;
	}

	@Override
	protected void onPostExecute(String result) {
		progressDialog.dismiss();
		if ( result != null ) { 
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { email_to });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
	
			emailIntent
					.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + result));
	
			context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
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
				 * Read bytes to t
				 * he Buffer until there is nothing more to
				 * read(-1).
				 */
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				/* Convert the Bytes read to a String. */
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray());
				fos.close();
				Log.d("ImageManager",
						"download ready in"
								+ ((System.currentTimeMillis() - startTime) / 1000)
								+ " sec");
				
				try { 
					MediaStore.Images.Media.insertImage(context.getContentResolver(),  absFilename, fileName, "CANON");				
				}
				catch ( OutOfMemoryError e ) { 
					Toast.makeText(context, "Not enough memory to store media", Toast.LENGTH_LONG).show();
				}
				
			}

			return absFilename;

		} catch (IOException e) {
			Log.d("ImageManager", "Error: " + e);
		}

		return null;
	}

}
