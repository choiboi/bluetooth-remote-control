package com.choiboi.apps.bluetoothremote;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

public class BluetoothService {
	
	// Member fields
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	private int mState;
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;		// Doing nothing
	public static final int STATE_LISTEN = 1;		// Listen for incoming connections
	public static final int STATE_CONNECTING = 2;	// Listen for outgoing connections
	public static final int STATE_CONNECTED = 3;	// Currently connected to a device
	
	public BluetoothService(Context context, Handler handler) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}
	
	public synchronized void start() {
		
	}
	
	/*
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}
}
