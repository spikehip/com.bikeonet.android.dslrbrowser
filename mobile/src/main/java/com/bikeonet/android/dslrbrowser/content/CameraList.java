package com.bikeonet.android.dslrbrowser.content;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class CameraList {


    /**
     * A map of camera items, by ID.
     */
    public static final ArrayList<CameraItem> ITEMS = new ArrayList<CameraItem>();
    private static final HashSet<String> HOSTS = new HashSet<String>();

    public static void addItem(CameraItem item) {
        if ( !HOSTS.contains( item.getHost() )) {
            HOSTS.add(item.getHost());
            ITEMS.add(item);
        }
    }

    public static void reset() {
        HOSTS.clear();
        ITEMS.clear();
    }


    public static boolean contains(CameraItem cameraItem) {
        return ITEMS.contains(cameraItem);
    }

    public static boolean remove(CameraItem item) {
        if ( ITEMS.indexOf(item) > -1 ) {
            return ITEMS.remove(item);
        }

        return false;
    }


}
