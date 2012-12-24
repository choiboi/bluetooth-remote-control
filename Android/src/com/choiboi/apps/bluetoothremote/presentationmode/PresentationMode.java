package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.choiboi.apps.bluetoothremote.ActivitiesBridge;
import com.choiboi.apps.bluetoothremote.BluetoothService;
import com.choiboi.apps.bluetoothremote.DeviceListActivity;
import com.choiboi.apps.bluetoothremote.R;

public class PresentationMode extends Activity {

    // Debugging
    private static final String TAG = "PresentationMode";

    // Member fields
    private BluetoothService mBluetoothService;
    private String mConnectedDeviceName;
    private String mLocalDeviceName;
    private String mPresentationProgram = "";

    // Layout
    private TextView mTitle;
    private TextView mModeTitle;

    // Values for retrieving data from Bundle
    public static final String BLUETOOTH_SERVICE = "BluetoothService";
    public static final String CONNECTED_DEVICE_NAME = "connected_device_name";
    public static final String PROGRAM = "program";
    
    // Intent request codes
    private static final int REQUEST_PROGRAM_USED = 1;
    
    // Constants that indicate command to computer
    private static final String LEFT = "LEFT";
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String RIGHT = "RIGHT";
    private static final String GO_FULLSCREEN = "GO_FULLSCREEN";
    private static final String EXIT_FULLSCREEN = "EXIT_FULLSCREEN";
    
    // Presentation program constants also used as commands sent to computer
    private static final String BROWSER = "BROWSER";
    private static final String MICROSOFT_POWERPOINT = "MICRO_PPT";
    private static final String ADOBE_READER = "ADOBE_PDF";

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
        
        mModeTitle = (TextView) findViewById(R.id.presentation_mode_title);

        // Setup the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setText(R.string.title_connected_to);
        mTitle.append(" " + mConnectedDeviceName);

        mBluetoothService = (BluetoothService) ActivitiesBridge.getObject();
        mLocalDeviceName = mBluetoothService.getLocalDeviceName();
        
        Intent serverIntent = new Intent(this, ProgramSelectActivity.class);
        startActivityForResult(serverIntent, REQUEST_PROGRAM_USED);
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
    
    /*
     * Invoked whenever the go fullscreen button is pressed.
     */
    public void goFullscreenPresentation(View v) {
        Log.i(TAG, "--- goFullscreenPresentation ---");
        
        String command = mLocalDeviceName + ":" + GO_FULLSCREEN + ":" + mPresentationProgram;
        mBluetoothService.write(command.getBytes());
    }
    
    /*
     * Invoked whenever the exit fullscreen button is pressed.
     */
    public void exitFullscreenPresentation(View v) {
        Log.i(TAG, "--- exitFullscreenPresentation ---");
        
        String command = mLocalDeviceName + ":" + EXIT_FULLSCREEN + ":" + mPresentationProgram;
        mBluetoothService.write(command.getBytes());
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "--- onActivityResult ---");

        mModeTitle.setText(R.string.presentation_title);
        if (resultCode == Activity.RESULT_OK) {
            String progSelection = data.getExtras().getString(PROGRAM);
            mModeTitle.append(" " + progSelection);
            
            if (progSelection.equals(getResources().getString(R.string.micro_ppt))) {
                mPresentationProgram = MICROSOFT_POWERPOINT;
            } else if (progSelection.equals(getResources().getString(R.string.adobe_pdf))) {
                mPresentationProgram = ADOBE_READER;
            } else if (progSelection.equals(getResources().getString(R.string.browser))) {
                mPresentationProgram = BROWSER;
            }
        }
    }
}
