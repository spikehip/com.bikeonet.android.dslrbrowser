package com.bikeonet.android.dslrbrowser.messaging;

import android.content.Intent;

import com.bikeonet.android.dslrbrowser.content.CameraItem;

/**
 * Created by andrasbekesi on 04/05/17.
 */

public class LocalBroadcastMessageBuilder {

    public final static String CAMERA_LIST_NEW_CONTENT = "CAMERA_LIST_NEW_CONTENT";
    public final static String PHOTO_LIST_NEW_CONTENT = "PHOTO_LIST_NEW_CONTENT";
    public static final String UPDATE_UI_LIST = "UPDATE_UI_LIST";
    public static final String DSLRBROWSER_SYNC_CAMERA = "dslrbrowser.sync.camera";

    public static Intent buildCameraListNewContentMessage() {
        Intent localIntent =
                new Intent(UPDATE_UI_LIST)
                        // Puts the status into the Intent
                        .putExtra(CAMERA_LIST_NEW_CONTENT, "true");
        return localIntent;
    }

    public static Intent buildPhotoListNewContentMessage() {
        Intent localIntent =
                new Intent(UPDATE_UI_LIST)
                        // Puts the status into the Intent
                        .putExtra(PHOTO_LIST_NEW_CONTENT, "true");
        return localIntent;
    }

    public static Intent buildSyncCameraMessage(CameraItem item) {
        Intent syncIntent = new Intent(DSLRBROWSER_SYNC_CAMERA);
        syncIntent.putExtra("host", item.getHost());
        syncIntent.putExtra("id", item.getId());
        return syncIntent;
    }
}
