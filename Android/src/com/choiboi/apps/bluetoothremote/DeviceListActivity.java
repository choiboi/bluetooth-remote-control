package com.choiboi.apps.bluetoothremote;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListActivity extends Activity {
	
	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	// Member fields
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		
		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);
		
		// Initiate the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});
		
		// Initialize array adapters. One for already paired devices
		// and one for newly discovered devices.
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		
		// Find and setup the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		// Find and setup this ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        
        // Register for broadcasts when discovery is finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
        	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
        	for (BluetoothDevice device : pairedDevices) {
        		mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        	}
        } else {
        	String noDevices = getResources().getText(R.string.none_paired).toString();
        	mPairedDevicesArrayAdapter.add(noDevices);
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Make sure we are not doing more discovery
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		
		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}
	
	/*
	 * Start device discover with BluetoothAdapter.
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		
		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		
		// Stop if we are already discovering
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		
		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}
	
	// On-click listener for all devices in the ListView
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because we are about to connect
			mBluetoothAdapter.cancelDiscovery();
			
			// Get the device MAC address, which is the last 17 chars in the View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			
			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			
			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
 	};
 	
 	// The BroadcastReceiver that listens for discovered devices and changes
 	// the title when discovery is finished.
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get BluetoothDevice object from Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				// If it's already paired, skip it, because it has been listed already
 				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
 					mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
 				}
 			// When discovery is finished, change the Activity title
 			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 				setProgressBarIndeterminateVisibility(false);
 				setTitle(R.string.select_device);
 				if (mNewDevicesArrayAdapter.getCount() == 0) {
 					String noDevices = getResources().getText(R.string.none_found).toString();
 					mNewDevicesArrayAdapter.add(noDevices);
 				}
 			}
 		}
 	};
}
