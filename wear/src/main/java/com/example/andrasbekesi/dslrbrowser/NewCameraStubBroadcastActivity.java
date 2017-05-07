package com.example.andrasbekesi.dslrbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class NewCameraStubBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("com.bikeonet.android.dslrbrowser.SHOW_NOTIFICATION");
        i.putExtra(NewCameraPostNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
