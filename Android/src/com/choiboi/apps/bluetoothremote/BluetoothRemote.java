package com.choiboi.apps.bluetoothremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class BluetoothRemote extends Activity {
	
	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_TOAST = 5;
	
	// Key names received from the BluetoothService Handler
	public static final String TOAST = "toast";
	
	// Intent request codes
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup the window layout
		setContentView(R.layout.main);
		
		// Get default Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// mBluetoothAdapter will be null if Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is NOT Available!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Request Bluetooth to be turned on, if it is not on
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
		// Otherwise, setup command service
		} else {
			// Initialize Bluetooth service to handle connections
			if (mBluetoothService == null) {
				mBluetoothService = new BluetoothService(this, mHandler);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBluetoothService != null) {
			if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
				mBluetoothService.start();
			}
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	// Handler gets the information back from the CommandService
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			}
		}
	};
}
