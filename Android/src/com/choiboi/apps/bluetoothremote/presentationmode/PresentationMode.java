package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

    // Values for retrieving data from Bundle
    public static final String BLUETOOTH_SERVICE = "BluetoothService";
    public static final String CONNECTED_DEVICE_NAME = "connected_device_name";
    
    // Constants that indicate command to computer
    public static final String LEFT = "LEFT";
    public static final String DOWN = "DOWN";
    public static final String UP = "UP";
    public static final String RIGHT = "RIGHT";

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

        mBluetoothService = (BluetoothService) ActivitiesBridge.getObject();
        mLocalDeviceName = mBluetoothService.getLocalDeviceName();
    }

    /*
     * Invoked whenever the left button is pressed.
     */
    public void leftArrow(View v) {
        Log.i(TAG, "--- leftArrow ---");

        String command = mLocalDeviceName + ":" + LEFT;
        mBluetoothService.write(command.getBytes());
    }

    /*
     * Invoked whenever the down button is pressed.
     */
    public void downArrow(View v) {
        Log.i(TAG, "--- downArrow ---");

        String command = mLocalDeviceName + ":" + DOWN;
        mBluetoothService.write(command.getBytes());
    }

    /*
     * Invoked whenever the up button is pressed.
     */
    public void upArrow(View v) {
        Log.i(TAG, "--- upArrow ---");

        String command = mLocalDeviceName + ":" + UP;
        mBluetoothService.write(command.getBytes());
    }

    /*
     * Invoked whenever the right button is pressed.
     */
    public void rightArrow(View v) {
        Log.i(TAG, "--- rightArrow ---");

        String command = mLocalDeviceName + ":" + RIGHT;
        mBluetoothService.write(command.getBytes());
    }
}
