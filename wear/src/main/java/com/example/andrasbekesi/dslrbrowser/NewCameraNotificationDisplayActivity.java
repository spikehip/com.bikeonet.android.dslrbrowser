package com.example.andrasbekesi.dslrbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NewCameraNotificationDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_display);
        mTextView = (TextView) findViewById(R.id.text);
    }

}
