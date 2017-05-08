package com.bikeonet.android.dslrbrowser;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.CameraList;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.content.PhotoList;
import com.bikeonet.android.dslrbrowser.messaging.LocalBroadcastMessageBuilder;
import com.bikeonet.android.dslrbrowser.upnp.BrowseManager;
import com.bikeonet.android.dslrbrowser.upnp.ContentDirectoryRegistryListener;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.registry.RegistryListener;

public class MainActivity extends AppCompatActivity implements CameraItemFragment.OnCameraListFragmentInteractionListener,
    PhotoListFragment.OnPhotoListFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener
{


    CameraItemFragment cameraListFragment = CameraItemFragment.newInstance(1);
    SettingsFragment settingsFragment = SettingsFragment.newInstance("","");
    PhotoListFragment photoListFragment = PhotoListFragment.newInstance(4);

    private class UpdateUIListReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private UpdateUIListReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {

            if ( intent.getExtras().containsKey(LocalBroadcastMessageBuilder.CAMERA_LIST_NEW_CONTENT)) {
                cameraListFragment.getViewAdapter().notifyDataSetChanged();
            }

            if ( intent.getExtras().containsKey(LocalBroadcastMessageBuilder.PHOTO_LIST_NEW_CONTENT)) {
                if (photoListFragment != null && photoListFragment.getViewAdapter() != null ) {
                    photoListFragment.getViewAdapter().notifyDataSetChanged();
                }
            }

        }
    }

    UpdateUIListReceiver updateCameraListReceiver = new UpdateUIListReceiver();
    private RegistryListener registryListener;
    private AndroidUpnpService upnpService;
    private ServiceConnection serviceConnection;

    @Override
    public void onCameraListFragmentInteraction(CameraItem item) {
        Log.d(this.getClass().getName(), item.toString());
    }

    @Override
    public void onPhotoListFragmentInteraction(PhotoItem item) {
        Log.d(this.getClass().getName(), item.toString());
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(this.getClass().getName(), uri.toString());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showCameraList();
                    return true;
                case R.id.navigation_dashboard:
                    showPhotoList();
                    return true;
                case R.id.navigation_notifications:
                    showSettings();
                    return true;
            }
            return false;
        }

    };

    private void showSettings() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, settingsFragment, settingsFragment.getTag());
        ft.commit();
    }

    private void showPhotoList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, photoListFragment, photoListFragment.getTag());
        ft.commit();
    }

    private void showCameraList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, cameraListFragment, cameraListFragment.getTag());
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showCameraList();

        this.registryListener = new ContentDirectoryRegistryListener(this.getApplicationContext());
        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                upnpService = (AndroidUpnpService) service;
                BrowseManager.initializeInstance(upnpService);
                upnpService.getRegistry().addListener(registryListener);
                upnpService.getControlPoint().search();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                upnpService = null;
            }
        };

        bindService(new Intent(this, AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter updateCameraListIntentFilter = new IntentFilter(LocalBroadcastMessageBuilder.UPDATE_UI_LIST);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateCameraListReceiver,
                updateCameraListIntentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        getApplicationContext().unbindService(serviceConnection);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_upnp:
                CameraList.reset();
                PhotoList.ITEMS.clear();
                if (cameraListFragment.getViewAdapter() != null) {
                    cameraListFragment.getViewAdapter().notifyDataSetChanged();
                }
                if (photoListFragment != null && photoListFragment.getViewAdapter() != null ) {
                    photoListFragment.getViewAdapter().notifyDataSetChanged();
                }
                if (upnpService != null && upnpService.getControlPoint() != null ) {
                    upnpService.getControlPoint().search();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
