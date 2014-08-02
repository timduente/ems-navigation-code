package de.duente.navigation.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnector {
	private OutputStream out;
	private InputStream in;
	private String mac;

	// UUID fuer die Serielle Verbindung
	private final UUID SERIAL_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Bluetoothvariablen
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;
	private BluetoothDevice device;

	public BluetoothConnector(String mac, BluetoothAdapter adapter) {
		this.mac = mac;
		this.adapter = adapter;
	}

	public boolean isConnected() {
		if (socket == null) {
			return false;
		} else if (!socket.isConnected()) {
			return false;
		}
		try {
			sendText(";");
			return true;
		} catch (IOException ioEx) {
			return false;
		}
	}

	/**
	 * @return the in
	 */
	public InputStream getIn() {
		return in;
	}

	/**
	 * 
	 * Stellt eine Verbindung zum Bluetoothchip her.
	 */
	public void connectBluetooth() throws IOException {
		// Liefert das Remoteger�t mit der folgenden MAC-Adresse unter der
		// Annahme, das beide schon gekoppelt sind.
		device = adapter.getRemoteDevice(mac);

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
		socket.connect();
		// Streams werden initialisiert
		out = socket.getOutputStream();
		in = socket.getInputStream();
	}

	/**
	 * Trennt die Verbindung zum Bluetoothchip
	 */
	public void disconnectBluetooth() throws IOException {
		if (out != null)
			out.close();
		if (socket != null)
			socket.close();

	}

	/**
	 * Sendet den Text �ber die Bluetoothverbindung
	 * 
	 * @param text
	 *            String der gesendet werden soll.
	 */
	public void sendText(String text) throws IOException {
		if (!socket.isConnected()) {
			System.err.println("Fehler keine Verbindung");
			return;
		}
		if (out != null) {
			// Der Text wird in Bytes umgewandelt, die dann
			// versendet werden.
			byte[] sendbytes = text.getBytes();
			out.write(sendbytes);
			out.flush();

			// System.out.println("L�nge: " + sendbytes.length);
			// out.write(text.getText().toString().getBytes());
			// System.out.println(text);
		}

	}

}
