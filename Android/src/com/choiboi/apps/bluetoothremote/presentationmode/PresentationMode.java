package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.choiboi.apps.bluetoothremote.ActivitiesBridge;
import com.choiboi.apps.bluetoothremote.BluetoothService;
import com.choiboi.apps.bluetoothremote.R;

public class PresentationMode extends Activity {

    // Debugging
    private static final String TAG = "PresentationMode";

    // Member fields
    private BluetoothService mBluetoothService;
    private String mConnectedDeviceName;
    private String mLocalDeviceName;

    // Layout
    private TextView mTitle;
    private ImageView mPresSlide;

    // Values for retrieving data from Bundle
    public static final String BLUETOOTH_SERVICE = "BluetoothService";
    public static final String CONNECTED_DEVICE_NAME = "connected_device_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ onCreate ++");

        // Setup the layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.presentation_screen);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mConnectedDeviceName = bundle.getString(CONNECTED_DEVICE_NAME);
        }

        // Setup the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setText(R.string.title_connected_to);
        mTitle.append(" " + mConnectedDeviceName);

        mPresSlide = (ImageView) findViewById(R.id.slide_image);

        mBluetoothService = (BluetoothService) ActivitiesBridge.getObject();
        mLocalDeviceName = mBluetoothService.getLocalDeviceName();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "++ onKeyDown ++");

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            String command = mLocalDeviceName + ":" + BluetoothService.VOL_UP;
            mBluetoothService.write(command.getBytes());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            String command = mLocalDeviceName + ":" + BluetoothService.VOL_DOWN;
            mBluetoothService.write(command.getBytes());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
