package com.bikeonet.android.dslrbrowser;

import java.io.File;
import java.io.FilenameFilter;

import com.bikeonet.android.dslrbrowser.ui.CachedImageAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Toast;

public class CachedImagesGalleryActivity extends Activity {
	
	private Gallery gallery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	    gallery = (Gallery) findViewById(R.id.gallery);
	    CachedImageAdapter adapter = new CachedImageAdapter(this);
	    gallery.setAdapter(adapter);	    	    
	    		
	    registerForContextMenu(gallery);
	    
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		  super.onCreateContextMenu(menu, v, menuInfo);
		  MenuInflater inflater = getMenuInflater();
		  inflater.inflate(R.menu.cachedgallery_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		  final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  switch (item.getItemId()) {
		  case R.id.sendcachedgalleryitem:
			  send(info.id);
			  return true;
		  case R.id.opencachedgalleryitem:
		    open(info.id);
		    return true;
		  case R.id.deletecachedgalleryitem:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to delete this image?")
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {				 			  
				        	    delete(info.id);
				        	    refreshGallery();
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();	
				alert.show();
		    return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
	}
	
	private void open(long id) { 
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""+id);
		intent.setData(uri);
		try {
			startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			Toast.makeText(CachedImagesGalleryActivity.this, "Cannot start activity for" + uri.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void send(long id) { 
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Where images are stored
		String[] projection = {
			MediaStore.Images.ImageColumns.TITLE
		};
		
		String selection = MediaStore.Images.ImageColumns._ID + "=?";
		String[] selectionArgs = { ""+id };
		
		Cursor cursor = managedQuery(uri, projection, selection, selectionArgs, null);	
		if (cursor != null && cursor.getCount() > 0) { 			
			cursor.moveToFirst();
			String fileName = cursor.getString(0);			
			cursor.close();
			
			File pdir = Environment.getExternalStorageDirectory();
			File imageFile = new File(pdir.getAbsolutePath() + "/DCIM/"+ImageGalleryActivity.DOWNLOAD_DIRECTORY+"/"+fileName);
			
			SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(this);			
			String email_to = prefs.getString("default_email", "");
	
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { email_to });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
	
			emailIntent
					.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFile.getAbsolutePath()));
	
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}
		
	}
	
	private void delete(long id) { 
		Log.i("CachedImageAdapter", "deleting gallery image " + id);
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Where images are stored
		String[] projection = {
			MediaStore.Images.ImageColumns.TITLE
		};
		
		String selection = MediaStore.Images.ImageColumns._ID + "=?";
		String[] selectionArgs = { ""+id };
		
		Cursor cursor = managedQuery(uri, projection, selection, selectionArgs, null);	
		if (cursor != null && cursor.getCount() > 0) { 			
			cursor.moveToFirst();
			String fileName = cursor.getString(0);			
			cursor.close();
			
			selection = MediaStore.Images.ImageColumns.TITLE + "=?";
			selectionArgs[0] = fileName;
			
			getContentResolver().delete(uri, selection, selectionArgs);
			
			//TODO: delete thumbnail
			
			File pdir = Environment.getExternalStorageDirectory();
			File imageFile = new File(pdir.getAbsolutePath() + "/DCIM/"+ImageGalleryActivity.DOWNLOAD_DIRECTORY+"/"+fileName);
			imageFile.delete();
			
			invalidateGallery();
		}
		
		
	}
	
	private void deleteAll() { 
			
			String selection = MediaStore.Images.ImageColumns.DESCRIPTION + "=?";
			String[] selectionArgs = { "CANON" };

			//delete database
			getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
			
			//delete files
			File pdir = Environment.getExternalStorageDirectory();
			File dcim = new File(pdir.getAbsolutePath() + "/DCIM/"+ImageGalleryActivity.DOWNLOAD_DIRECTORY);
			if (dcim.isDirectory()) {
				File[] files = dcim.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (name.toUpperCase().endsWith("JPG")) {
							return true;
						}
						return false;
					}
				});
				if (files.length > 0) {
					for (int i = 0; i < files.length; i++) {
						File image = files[i];
						image.delete();
					}
				}
			}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Delete all cached content").setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, 1, 1, "Refresh").setIcon(
				android.R.drawable.ic_menu_rotate);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Delete all downloaded images?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	    deleteAll();
			                CachedImagesGalleryActivity.this.finish();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();	
			alert.show();
		}
		
		if (item.getItemId() == 1) {
			refreshGallery();
		}
		
		return true;
	}
	
	private void refreshGallery() { 
	    ((BaseAdapter) gallery.getAdapter()).notifyDataSetChanged();
	    gallery.refreshDrawableState();		
	}
	
	private void invalidateGallery() { 
		((BaseAdapter) gallery.getAdapter()).notifyDataSetInvalidated();
		gallery.invalidate();
		gallery.refreshDrawableState();
	}
	
	Gallery getGallery() { 
		return gallery;
	}
	
}
