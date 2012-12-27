package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private String mPresentationProgram = "";

    // Layout
    private TextView mTitle;

    // Values for retrieving data from Bundle
    public static final String BLUETOOTH_SERVICE = "BluetoothService";
    public static final String CONNECTED_DEVICE_NAME = "connected_device_name";
    public static final String PROGRAM = "program";
    
    // Intent request codes
    private static final int REQUEST_PROGRAM_USED = 1;
    
    // Message types sent from BluetoothService Handler
    public static final int RECEIVED_IMAGE = 1;
    public static final int CONNECTION_LOST = 2;
    
    // Constants that indicate command to computer
    private static final String LEFT = "LEFT";
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String RIGHT = "RIGHT";
    private static final String GO_FULLSCREEN = "GO_FULLSCREEN";
    private static final String EXIT_FULLSCREEN = "EXIT_FULLSCREEN";
    private static final String APP_STARTED = "APP_STARTED";
    
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

        // Setup the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setText(R.string.title_connected_to);
        mTitle.append(" " + mConnectedDeviceName);

        mBluetoothService = (BluetoothService) ActivitiesBridge.getObject();
        mLocalDeviceName = mBluetoothService.getLocalDeviceName();
        
        // Ask user which presentation program they will be using
        selectProgramDialog();
    }

    @Override
	protected void onStart() {
		super.onStart();
		mBluetoothService.setPresModeHandler(mHandler);
		
		String command = mLocalDeviceName + ":" + APP_STARTED;
		mBluetoothService.write(command.getBytes());
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		mBluetoothService.removePresModeHandler();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.presentation_mode_menu, menu);
        return true;
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
    
    /*
     * This will start an Activity which opens up a dialog asking the user to
     * select which presentation program they will be using.
     */
    private void selectProgramDialog() {
        Intent serverIntent = new Intent(this, ProgramSelectActivity.class);
        startActivityForResult(serverIntent, REQUEST_PROGRAM_USED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
        case R.id.change_presentation_program:
            selectProgramDialog();
            return true;
        }
        
        return false;
    }

    /*
     * Invoked whenever an Activity that is looking for result is finished.
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "--- onActivityResult ---");

        if (resultCode == Activity.RESULT_OK) {
            String progSelection = data.getExtras().getString(PROGRAM);
            TextView modeTitle = (TextView) findViewById(R.id.presentation_mode_title);
            modeTitle.setText(R.string.presentation_title);
            modeTitle.append(" " + progSelection);
            
            if (progSelection.equals(getResources().getString(R.string.micro_ppt))) {
                mPresentationProgram = MICROSOFT_POWERPOINT;
            } else if (progSelection.equals(getResources().getString(R.string.adobe_pdf))) {
                mPresentationProgram = ADOBE_READER;
            } else if (progSelection.equals(getResources().getString(R.string.browser))) {
                mPresentationProgram = BROWSER;
            }
        }
    }
    
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch(msg.what) {
            case RECEIVED_IMAGE:
                // Get the image and set it to ImageView
                Bitmap b = (Bitmap) msg.obj;
                ImageView tv = (ImageView) findViewById(R.id.slide_image);
                tv.setImageBitmap(b);
                break;
            case CONNECTION_LOST:
            	mTitle.setText(R.string.title_not_connected);
            	finish();
            	break;
            }
        }
    };
}
