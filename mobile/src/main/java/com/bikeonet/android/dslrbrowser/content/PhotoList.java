package com.bikeonet.android.dslrbrowser.content;

import java.util.ArrayList;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class PhotoList {

    public static final ArrayList<PhotoItem> ITEMS = new ArrayList<PhotoItem>();

    private static void addItem(PhotoItem item) {
        ITEMS.add(item);
    }

}
