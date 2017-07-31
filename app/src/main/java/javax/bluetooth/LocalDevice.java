package javax.bluetooth;

import javax.microedition.io.Connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.app.Activity;
import javax.microedition.util.ActivityResultListener;
import java.util.Hashtable;
import javax.microedition.util.ContextHolder;

public class LocalDevice implements ActivityResultListener {
	private static LocalDevice dev;
	private DiscoveryAgent agent;
	private BluetoothAdapter adapter;
	private static Hashtable<String, String> properties;
	private boolean lock = false;
	private Object monitor = new Object();

	static {
		properties = new Hashtable<String, String>();

		properties.put("bluetooth.api.version", "1.1");
		properties.put("bluetooth.master.switch", "true");
		properties.put("bluetooth.sd.attr.retrievable.max", "256");
		properties.put("bluetooth.connected.devices.max", "7");
		properties.put("bluetooth.l2cap.receiveMTU.max", "672");
		properties.put("bluetooth.sd.trans.max", "1");
		properties.put("bluetooth.connected.inquiry.scan", "true");
		properties.put("bluetooth.connected.page.scan", "true");
		properties.put("bluetooth.connected.inquiry", "true");
		properties.put("bluetooth.connected.page", "true");

	}

	private LocalDevice() throws BluetoothStateException {
		adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null)
                        throw new BluetoothStateException();
		agent = new DiscoveryAgent(adapter);
	}

	public static LocalDevice getLocalDevice() throws BluetoothStateException {
		if (dev == null)
			dev = new LocalDevice();
		return dev;
	}

	public DiscoveryAgent getDiscoveryAgent() {
		return agent;
	}

	public String getFriendlyName() {
		return adapter.getName();
	}

	public DeviceClass getDeviceClass() {
		// Will this work?
		return new DeviceClass(adapter.getRemoteDevice(adapter.getAddress()).getBluetoothClass().getDeviceClass());
	}

	public boolean setDiscoverable(int mode) throws BluetoothStateException {
		if ((mode != DiscoveryAgent.GIAC) && (mode != DiscoveryAgent.LIAC) && (mode != DiscoveryAgent.NOT_DISCOVERABLE)
				&& (mode < 0x9E8B00 || mode > 0x9E8B3F)) {
			throw new IllegalArgumentException("Invalid discoverable mode");
		}

		if (lock)
			return false;

		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		// HACK: Android does not allow us to set scan mode directly, but we can use reflection to change it.
		// However, this requires WRITE_SETTINGS permission, which is "scary" for users
		if (mode != DiscoveryAgent.GIAC)
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
		ContextHolder.addActivityResultListener(this);
		lock = true;
		ContextHolder.getCurrentActivity().startActivityForResult(discoverableIntent, 1);
		while (lock) {
			synchronized (monitor) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					lock = false;
				}
			}
		}
		// Wait until scan mode changes
		if (mode != DiscoveryAgent.GIAC) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			lock = false;

			synchronized (monitor) {
				monitor.notifyAll();
			}
		}
	}

	public static boolean isPowerOn() {
		return BluetoothAdapter.getDefaultAdapter().isEnabled();
	}

	public int getDiscoverable() {
		int scanMode = adapter.getScanMode();
		switch (scanMode) {
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
				return DiscoveryAgent.LIAC;
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
				return DiscoveryAgent.GIAC;
			case BluetoothAdapter.SCAN_MODE_NONE:
			default:
				return DiscoveryAgent.NOT_DISCOVERABLE;
		}
	}

	public static String getProperty(String property) {
		return properties.get(property);
	}

	static String androidToJavaAddress(String addr) {
		return addr.replaceAll(":", "");
	}

	public String getBluetoothAddress() {
		return androidToJavaAddress(adapter.getAddress());
	}

	// TODO
	public ServiceRecord getRecord(Connection notifier) {
		if (notifier == null) {
			throw new NullPointerException("notifier is null");
		}

		return null;
	}

	// TODO
	public void updateRecord(ServiceRecord srvRecord) throws ServiceRegistrationException {
		if (srvRecord == null) {
			throw new NullPointerException("Service Record is null");
		}
	}

}
