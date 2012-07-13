package com.bikeonet.android.dslrbrowser;

import java.io.File;
import java.util.List;

import com.bikeonet.android.dslrbrowser.data.EOSImageContent;
import com.bikeonet.android.dslrbrowser.ui.ImageAdapter;
import com.bikeonet.android.dslrbrowser.util.DownloadManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Gallery;

public class ImageGalleryActivity extends Activity {

	public static final String DOWNLOAD_DIRECTORY = "CANON-WFT";
	private List<EOSImageContent> images;
	private PowerManager.WakeLock wakeLock;
	private ImageAdapter imageAdapter;
	private String browsingDevice;
	protected String TAG = "ImageGalleryActivity";
	private Gallery g;

	
	@Override
	protected void onRestart() {
		if ( wakeLock != null ) 
			wakeLock.acquire();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if ( wakeLock != null ) 
			wakeLock.acquire();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if ( wakeLock != null ) 
			wakeLock.release();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if ( wakeLock != null ) 
			wakeLock.release();
		super.onDestroy();

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
				
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "EOSBrowser");
		wakeLock.acquire();
				
		if ( getIntent().getExtras() != null ) { 
			this.images = (List<EOSImageContent>) getIntent().getExtras().get("EOSIMAGES");
			this.browsingDevice = (String) getIntent().getExtras().get("EOSDEVICE");
			Log.i(TAG,"BrowsingDevice: "+browsingDevice);
		    g = (Gallery) findViewById(R.id.gallery);
		    imageAdapter = new ImageAdapter(this, images);
		    g.setAdapter(imageAdapter);
		    
		    g.setOnItemLongClickListener(new OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {

					if ( checkSDCardAvailable() ) { 						
						File pdir = Environment.getExternalStorageDirectory();						
						File dcim = new File(pdir.getAbsolutePath() + "/DCIM/"+DOWNLOAD_DIRECTORY);
						if ( createDir(dcim)) {
							DownloadManager dm = new DownloadManager(dcim.getAbsolutePath(), ImageGalleryActivity.this);
							EOSImageContent image = images.get(position);					
							dm.execute(image);
						}
					}
					else { 
						showErrorDialog(ImageGalleryActivity.this, "Error", "Storage media not available", "Ok");
					}
					
					return true;
				}
		    	
			});
		    
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Download images").setIcon(android.R.drawable.ic_menu_upload);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			if ( checkSDCardAvailable() ) { 
				File pdir = Environment.getExternalStorageDirectory();
				File dcim = new File(pdir.getAbsolutePath() + "/DCIM/"+DOWNLOAD_DIRECTORY);
				if ( createDir(dcim)) {
					DownloadManager dm = new DownloadManager(dcim.getAbsolutePath(), ImageGalleryActivity.this);						
					EOSImageContent[] imageArray = new EOSImageContent[images.size()];
					int i = 0;
					for (EOSImageContent eosImageContent : images) {
						imageArray[i++] = eosImageContent;
					}
					dm.execute(imageArray);
				}
			}
			else { 
				showErrorDialog(this, "Error", "Storage media not available", "Ok");
			}
		}
				
		return false;
	}
	
	public boolean checkSDCardAvailable() { 
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}

    public static void showErrorDialog(Context context, String title, String message, String confirmation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(confirmation, new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                }
                
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public static boolean createDir(File dir) { 
    	if ( dir.exists()) { 
    		return true;
    	}
    	
    	return dir.mkdirs();
    }
    
}
