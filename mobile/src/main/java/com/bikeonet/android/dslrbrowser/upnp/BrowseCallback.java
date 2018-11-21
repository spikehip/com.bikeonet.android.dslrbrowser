package com.bikeonet.android.dslrbrowser.upnp;

import android.util.Log;

import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.content.PhotoList;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Photo;

/**
 * Created by andrasbekesi on 07/05/17.
 */

public class BrowseCallback extends Browse {


        private static final String TAG = BrowseCallback.class.getName();

        private String currentNode;
        private DIDLContent receivedDIDL = null;
        private BrowseManager manager;
        private RemoteDevice device;

        public BrowseCallback(Service service, String containerId,
                              BrowseManager manager, RemoteDevice device) {
            this(service, containerId, BrowseFlag.DIRECT_CHILDREN);
            currentNode = containerId;
            this.manager = manager;
            this.device = device;

        }

        private BrowseCallback(Service service, String containerId, BrowseFlag flag) {
            super(service, containerId, flag);
        }

        @Override
        public void received(ActionInvocation actionInvocation, DIDLContent didl) {

            Log.d(TAG, actionInvocation.getFailure() != null ? actionInvocation
                    .getFailure().getMessage()
                    : "Received action invocation no failure");
            receivedDIDL = didl;

            for( Container container : didl.getContainers() ) {
                Log.i(TAG, "Queue container id " + container.getId() + " parent "
                        + container.getParentID());
                manager.queueNode(device, container.getId());
            }

            for (Item item : receivedDIDL.getItems()) {
                Log.i(TAG, "Discovered item : " + item.getTitle());
                if (item instanceof Photo) {

                    CameraItem cameraItem = new CameraItem(device, false);
                    PhotoItem photoItem = new PhotoItem();
                    photoItem.setCameraItem(cameraItem);
                    photoItem.setTitle(item.getTitle());


                    //select the largest
                    long size = 0;
                    for (Res res : item.getResources()) {
                        try {
                            if (res.getResolution() != null && (res.getSize()!=null?res.getSize():0) > size) {
                                size = res.getSize();
                                photoItem.setResourceUrl(res.getValue());
                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "failed size for "+res.getValue());
                        }
                        Log.i(TAG, res.getValue());
                    }

                    //select the smallest
                    for (Res res : item.getResources()) {
                        Log.i(TAG, res.getValue());
                        try {
                            if (res.getProtocolInfo() != null && res.getProtocolInfo().getAdditionalInfo() != null && res.getProtocolInfo().getAdditionalInfo().toUpperCase().contains("JPEG_TN") ) {
                                photoItem.setThumbnailResourceUrl(res.getValue());
                                break;
                            }
                            long currentSize = res.getSize()!=null?res.getSize():0;
                            if (res.getResolution() != null && currentSize > 0 && currentSize < size ) {
                                size = res.getSize();
                                photoItem.setThumbnailResourceUrl(res.getValue());
                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "failed size for "+res.getValue());
                        }
                    }

                    photoItem.downloadThumbnail();
                    PhotoList.addItem(photoItem);

                }
            }


        }

        @Override
        public void updateStatus(Status status) {
            // Called before and after loading the DIDL content
            Log.i(TAG, "UpdateStatus for container " + currentNode + " is : "
                    + status.toString());

		/*
		 * Loading starts
		 */
            if (status.toString().equals("LOADING")) {
                manager.setLoading(true);
            }

		/*
		 * Loading finished. If we have a device list item to append images to,
		 * do it. if not, skip silently
		 */
            if (status.toString().equals("OK") ) {
//                if (deviceDisplay != null) {
//                    int photocount = doHydrate();
//			/* browse completed */
//
//                    if (photocount > 0) {
//                        String deviceName = device.isFullyHydrated() ? device
//                                .getDetails().getFriendlyName() : device
//                                .getDisplayString() + " *";
//
//                    }
//                }
//                else {
//				/* failed to insert records, re-query */
//                    manager.queueNode(device, currentNode);
//                }

                manager.setLoading(false);
            }
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation,
                            String defaultMsg) {
            invocation.getFailure().printStackTrace();
            Log.e(TAG, "FAILURE for container " + currentNode + " Action "
                    + invocation.getAction().getName());
            Log.e(TAG, "FAILURE: Operation "
                    + (operation == null ? "null" : operation.getResponseDetails()));
            Log.e(TAG, "FAILURE: Message: " + defaultMsg);
            Log.e(TAG, "Retry "+currentNode);
            manager.queueNode(device, currentNode);
            manager.setLoading(false);
        }

    }
