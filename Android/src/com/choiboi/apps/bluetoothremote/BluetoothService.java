package com.choiboi.apps.bluetoothremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	// Debugging
	private static final String TAG = "BluetoothService";

	// Member fields
	private final BluetoothAdapter mBluetoothAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	private String mLocalDeviceName;

	// UUID for this application
	private static final UUID _UUID = UUID.fromString("C46C11A9-3E42-4F64-AB1E-FC892E87B9DE");

	// Constants for current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	// Constants that indicate command to computer
	public static final String EXIT_CMD = "EXIT";
	public static final String VOL_UP = "VK_RIGHT";
	public static final String VOL_DOWN = "VK_LEFT";
	public static final int MOUSE_MOVE = 3;

	public BluetoothService(Context context, Handler handler) {
		Log.e(TAG, "++ BluetoothService ++");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mLocalDeviceName = mBluetoothAdapter.getName();
		mState = STATE_NONE;
		mHandler = handler;
	}
    
    public synchronized void start() {
    	Log.e(TAG, "--- start ---");
    	
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }

        setState(STATE_LISTEN);
    }
    
    /*
     * Stop all threads.
     */
    public synchronized void stop() {
        Log.e(TAG, "--- stop ---");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }
        
        // Cancel any thread currently running a connection	
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }
        
        setState(STATE_NONE);
    }
    
    /*
     * Set the current state of the connection.
     * 
     * @param state	An integer defining the current connection state.
     */
    private synchronized void setState(int state) {
    	Log.e(TAG, "--- setState ---");
    	
    	mState = state;
    	// Give the new state to the Handler so the UI Activity can update
    	mHandler.obtainMessage(BluetoothRemote.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    
    /*
     * Return the current connection state.
     */
    public synchronized int getState() {
    	Log.e(TAG, "--- getState ---");
    	
    	return mState;
    }
    
    /*
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
    	Log.e(TAG, "--- connect to: " + device + " ---");

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

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    /*
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     * 
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.e(TAG, "--- connected ---");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothRemote.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothRemote.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        
        setState(STATE_CONNECTED);
    }
    
    /*
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
    	Log.e(TAG, "--- connectionFailed ---");
    	
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothRemote.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothRemote.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
	/*
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		Log.e(TAG, "--- connectionLost ---");

		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothRemote.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothRemote.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}
    
	/*
	 * Write to the ConnectedThread in an unsynchronized manner.
	 * 
	 * @param out The bytes to write
	 * 
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		Log.e(TAG, "--- write byte[] ---");

		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}
    
	/*
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			Log.e(TAG, "+++ create ConnectThread +++");
			
			mmDevice = device;
			BluetoothSocket tmpBluetoothSocket = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmpBluetoothSocket = device.createRfcommSocketToServiceRecord(_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmpBluetoothSocket;
		}

		public void run() {
			Log.e(TAG, "+++ BEGIN mConnectThread +++");
			
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mBluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				// Start the service over to restart listening mode
				BluetoothService.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
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
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/*
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.e(TAG, "+++ create ConnectedThread +++");
			
			mmSocket = socket;
			InputStream tmpInStream = null;
			OutputStream tmpOutStream = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpInStream = socket.getInputStream();
				tmpOutStream = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpInStream;
			mmOutStream = tmpOutStream;
		}

		public void run() {
			Log.e(TAG, "+++ BEGIN mConnectedThread +++");
			
			byte[] buffer = new byte[1024];

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					int bytes = mmInStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(BluetoothRemote.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/*
		 * Write to the connected OutStream.
		 * 
		 * @param buffer The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				Log.e(TAG, "++ write wrote to outstream ++");
				mmOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				String command = mLocalDeviceName + ":" + EXIT_CMD;
				mmOutStream.write(command.getBytes());
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
