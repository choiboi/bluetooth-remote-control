package com.choiboi.apps.bluetoothremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.choiboi.apps.bluetoothremote.presentationmode.PresentationMode;

public class BluetoothRemote extends Activity {

    // Debugging
    private static final String TAG = "BluetoothRemote";

    // Member fields
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private String mConnectedDeviceName = null;
    private String mLocalDeviceName = null;

    // Layout
    private TextView mTitle;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int DEVICE_NOT_CONNECTED = 6;

    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String DEVICE_CONNECTED = "DEVICE_CONNECTED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ onCreate ++");

        // Setup the layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main_screen);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Setup the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Retrieve the Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If Bluetooth is not supported then mBluetoothAdapter should be null
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "++ onStart ++");

        mLocalDeviceName = mBluetoothAdapter.getName();

        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // Start the Bluetooth Service
            if (mBluetoothService == null)
                setupBluetoothService();
            Log.e(TAG, "++++ on start else ++++");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "++ onResume ++");

        if (mBluetoothService != null) {
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                mBluetoothService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "++ onDestroy ++");

        if (mBluetoothService != null)
            mBluetoothService.stop();
    }

    /*
     * Whenever the Connect a Device button is clicked it will invoke this
     * function and launch the DeviceListActivity to see devices and do scan.
     */
    public void connectButtonClicked(View v) {
        Log.i(TAG, "--- connectButtonClicked ---");
        
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /*
     * Whenever the Make Discoverable button is clicked it will invoke this
     * function and will ensure that this device is discoverable by others.
     */
    public void discoverableButtonClicked(View v) {
        Log.i(TAG, "--- discoverableButtonClicked ---");
        
        ensureDiscoverable();
    }
    
    public void disconnectButtonClicked(View v) {
    	Log.i(TAG, "--- disconnectButtonClicked ---");
    	
    	mBluetoothService.disconnect();
    }

    /*
     * Initialize BluetoothService to perform Bluetooth connections.
     */
    private void setupBluetoothService() {
        Log.e(TAG, "--- setupBluetoothService ---");

        mBluetoothService = new BluetoothService(this, mHandler);
    }

    /*
     * Start making the device discoverable for 300 seconds.
     */
    private void ensureDiscoverable() {
        Log.e(TAG, "--- ensureDiscoverable ---");

        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    /*
     * Once the device is connected, switch to Presentation Mode were all
     * the input controls necessary for presentation will be there.
     */
    private void startPresentationMode() {
        Intent presModeIntent = new Intent(this, PresentationMode.class);
        presModeIntent.putExtra(PresentationMode.CONNECTED_DEVICE_NAME, mConnectedDeviceName);
        
        // Pass BluetoothService object to PresentationMode Activity
        ActivitiesBridge.setObject(mBluetoothService);
        startActivity(presModeIntent);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "--- onActivityResult ---");

        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mBluetoothService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupBluetoothService();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "--- Handler handleMessage ---");

            switch (msg.what) {
            // Changes the message on the application header
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(" " + mConnectedDeviceName);

                    // Send the name of the connected device to the server
                    String command = DEVICE_CONNECTED + ":" + mLocalDeviceName;
                    mBluetoothService.write(command.getBytes()); 

                    // Move to screen for presentation controls
                    startPresentationMode();
                    break;
                case BluetoothService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // Save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
            	break;
			case DEVICE_NOT_CONNECTED:
				Toast.makeText(getApplicationContext(), R.string.not_connected_msg, Toast.LENGTH_SHORT).show();
				break;
			}
        }
    };
}
