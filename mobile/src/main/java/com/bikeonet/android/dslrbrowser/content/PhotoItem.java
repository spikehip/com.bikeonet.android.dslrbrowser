package com.bikeonet.android.dslrbrowser.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class PhotoItem {

    private CameraItem cameraItem;
    private String title;
    private Bitmap thumbnail;
    private String resourceUrl;
    private String thumbnailResourceUrl;

    public CameraItem getCameraItem() {
        return cameraItem;
    }

    public void setCameraItem(CameraItem cameraItem) {
        this.cameraItem = cameraItem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getThumbnailResourceUrl() {
        return thumbnailResourceUrl;
    }

    public void setThumbnailResourceUrl(String thumbnailResourceUrl) {
        this.thumbnailResourceUrl = thumbnailResourceUrl;
    }

    public void downloadThumbnail() {
        try {
            if (getThumbnailResourceUrl() != null) {
                setThumbnail(createBitmapFromByteArray(downloadIconFromUrl(getThumbnailResourceUrl())));
            }
        }
        catch (IOException e) {
            Log.d(this.getClass().getName(), "failed to load thumbnail from "+getThumbnailResourceUrl()+" error: "+e.getMessage());
        }
    }
    private Bitmap createBitmapFromByteArray(byte[] icon) {

        if (icon != null && icon.length > 0) {
            Bitmap bMap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
            return bMap;
        }

        return null;
    }

    private byte[] downloadIconFromUrl(String url) throws IOException {
        URL iconUrl = new URL(url);
        URLConnection ucon = iconUrl.openConnection();
        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        int current = 0;
            /* Convert the Bytes read to a String. */
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        byte[] buffer = new byte[1000];
        while((current = bis.read(buffer))!=-1) {
            bos.write(buffer,0,current);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return this.getTitle();
    }
}
