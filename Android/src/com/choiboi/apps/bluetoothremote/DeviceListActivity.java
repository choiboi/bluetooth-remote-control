package com.choiboi.apps.bluetoothremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
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
