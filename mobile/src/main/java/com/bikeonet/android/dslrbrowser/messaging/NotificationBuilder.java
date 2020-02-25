package com.bikeonet.android.dslrbrowser.messaging;

import android.app.PendingIntent;
import android.content.Context;
import com.bikeonet.android.dslrbrowser.MainActivity;
import com.bikeonet.android.dslrbrowser.R;
import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.CameraList;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.content.PhotoList;

import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/**
 * Created by andrasbekesi on 14/05/17.
 */

public class NotificationBuilder {

    private static Context mContext;
    private static int notificationId = 001;

    public static void showNotification(PhotoItem item) {

        if (mContext != null) {
            // Build intent for notification content
            Intent viewIntent = new Intent(mContext, MainActivity.class);
            //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(mContext, 0, viewIntent, 0);

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setHintHideIcon(true)
                            .setBackground(item.getThumbnail());


            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle(item.getTitle())
                            .setContentIntent(viewPendingIntent)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(item.getThumbnail()))
                            .addAction(R.drawable.common_full_open_on_phone, mContext.getString(R.string.download_to_album), viewPendingIntent)
                        .extend(wearableExtender);

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);

            // Issue the notification with notification manager.
            notificationManager.notify(notificationId, notificationBuilder.build());
            //notificationId++;
        }
    }

    public static void showNotification(CameraItem item) {

        if (mContext != null) {
            // Build intent for notification content
            Intent viewIntent = new Intent(mContext, MainActivity.class);
            viewIntent.setAction("VIEW_CAMERA_ITEM_ACTION");
            viewIntent.putExtra("cameraId", item.getHost());
            //Intent syncIntent = LocalBroadcastMessageBuilder.buildSyncCameraMessage(item);
            Intent syncIntent = new Intent(mContext, MainActivity.class);
            syncIntent.setAction(LocalBroadcastMessageBuilder.DSLRBROWSER_SYNC_CAMERA);
            syncIntent.putExtra("host", item.getHost());
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(mContext, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent syncPendingIntent = PendingIntent.getActivity(mContext, 0, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            int numberOfPhotosOnCamera = PhotoList.filterOnCameraHost(item.getHost()).size();

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle(item.getName())
                            .setContentText("Found "+numberOfPhotosOnCamera+" photos.")
                            .setContentIntent(viewPendingIntent)
                            .addAction(R.drawable.common_full_open_on_phone, mContext.getString(R.string.sync_with_phone), syncPendingIntent);

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);

            // Issue the notification with notification manager.
            notificationManager.notify(1000 + notificationId + CameraList.ITEMS.indexOf(item), notificationBuilder.build());
            //notificationId++;
        }
    }


    public static void setContext(Context mContext) {
        NotificationBuilder.mContext = mContext;
    }
}
