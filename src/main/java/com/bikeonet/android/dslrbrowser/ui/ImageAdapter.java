package com.bikeonet.android.dslrbrowser.ui;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.bikeonet.android.dslrbrowser.R;
import com.bikeonet.android.dslrbrowser.data.EOSImageContent;

public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private static Context mContext;
    private List<EOSImageContent> items;
	private String previewSize;
    private static HashMap<String, ImageView> imageCache;
    
	private static ImageView placeHolder; 
    
    public ImageAdapter(Context c, List<EOSImageContent> items) {
        mContext = c;
        this.items = items;
//        TypedArray a = obtainStyledAttributes(R.styleable.ImageGalleryActivity);
//        
//        mGalleryItemBackground = a.getResourceId(
//                R.styleable.ImageGalleryActivity_android_galleryItemBackground, 0);
//        a.recycle();
        
		//Call the garbage collector to free up memory before creating a new image
		SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(mContext);
		previewSize = prefs.getString("sizes_preview_preference", EOSImageContent.SIZE_160x120);
        
		System.gc();
        
		placeHolder = new ImageView(mContext);
		placeHolder.setImageResource(R.drawable.icon);
		placeHolder.setMinimumHeight(480);
		placeHolder.setMinimumWidth(640);
        
        createCache();
    }

    private static void createCache() {
    	if ( imageCache == null )
            imageCache = new HashMap<String, ImageView>();
	}
    
    private static void clearCache(int position) { 
		for (int i = 0; i < imageCache.size(); i++) {
			if ( i < position -1 || i > position + 1 )
			imageCache.remove(i);			
		}		
		System.gc();
    }
    
    private static void clearCache(String url, List<EOSImageContent> items) {
    	int i=0;
    	for (EOSImageContent image : items) {
			while ( image.getSizes().iterator().hasNext()) { 
				if ( image.getSizes().iterator().next().equals(url)) { 
					clearCache(i);
					return;
				}
			}
			i++;
		}
    }

	public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
		if ( convertView != null && position >= 0 )
			return convertView;
		
		if ( position < 0 || position >= items.size() ) { 
			return placeHolder;
		}
		
        EOSImageContent image = items.get(position);        
        String uri = image.getSize(previewSize);
        
        if ( !image.hasSize(previewSize) && image.getSizes().iterator().hasNext() ) {
        	uri = image.getSize(image.getSizes().iterator().next());
        }

    	if ( getImageCache().containsKey(uri) ) { 
    		return getImageCache().get(uri);    		
    	}

    	new LoadImageViewTask().execute(uri);
    	
    	return placeHolder;
    }
    
	private static HashMap<String, ImageView> getImageCache() {
		if ( imageCache == null )
			createCache();
		return imageCache;
	}

	public List<EOSImageContent> getItems() { 
		return this.items;
	}
	
	private class LoadImageViewTask extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			
			for (int i = 0; i < params.length; i++) {
				
				String url = params[i];
								
		        try {
		            InputStream is = (InputStream) new URL(url).getContent();
		            
		            System.gc();
		            Drawable d = Drawable.createFromStream(is, "src name");
                    ImageView imageView = new ImageView(mContext);
		            imageView.setImageDrawable(d);
		            
		            imageView.setLayoutParams(new Gallery.LayoutParams(640, 480));
		            imageView.setPadding(30, 0, 30, 0);
		            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		  	//        i.setBackgroundResource(mGalleryItemBackground);
		            getImageCache().put(url, imageView);
		        } 
		        catch (Throwable e) {
		        	clearCache(url, ImageAdapter.this.items);
		        	getImageCache().put(url, placeHolder);        	
		        }
			}
			
			return params[0];
		} 
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null )
			notifyDataSetChanged();
		}
		
	}
	
    
}
