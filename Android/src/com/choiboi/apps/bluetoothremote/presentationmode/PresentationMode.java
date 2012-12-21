package com.choiboi.apps.bluetoothremote.presentationmode;

import com.choiboi.apps.bluetoothremote.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class PresentationMode extends Activity {

    // Debugging
    private static final String TAG = "PresentationMode";
    
    // Member fields
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ onCreate ++");
        
        // Setup the layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.presentation_screen);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
    }
}
