package com.bikeonet.android.dslrbrowser.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


/**
 * Created by andrasbekesi on 04/05/17.
 */

public class CameraItem {

    private final String id;
    private final String name;
    private final String description;
    private RemoteDevice remoteDevice;
    private Bitmap largeIcon;
    private Bitmap smallIcon;

    public CameraItem(RemoteDevice remoteDevice) {
        this(remoteDevice, true);
    }

    public CameraItem(RemoteDevice remoteDevice, Boolean loadContents) {
        this(remoteDevice.getIdentity().getUdn().getIdentifierString(),
                remoteDevice.getDetails() != null ? remoteDevice.getDetails().getFriendlyName() : remoteDevice.getIdentity().getUdn().getIdentifierString(),
                (remoteDevice.getDetails() != null ? ( remoteDevice.getDetails().getModelDetails() != null ? remoteDevice.getDetails().getModelDetails().getModelDescription() : "") : "")
                        + " " +
                        (remoteDevice.getDetails() != null ? ( remoteDevice.getDetails().getModelDetails() != null ? remoteDevice.getDetails().getModelDetails().getModelName():"") : ""));

        this.remoteDevice = remoteDevice;
        if (loadContents) {
            loadIcons();
        }
    }

    public void loadIcons() {
        try {
            smallIcon = downloadSmallIcon();
            largeIcon = downloadLargeIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CameraItem(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return id + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CameraItem that = (CameraItem) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private Bitmap downloadLargeIcon() throws IOException {
        int width=0;
        String iconurl = "";
        for(Icon icon : remoteDevice.getIcons()) {
            if (icon.getWidth() > width && icon.getMimeType().toString().toLowerCase(Locale.ROOT).contains("png")) {
                width = icon.getWidth();
                String host = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://" + remoteDevice.getIdentity().getDescriptorURL().getHost() + ":" + remoteDevice.getIdentity().getDescriptorURL().getPort();
                String iconPath = icon.getUri().getPath();
                iconurl = host + iconPath;
            }
        }
        return createBitmapFromByteArray(downloadIconFromUrl(iconurl));
    }

    private Bitmap downloadSmallIcon() throws IOException {
        int width=1024;
        String iconurl = "";
        for(Icon icon : remoteDevice.getIcons()) {
            if (icon.getWidth() <= width && icon.getMimeType().toString().toLowerCase(Locale.ROOT).contains("png")) {
                width = icon.getWidth();
                String host = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://" + remoteDevice.getIdentity().getDescriptorURL().getHost() + ":" + remoteDevice.getIdentity().getDescriptorURL().getPort();
                String iconPath = icon.getUri().getPath();
                iconurl = host + iconPath;
            }
        }
        return createBitmapFromByteArray(downloadIconFromUrl(iconurl));
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

    public Bitmap getLargeIcon() {
        return largeIcon;
    }

    public Bitmap getSmallIcon() {
        return smallIcon;
    }

    public boolean isCanon() {
        if ( this.name.toLowerCase(Locale.ROOT).contains("canon") || this.description.toLowerCase(Locale.ROOT).contains("canon")) {
            return true;
        }
        return false;
    }

    public String getHost() {
        return this.remoteDevice.getIdentity().getDescriptorURL().getHost();
    }
}
