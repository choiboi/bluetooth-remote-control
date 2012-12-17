package com.choiboi.apps.bluetoothremote;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothService {
	
	// Member fields
	private final BluetoothAdapter mBluetoothAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	
	// Unique UUID
	private static final UUID _UUID = UUID.fromString("C46C11A93E424F64AB1EFC892E87B9DE");
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;		// Doing nothing
	public static final int STATE_LISTEN = 1;		// Listen for incoming connections
	public static final int STATE_CONNECTING = 2;	// Listen for outgoing connections
	public static final int STATE_CONNECTED = 3;	// Currently connected to a device
	
	// For Debugging purposes
	private static final String TAG = "BluetoothService";
	
	public BluetoothService(Context context, Handler handler) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}
	
	/*
	 * Start Bluetooth service. Begin a session in listening (server) mode.
	 */
	public synchronized void start() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		setState(STATE_LISTEN);
	}
	
	/*
	 * Start the ConnectThread to initiate a connection to a remote
	 * device.
	 * @param device The Bluetooth device to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}
		
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		// Start a thread to connect with a given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		
	}
	
	
	/*
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothRemote.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothRemote.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		
		// Start the service over the restart listening mode
		BluetoothService.this.start();
	}
	
	/*
	 * Set the state of the current connection.
	 * @param state An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		mState = state;
		
		// Give the new state to the Handler to update the UI Activity
		mHandler.obtainMessage(BluetoothRemote.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}
	
	/*
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}
	
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tempSocket = null;
			
			try {
				tempSocket = device.createRfcommSocketToServiceRecord(_UUID);
			} catch(Exception e) {
				Log.e(TAG, "create Failed ====== ConnectThread");
			}
			
			mmSocket = tempSocket;
		}
		
		public void run() {
			setName("ConnectThread");
			
			// Cancel discovery because it will slow down the connection.
			mBluetoothAdapter.cancelDiscovery();
			
			try {
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				
				// Close the socket.
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "Unable to close() socket during connection failure.",e2);
				}
				
				BluetoothService.this.start();
				return;
			}
			
			// Reset the ConnectThread because we are done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			
			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect failed.", e);
			}
		}
	}
	
	private class ConnectedThread extends Thread {
		public ConnectedThread(BluetoothSocket socket) {
			
		}

		public void cancel() {
			// TODO Auto-generated method stub
			
		}
	}
}
