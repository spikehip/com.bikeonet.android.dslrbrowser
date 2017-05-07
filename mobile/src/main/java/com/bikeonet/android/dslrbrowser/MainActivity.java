package com.bikeonet.android.dslrbrowser;

import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.messaging.LocalBroadcastMessageBuilder;
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
                photoListFragment.getViewAdapter().notifyDataSetChanged();
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
                upnpService.getRegistry().addListener(registryListener);
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

}
