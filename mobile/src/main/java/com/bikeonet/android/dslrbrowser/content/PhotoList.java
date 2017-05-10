package com.bikeonet.android.dslrbrowser.content;

import java.util.ArrayList;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class PhotoList {

    public static final ArrayList<PhotoItem> ITEMS = new ArrayList<PhotoItem>();

    public static void addItem(PhotoItem item) {
        if (!ITEMS.contains(item)) {
            ITEMS.add(item);
        }
    }

}
