package com.choiboi.apps.bluetoothremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.choiboi.apps.bluetoothremote.presentationmode.PresentationMode;

public class BluetoothService {

    // Debugging
    private static final String TAG = "BluetoothService";

    // Member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mBtRemoteHandler;
    private Handler mPresModeHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private String mLocalDeviceName;

    // UUID for this application
    private static final UUID _UUID = UUID.fromString("C46C11A9-3E42-4F64-AB1E-FC892E87B9DE");

    // Constants for current connection state
    public static final int STATE_NONE = 0;         // We are doing nothing
    public static final int STATE_LISTEN = 1;       // Now listening for incoming connections
    public static final int STATE_CONNECTING = 2;   // Now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;    // Now connected to a remote device

    // Constants that indicate command to computer
    public static final String EXIT_CMD = "EXIT";
    
    // Acknowledge from the server
    private static final String ACKNOWLEDGE = "<ACK>";
    private static final String ACKNOWLEDGE_CMD_RECEIVED = "<ACK-CMD-RECEIVED>";
    private static final String ACKNOWLEDGE_IMG_CAN_RECEIVE = "<ACK-IMG-CAN-RECEIVE>";
    private static final String ACKNOWLEDGE_IMG_SENDING = "<ACK-IMG-SENDING>";
    private static final String ACKNOWLEDGE_IMG_RECEIVED = "<ACK-IMG-RECEIVED>";

    public BluetoothService(Context context, Handler handler) {
        Log.e(TAG, "++ BluetoothService ++");
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLocalDeviceName = mBluetoothAdapter.getName();
        mState = STATE_NONE;
        mBtRemoteHandler = handler;
        mPresModeHandler = null;
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
     * @param state An integer defining the current connection state.
     */
    private synchronized void setState(int state) {
        Log.e(TAG, "--- setState ---");

        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mBtRemoteHandler.obtainMessage(BluetoothRemote.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /*
     * Return the current connection state.
     */
    public synchronized int getState() {
        Log.e(TAG, "--- getState ---");
        
        return mState;
    }
    
    /*
     * Set the Handler for PresentationMode.
     */
    public void setPresModeHandler(Handler handler) {
        mPresModeHandler = handler;
    }
    
    /*
     * Remove Handler for PresentationMode as the Activity has ended.
     */
    public void removePresModeHandler() {
    	mPresModeHandler = null;
    }
    
    /*
     * Return the name of the currently connected device.
     */
    public synchronized String getLocalDeviceName() {
        Log.e(TAG, "--- getDeviceName ---");
        
        return mBluetoothAdapter.getName();
    }

    /*
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device The BluetoothDevice to connect
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
     * @param socket The BluetoothSocket on which the connection was made
     * 
     * @param device The BluetoothDevice that has been connected
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
        Message msg = mBtRemoteHandler.obtainMessage(BluetoothRemote.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothRemote.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mBtRemoteHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /*
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.e(TAG, "--- connectionFailed ---");

        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mBtRemoteHandler.obtainMessage(BluetoothRemote.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothRemote.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mBtRemoteHandler.sendMessage(msg);
    }

    /*
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.e(TAG, "--- connectionLost ---");

        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mBtRemoteHandler.obtainMessage(BluetoothRemote.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothRemote.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mBtRemoteHandler.sendMessage(msg);
        
        // Tell PresentationMode Activity that connection has been lost
        if (mPresModeHandler != null) {
        	mPresModeHandler.obtainMessage(PresentationMode.CONNECTION_LOST).sendToTarget();
        }
    }
    
    public void disconnect() {
    	Log.i(TAG, "--- disconnect ---");
    	
    	// Disconnect device
    	ConnectedThread cThread;
    	synchronized (this) {
            // Tell UI Activity that this device is not connected to anything
            if (mState != STATE_CONNECTED) {
                mBtRemoteHandler.obtainMessage(BluetoothRemote.DEVICE_NOT_CONNECTED).sendToTarget();
                return;
            }
	    	cThread = mConnectedThread; 
    	}
    	cThread.disconnect();
    }

    /*
     * Write to the ConnectedThread in an unsynchronized manner.
     * 
     * @param out The bytes to write
     * 
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        Log.e(TAG, "--- write ---");

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
    
    public void sendCommandMain(String inputCmd) {
        Log.i(TAG, "--- sendCommandMain ---");

        // Create temporary object
        ConnectedThread connectedThd;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            connectedThd = mConnectedThread;
        }
        
        connectedThd.write(inputCmd.getBytes());
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

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
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
        
        private boolean mIsDisconnect;
        
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
            
            // Initially set it to false as the user did not choose to disconnect
            mIsDisconnect = false;
        }

        public void run() {
            Log.i(TAG, "+++ BEGIN mConnectedThread +++");
            
            byte[] buffer = new byte[512];
            int bytes;
            
            while (true) {
                try {                    
                    bytes = mmInStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);
                    
                    if (receivedData.equals(ACKNOWLEDGE_CMD_RECEIVED)) {
                        if (mPresModeHandler != null) {
                            mPresModeHandler.obtainMessage(PresentationMode.IMAGE_TRANSFER_DONE).sendToTarget();
                        }
                        receiveScreenshot();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // Invoke connectionLost() only if it lost connection with the server
                    if (!mIsDisconnect) {
                        connectionLost();
                    }
                    BluetoothService.this.start();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
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
                Log.i(TAG, "++ write wrote to outstream ++");
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        public void receiveScreenshot() {
            Log.i(TAG, "--- receiveScreenshot ---");
            
            byte[] buffer = new byte[512];
            int bytes;
            
            try {
                // Notify server that image can be sent
                mmOutStream.write(ACKNOWLEDGE_IMG_CAN_RECEIVE.getBytes());
                
                // Receive that image will be sent
                bytes = mmInStream.read(buffer);
                if (!ACKNOWLEDGE_IMG_SENDING.equals(new String(buffer, 0, bytes))) 
                    return;
                
                // Send acknowledgment
                mmOutStream.write(ACKNOWLEDGE.getBytes());
                
                // Receive image
                // Read Image from the InputStream and decode it into bitmap
                BitmapFactory.Options Bitmp_Options = new BitmapFactory.Options();
                Bitmp_Options.inJustDecodeBounds = true;
                mmInStream.mark(mmInStream.available());
                Bitmap bmp = BitmapFactory.decodeStream(mmInStream);
                
                // Send the obtained image to PresentationMode Activity
                if (mPresModeHandler != null)
                    mPresModeHandler.obtainMessage(PresentationMode.RECEIVED_IMAGE, -1, -1, bmp).sendToTarget();
                
                // Send Acknowledge image received
                mmOutStream.write(ACKNOWLEDGE_IMG_RECEIVED.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                // Invoke connectionLost() only if it lost connection with the server
                if (!mIsDisconnect) {
                    connectionLost();
                }
                BluetoothService.this.start();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        
        /*
         * Disconnect the currently connected device.
         */
        public void disconnect() {
            try {
                // Set it to true so that it won't invoke connectionLost()
                mIsDisconnect = true;

                // Close all input and output streams
                if (mmOutStream != null)
                    mmOutStream.close();

                if (mmInStream != null)
                    mmInStream.close();

                // Close the socket
                if (mmSocket != null)
                    mmSocket.close();

                // Tell the UI Activity that the device has been successfully disconnected
                mBtRemoteHandler.obtainMessage(BluetoothRemote.DEVICE_DISCONNECT_SUCCESS).sendToTarget();
                BluetoothService.this.start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            BluetoothService.this.start();
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
