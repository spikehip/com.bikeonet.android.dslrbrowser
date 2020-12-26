package com.bikeonet.android.dslrbrowser;

import android.app.DownloadManager;

import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;

import java.io.IOException;

public class PhotoDetail extends Fragment {

    private static final String ARG_IMAGE_ITEM_TITLE = "image_item_title";
    private static final String ARG_IMAGE_ITEM_URL = "image_item_url";
    private static final String ARG_IMAGE_ITEM_PREVIEW_URL = "image_item_preview_url";
    private static final String UPDATE_PHOTO_DETAIL = "UPDATE_PHOTO_DETAIL";
    private static final String PHOTO_DETAIL_PREVIEW_LOADED = "PHOTO_DETAIL_PREVIEW_LOADED";
    private String itemTitle;
    private String itemUrl;
    private String itemPreviewUrl;
    private TextView detailImageTitle;
    private ImageView detailImageView;
    private Thread previewDownloadThread;
    private Bitmap preview;
    private Button downloadButton;
    private ProgressBar progressBar;

    private PhotoDetailViewModel mViewModel;

    private class UpdatePhotoDetailReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private UpdatePhotoDetailReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(PHOTO_DETAIL_PREVIEW_LOADED)) {
                progressBar.setVisibility(View.GONE);
                detailImageView.setImageBitmap(preview);
                detailImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    UpdatePhotoDetailReceiver updatePhotoDetailReceiver = new UpdatePhotoDetailReceiver();

    public static PhotoDetail newInstance(PhotoItem item) {
        PhotoDetail fragment = new PhotoDetail();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_ITEM_TITLE, item.getTitle());
        args.putString(ARG_IMAGE_ITEM_URL, item.getResourceUrl());
        args.putString(ARG_IMAGE_ITEM_PREVIEW_URL, item.getPreviewResourceUrl());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        if (getArguments() != null) {
            itemTitle = getArguments().getString(ARG_IMAGE_ITEM_TITLE);
            itemUrl = getArguments().getString(ARG_IMAGE_ITEM_URL);
            itemPreviewUrl = getArguments().getString(ARG_IMAGE_ITEM_PREVIEW_URL);
        }

        IntentFilter updatePhotoDetailIntentFilter = new IntentFilter(UPDATE_PHOTO_DETAIL);
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(
                updatePhotoDetailReceiver,
                updatePhotoDetailIntentFilter);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context context = this.getContext();
        View view = inflater.inflate(R.layout.photo_detail_fragment, container, false);
        detailImageTitle = view.findViewById(R.id.detail_image_title);
        detailImageView = view.findViewById(R.id.detail_imageView);
        downloadButton = view.findViewById(R.id.photo_detail_download_button);
        progressBar = view.findViewById(R.id.detail_progressBar);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(itemUrl))
                        .setDescription(itemUrl)
                        .setTitle(itemTitle)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
            }
        });

        detailImageTitle.setText(itemTitle);

        previewDownloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (itemPreviewUrl != null) {
                        preview = PhotoItem.createBitmapFromByteArray(PhotoItem.downloadIconFromUrl(itemPreviewUrl));
                        Intent previewDownloadFinishedIntent = new Intent(UPDATE_PHOTO_DETAIL)
                                // Puts the status into the Intent
                                .putExtra(PHOTO_DETAIL_PREVIEW_LOADED, "true");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(previewDownloadFinishedIntent);
                    }
                }
                catch (IOException e) {
                    Log.d(this.getClass().getName(), "failed to load thumbnail from "+itemUrl+" error: "+e.getMessage());
                }

            }
        });
        previewDownloadThread.start();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PhotoDetailViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.toString();
    }

    @Override
    public void onDestroy() {
        if (previewDownloadThread.isAlive() && !previewDownloadThread.isInterrupted()) {
            previewDownloadThread.interrupt();
        }
        super.onDestroy();
    }
}
