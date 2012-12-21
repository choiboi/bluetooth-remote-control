package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.choiboi.apps.bluetoothremote.ActivitiesBridge;
import com.choiboi.apps.bluetoothremote.BluetoothService;
import com.choiboi.apps.bluetoothremote.R;

public class PresentationMode extends Activity {

    // Debugging
    private static final String TAG = "PresentationMode";
    
    // Member fields
    private BluetoothService mBluetoothService;
    
    // Values for retrieving data from Bundle
    public static final String BLUETOOTH_SERVICE = "BluetoothService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ onCreate ++");
        
        // Setup the layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.presentation_screen);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        mBluetoothService = (BluetoothService) ActivitiesBridge.getObject();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "++ onKeyDown ++");

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            String command = "SG3.CHOI" + ":" + BluetoothService.VOL_UP;
            mBluetoothService.write(command.getBytes());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            String command = "SG3.CHOI" + ":" + BluetoothService.VOL_DOWN;
            mBluetoothService.write(command.getBytes());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
