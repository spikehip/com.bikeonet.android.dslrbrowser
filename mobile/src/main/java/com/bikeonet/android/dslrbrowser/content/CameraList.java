package com.bikeonet.android.dslrbrowser.content;

import java.util.ArrayList;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class CameraList {


    /**
     * A map of camera items, by ID.
     */
    public static final ArrayList<CameraItem> ITEMS = new ArrayList<CameraItem>();

    private static void addItem(CameraItem item) {
        ITEMS.add(item);
    }

}
