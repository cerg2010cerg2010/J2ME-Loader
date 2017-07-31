package javax.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import javax.microedition.util.ContextHolder;

public class DiscoveryAgent {

	public static final int NOT_DISCOVERABLE = 0;
	public static final int GIAC = 0x9E8B33;
	public static final int LIAC = 0x9E8B00;
	public static final int CACHED = 0x00;
	public static final int PREKNOWN = 0x01;

	private static int maxID = 1;

	private BluetoothAdapter adapter;

	DiscoveryAgent(BluetoothAdapter adapter) {
		this.adapter = adapter;
	}

	public RemoteDevice[] retrieveDevices(int option) {
		return null;
	}

	public boolean startInquiry(int accessCode, final DiscoveryListener listener) throws BluetoothStateException {
		if (listener == null) {
			throw new NullPointerException("DiscoveryListener is null");
		}
		if ((accessCode != LIAC) && (accessCode != GIAC) && ((accessCode < 0x9E8B00) || (accessCode > 0x9E8B3F))) {
			throw new IllegalArgumentException("Invalid accessCode " + accessCode);
		}

		if (adapter.isDiscovering())
			return false;

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		ContextHolder.getContext().registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					RemoteDevice dev = new RemoteDevice(device);
					DeviceClass cod = new DeviceClass(device.getBluetoothClass().getDeviceClass());
					listener.deviceDiscovered(dev, cod);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					listener.inquiryCompleted(DiscoveryListener.INQUIRY_COMPLETED);
					ContextHolder.getContext().unregisterReceiver(this);
				}
			}
		}, filter);

		return adapter.startDiscovery();
	}

	public boolean cancelInquiry(DiscoveryListener listener) {
		if (listener == null) {
			throw new NullPointerException("DiscoveryListener is null");
		}
		boolean ret = adapter.cancelDiscovery();
		listener.inquiryCompleted(DiscoveryListener.INQUIRY_TERMINATED);
		return ret;
	}

	public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev, DiscoveryListener discListener)
			throws BluetoothStateException {
		if (uuidSet == null) {
			throw new NullPointerException("uuidSet is null");
		}
		if (uuidSet.length == 0) {
			// The same as on Motorola, Nokia and SE Phones
			throw new IllegalArgumentException("uuidSet is empty");
		}
		for (int u1 = 0; u1 < uuidSet.length; u1++) {
			for (int u2 = u1 + 1; u2 < uuidSet.length; u2++) {
				if (uuidSet[u1].equals(uuidSet[u2])) {
					throw new IllegalArgumentException("uuidSet has duplicate values " + uuidSet[u1].toString());
				}
			}
		}
		if (btDev == null) {
			throw new NullPointerException("RemoteDevice is null");
		}
		if (discListener == null) {
			throw new NullPointerException("DiscoveryListener is null");
		}
		for (int i = 0; attrSet != null && i < attrSet.length; i++) {
			if (attrSet[i] < 0x0000 || attrSet[i] > 0xffff) {
				throw new IllegalArgumentException("attrSet[" + i + "] not in range");
			}
		}
/*
		Thread thread = new Thread(new Runnable() {
			public void run() {
				for (UUID jsr82UUID : uuidSet) {
					java.util.UUID javaUUID = */
		return maxID++;
	}

	public boolean cancelServiceSearch(int transID) {
		return false;
	}

	public String selectService(UUID uuid, int security, boolean master) throws BluetoothStateException {
		return null;
	}

}
