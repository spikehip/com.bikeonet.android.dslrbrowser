package com.bikeonet.android.dslrbrowser;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.StaleDataException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class CachedImageAdapter extends BaseAdapter {
    
	private static Activity mContext;
	private Cursor cursor;
	private String TAG = "CachedImageAdapter";
	private static SparseArray<ImageView> imageCache;
	private static SparseArray<Long> idCache;
	private static SparseArray<Long> miniThumbIdCache;
	private int count;
	
	private static ImageView placeHolder; 
		
	/**
	 * Initialize a new CachedImageAdapter class
	 * @param context view context
	 * @param layout 
	 * @param c Cursor
	 * @param from
	 * @param to
	 */
	public CachedImageAdapter(Activity context) {
		mContext = context;
		
		//Call the garbage collector to free up memory before creating a new image
		System.gc();
		
		placeHolder = new ImageView(mContext);
		placeHolder.setImageResource(R.drawable.icon);
		placeHolder.setMinimumHeight(480);
		placeHolder.setMinimumWidth(640);
		
		idCache = new SparseArray<Long>(10);
		miniThumbIdCache = new SparseArray<Long>(10);
		
		openCursor();		
		count = getCursor().getCount();

		//fill the cache
		for(int i=0; i<count; i++) {
			long itemId = getItemId(i);
			getIdCache().put(i, itemId);
			getMiniThumIdCache().put(i, getItemThumbId(itemId));
		}
		cursor.close();
		
		createCache();
	}
	
	private Long getItemThumbId(long id) {
		Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI; // Where images are stored
		String[] projection = {
			MediaStore.Images.ImageColumns._ID,  // The columns we want
			MediaStore.Images.Thumbnails.IMAGE_ID,
			MediaStore.Images.Thumbnails.KIND
		};		
		String selection = MediaStore.Images.Thumbnails.IMAGE_ID + "=? AND "+MediaStore.Images.Thumbnails.KIND + "=?";
		String[] selectionArgs = { String.valueOf(id), String.valueOf(MediaStore.Images.Thumbnails.MINI_KIND) };
		
		Cursor c = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);	
		
		long ret=0L;
		
		if ( c!=null && !c.isClosed() && c.getCount()>0) {
			c.moveToFirst();
			ret = c.getLong(0);
		}
		c.close();
		
		return ret;
	}

	private static void createCache() { 
		imageCache = new SparseArray<ImageView>(10);
	}
	    
	private SparseArray<ImageView> getCache() { 
		if ( imageCache == null )
			createCache();
		return imageCache;
	}
	
	private SparseArray<Long> getIdCache() { 
		if ( idCache == null ) { 
			idCache=new SparseArray<Long>(10);
		}
		
		return idCache;
	}
	private SparseArray<Long> getMiniThumIdCache() { 
		if ( miniThumbIdCache == null ) { 
			miniThumbIdCache=new SparseArray<Long>(10);
		}
		
		return miniThumbIdCache;
	}
	
	
	private void clearCache(int position) { 
		
		for (int i = 0; i < imageCache.size(); i++) {
			if ( i < position -2 || i > position + 2 )
			imageCache.remove(i);			
		}		
	}
	
	@Override
	public void notifyDataSetChanged() {
		Log.i(TAG , "notifyDataSetChanged");
		closeCursor();
		openCursor();
		super.notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetInvalidated() {
		Log.i(TAG , "notifyDataSetInvalidated");
		createCache();
		super.notifyDataSetInvalidated();
	}
	
	public void openCursor() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean isDesc = prefs.getBoolean("cache_order_desc", true);
		
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Where images are stored
		String[] projection = {
			MediaStore.Images.ImageColumns._ID,  // The columns we want
			MediaStore.Images.ImageColumns.DESCRIPTION,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaStore.Images.ImageColumns.ORIENTATION,
			MediaStore.Images.ImageColumns.BUCKET_ID,
			MediaStore.Images.ImageColumns.TITLE,
			MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC
		};		
		String selection = MediaStore.Images.ImageColumns.DESCRIPTION + "=?";
		String[] selectionArgs = { "CANON" };
		String sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + " " +(isDesc?"DESC":"ASC");
		
		cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);	
	}

	public void closeCursor() { 
		getCursor().close();
		cursor=null;
	}
	
	public Cursor getCursor() { 
		
		if ( cursor == null || cursor.isClosed() ) { 
			openCursor();
		}
		
		return cursor;
	}

	public int getCount() {
		return getCursor().getCount();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		
		if ( getCursor().isClosed() || position < 0 || position >= getCursor().getCount() ) { 
			return 0L;
		}
		
		getCursor().moveToFirst();
		getCursor().move(position);
		long ret = getCursor().getLong(0);
				
		return ret;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if ( convertView != null || position < 0 )
			return convertView;
		
		if ( getCache().get(position) != null ) { 
			Log.i("CURSOR", "returning view from cache at position "+position);
			return getCache().get(position);
		}
		
		Log.i("CURSOR", "loading new image from uri by asynctask");
		
		if ( position < 0 || position >= count ) { 
			return placeHolder;
		}
		
		clearCache(position);
		if ( getIdCache().get(position) == null ) {
			try { 
				getIdCache().put(position, getItemId(position));
				getMiniThumIdCache().put(position, getIdCache().get(position));
				closeCursor();
			}
			catch (StaleDataException e) {
				return placeHolder;
			}
		}
		new LoadImageViewTask().execute(position);
		
		return placeHolder;
	}

	private class LoadImageViewTask extends AsyncTask<Integer, Void, ImageView> {
		
		@Override
		protected ImageView doInBackground(Integer... params) {
			
			for (int i = 0; i < params.length; i++) {
				
				int position = params[i];
								
				Log.i("CURSOR", "creating view at position "+position);				
				try {

				if ( getCursor().isClosed() || position < 0 || position >= count ) { 
					Thread.sleep(10000);
				}
					
				Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""+getIdCache().get(position, 0L));
				Uri thumbURI = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""+getMiniThumIdCache().get(position, 0L));

				
				//Call the garbage collector to free up memory before creating a new image
				System.gc();
				ImageView imageView = new ImageView(mContext);
				imageView.setPadding(30, 0, 30, 0);
								
//				Bitmap thumbBitmap = MediaStore.Images.Thumbnails
//						.getThumbnail(mContext.getContentResolver(),
//								getMiniThumIdCache().get(position, 0L),
//								MediaStore.Images.Thumbnails.MINI_KIND,
//								null);
				
				if ( thumbURI != null )  { 
					imageView.setImageURI(thumbURI);
					if ( imageView.getDrawable() == null ) { 
						imageView.setImageURI(imageURI);
					}
				}
				else {
					imageView.setImageURI(imageURI);
				}
				
				imageView.setMinimumHeight(480);
				imageView.setMinimumWidth(640);
				Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(640, 480);
				imageView.setLayoutParams(layoutParams);
							
				getCache().put(position, imageView);
				
				}
				catch (OutOfMemoryError e) { 
					Log.e("CURSOR", "Out of memory, calling clear cache and gc");
					clearCache(position);
					System.gc();
					
//					new LoadImageViewTask().execute(position);
					
					getCache().put(position, placeHolder);
				} catch (InterruptedException e) {
//					new LoadImageViewTask().execute(position);
					return placeHolder;
				}
				catch (StaleDataException e) { 
					Log.e("CURSOR", "Cursor closed, slow down!");
					return placeHolder;
				}
				
			}
			
			return getCache().get(params[0]);
		} 
		
		@Override
		protected void onPostExecute(ImageView result) {
			if (result != null )
			notifyDataSetChanged();
		}
		
	}
	
}
