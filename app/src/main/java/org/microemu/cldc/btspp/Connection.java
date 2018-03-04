package org.microemu.cldc.btspp;

import org.microemu.microedition.io.ConnectionImplementation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import java.lang.reflect.Method;
import android.os.ParcelUuid;

import android.os.Build;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Connection implements ConnectionImplementation, StreamConnectionNotifier, StreamConnection {
	private BluetoothServerSocket serverSocket = null;
	public BluetoothSocket socket = null;
	public javax.bluetooth.UUID connUuid = null;

	// Android closes socket when one of streams is closed, we need to workaround it
	// Also if we're connecting with SPP profile the data we write is returned back to InputStream
	// This is not an expected behavior
	private class BTInputStream extends InputStream {
		private InputStream is;

		public BTInputStream(InputStream is) {
			this.is = is;
		}

		public int available() throws IOException {
			return is.available();
		}

		public void close() throws IOException {
			// application may call Connection.close(), which closes socket
			// So do nothing
		}

		public void mark(int readlimit) {
			is.mark(readlimit);
		}

		public boolean markSupported() {
			return is.markSupported();
		}

		public int read() throws IOException {
			return is.read();
		}

		public int read(byte[] b) throws IOException {
			return is.read(b);
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return is.read(b, off, len);
		}

		public void reset() throws IOException {
			is.reset();
		}

		public long skip(long n) throws IOException {
			return is.skip(n);
		}
	}

	private class BTOutputStream extends OutputStream {
		private OutputStream os;
		public InputStream is;

		public BTOutputStream(OutputStream os, InputStream is) {
			this.os = os;
			this.is = is;
		}

		public void close() throws IOException {
			// same as above
			os.flush();
		}

		public void flush() throws IOException {
			os.flush();
		}

		public void write(byte[] b) throws IOException {
			os.write(b);
			if (is != null)
				is.skip(b.length);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			os.write(b, off, len);
			if (is != null)
				is.skip(len);
		}

		public void write(int b) throws IOException {
			os.write(b);
			if (is != null)
				is.skip(1);
		}
	}

	private BTInputStream btin = null;
	private BTOutputStream btout = null;
	boolean skipAfterWrite = false;

	public javax.microedition.io.Connection openConnection(String name, int mode, boolean timeouts) throws IOException {
		if (name == null)
			throw new IllegalArgumentException("URL is null");
		System.out.println("***** Connection URL: " + name);

		int port = -1;
		int portSepIndex = name.lastIndexOf(':');
		if (portSepIndex == -1) {
			throw new IllegalArgumentException("Port missing");
		}
		String host = name.substring("btspp://".length(), portSepIndex);

		int argsStart = name.indexOf(";");
		String[] args = name.substring(argsStart + 1).split(";");
		boolean authenticate = false, encrypt = false, secure;
		String srvname = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("authenticate="))
				authenticate = Boolean.parseBoolean(args[i].substring(args[i].indexOf("=") + 1));
			if (args[i].startsWith("encrypt="))
				encrypt = Boolean.parseBoolean(args[i].substring(args[i].indexOf("=") + 1));
			if (args[i].startsWith("name="))
				srvname = args[i].substring(args[i].indexOf("=") + 1);
			if (args[i].startsWith("skipAfterWrite="))
				skipAfterWrite = Boolean.parseBoolean(args[i].substring(args[i].indexOf("=") + 1));
		}
		secure = authenticate && encrypt;

		String uuid = name.substring(portSepIndex + 1, argsStart);
		connUuid = new javax.bluetooth.UUID(uuid, false);
		java.util.UUID btUuid = connUuid.uuid;

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter.isDiscovering())
			adapter.cancelDiscovery();
		// java.util.UUID btUuid = getJavaUUID(uuid);
		// "localhost" indicates that we are acting as server
		if (host.equals("localhost")) {
			// btUuid = new javax.bluetooth.UUID(0x1101).uuid;

			// Android 6.0.1 bug: UUID is reversed
			// see https://issuetracker.google.com/issues/37075233
			if (secure)
				serverSocket = adapter.listenUsingRfcommWithServiceRecord(srvname, btUuid);
			else
				serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(srvname, btUuid);

		} else {
			StringBuilder sb = new StringBuilder(host);
			for (int i = 2; i < sb.length(); i += 3)
				sb.insert(i, ':');
			String addr = sb.toString();
 
			BluetoothDevice dev = adapter.getRemoteDevice(addr);
			if (secure)
				socket = dev.createRfcommSocketToServiceRecord(btUuid);
			else
				socket = dev.createInsecureRfcommSocketToServiceRecord(btUuid);

			try {
				socket.connect();
			} catch (IOException e) {
			}
		}
		return this;
	}

	public StreamConnection acceptAndOpen() throws IOException {
		if (serverSocket != null) {
			socket = serverSocket.accept();
			serverSocket.close();
			serverSocket = null;
		}
		/*if (socket != null)
			socket.connect();*/
		return this;
	}

	public void close() throws IOException {
		if (serverSocket != null)
			serverSocket.close();
		if (socket != null)
			socket.close();
	}

	public InputStream openInputStream() throws IOException {
		if (btin != null)
			return btin;
		if (socket != null)
			return btin = new BTInputStream(socket.getInputStream());
		throw new IOException("socket is null");
	}

	public DataInputStream openDataInputStream() throws IOException {
		if (socket != null)
			return new DataInputStream(openInputStream());
		throw new IOException("socket is null");
	}

	public OutputStream openOutputStream() throws IOException {
		if (btout != null)
			return btout;
		if (socket != null)
			return btout = new BTOutputStream(socket.getOutputStream(), skipAfterWrite ? socket.getInputStream() : null);
		throw new IOException("socket is null");
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		if (socket != null)
			return new DataOutputStream(openOutputStream());
		throw new IOException("socket is null");
	}
}
