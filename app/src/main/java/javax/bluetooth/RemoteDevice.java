package javax.bluetooth;

import java.io.IOException;

import javax.microedition.io.Connection;
import android.bluetooth.BluetoothDevice;

public class RemoteDevice {
	BluetoothDevice dev;

	RemoteDevice(BluetoothDevice dev) {
		this.dev = dev;
	}

	protected RemoteDevice(String address) {
		if (address == null) {
			throw new NullPointerException("address is null");
		}
		throw new RuntimeException("Can't initialize bluetooth support");
	}

	public boolean isTrustedDevice() {
		return false;
	}

	public String getFriendlyName(boolean alwaysAsk) throws IOException {
		return null;
	}

	public final String getBluetoothAddress() {
		return null;
	}

	public boolean equals(Object obj) {
		return false;
	}

	public int hashCode() {
		return 0;
	}

	public static RemoteDevice getRemoteDevice(Connection conn) throws IOException {
		return null;
	}

	public boolean authenticate() throws IOException {
		return false;
	}

	public boolean authorize(javax.microedition.io.Connection conn) throws IOException {
		return false;
	}

	public boolean encrypt(javax.microedition.io.Connection conn, boolean on) throws IOException {
		return false;
	}

	public boolean isAuthenticated() {
		return false;
	}

	public boolean isAuthorized(javax.microedition.io.Connection conn) throws IOException {
		return false;
	}

	public boolean isEncrypted() {
		return false;
	}

}
