package com.bikeonet.android.dslrbrowser.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bikeonet.android.dslrbrowser.PhotoRecyclerViewAdapter;
import com.bikeonet.android.dslrbrowser.messaging.NotificationBuilder;

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

    public static List<PhotoItem> filterOnCameraHost(String host){

        if (PhotoRecyclerViewAdapter.isSelectionMode) {
            return ITEMS.stream()
                    .filter( photoItem -> photoItem.getCameraItem().getHost().equals(host) &&
                                          photoItem.isSelected())
                    .collect(Collectors.<PhotoItem>toList());
        }

        return ITEMS.stream()
                .filter( photoItem -> photoItem.getCameraItem().getHost().equals(host) )
                .collect(Collectors.<PhotoItem>toList());

    }

    public static List<PhotoItem> getAllItems(){

        if (PhotoRecyclerViewAdapter.isSelectionMode) {
            return ITEMS.stream()
                    .filter( photoItem -> photoItem.isSelected() )
                    .collect(Collectors.<PhotoItem>toList());
        }

        return ITEMS;
    }


    public static void selectItem(PhotoItem mItem, boolean isSelected) {
        int index = ITEMS.indexOf(mItem);
        ITEMS.get(index).setSelected(isSelected);
    }

}
