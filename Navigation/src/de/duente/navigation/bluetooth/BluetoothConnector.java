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
	private boolean connected;
	
	public boolean isConnected(){
		return connected;
	}
	
	/**
	 * @return the in
	 */
	public InputStream getIn() {
		return in;
	}

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
		connected = false;
	}

	/**
	 * 
	 * Stellt eine Verbindung zum Bluetoothchip her.
	 */
	public void connectBluetooth() throws IOException {
		// Liefert das Remotegerät mit der folgenden MAC-Adresse unter der
		// Annahme, das beide schon gekoppelt sind.
		device = adapter.getRemoteDevice(mac);

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
		socket.connect();
		// Streams werden initialisiert
		out = socket.getOutputStream();
		 in = socket.getInputStream();
		// Ist zuständig für das Empfangen eingehender Daten.
		// TODO: new BluetoothReceiver().execute("");
		 connected = true;
	}

	/**
	 * Trennt die Verbindung zum Bluetoothchip
	 */
	public void disconnectBluetooth() throws IOException{
		connected = false;
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
			
	}

	/**
	 * Sendet den Text über die Bluetoothverbindung
	 * 
	 * @param text
	 *            String der gesendet werden soll.
	 */
	public void sendText(String text) throws IOException{
		if(!connected){
			System.err.println("Fehler keine Verbindung");
			return;
		}
		if (out != null) {		
				// Der Text wird in Bytes umgewandelt, die dann einzeln
				// versendet werden. So hoffe ich den Buffer nicht zu überfüllen
				// vom Arduino
				byte[] sendbytes = text.getBytes();
				out.write(sendbytes);
				out.flush();
//				for (int i = 0; i < sendbytes.length; i++) {
//					// System.out.println(sendbytes[i] + "");
//					// System.out.printf("b", sendbytes[i]);
//					out.write(sendbytes[i]);
//					out.flush();
//				}
				
				// System.out.println("Länge: " + sendbytes.length);
				// out.write(text.getText().toString().getBytes());
				//System.out.println(text);
		}
		
	}

}
