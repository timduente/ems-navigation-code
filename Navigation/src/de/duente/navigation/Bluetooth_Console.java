package de.duente.navigation;

import java.io.IOException;
import java.io.InputStream;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Bluetooth_Console extends Activity {
	// Restore STATE
	private final static String CONNECTION_STATUS = "Connection_Status";
	private final static String SEND_TEXT = "Send_Text";
	private final static String CONSOLE_TEXT = "Console_Text";
	// END Restore STATE
	private boolean oldState = false;

	// Verbindungsstatus
	private boolean connected = false;

	// Streams für die Kommunikation
	private InputStream in;

	private final static int REQUEST_ENABLE_BT = 1;

	// Mac Adresse des RN42 Bluetoothchips
	private final String mac = "00:06:66:67:3E:4B";

	private BluetoothConnector bluetoothConnector;
	private CommandManager commandManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_console);

		// Pruefen ob ein Bluetoothadapter vorhanden ist.
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			// Device does not support Bluetooth
			finish(); // exit
		}

		// Bluetoothadapter einschalten, falls er ausgeschaltet ist.
		if (!adapter.isEnabled()) {
			// make sure the device's bluetooth is enabled
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
		}

		bluetoothConnector = new BluetoothConnector(mac, adapter);
		commandManager = new CommandManager(bluetoothConnector);
		new Thread(commandManager).start();
	}

	/**
	 * Sendet die Eingabe aus dem Textfeld über Bluetooth.
	 * 
	 * @param view
	 */
	public void sendText(View view) {
		EditText text = (EditText) findViewById(R.id.bluetooth_send_text);
		try {
			bluetoothConnector.sendText(text.getText().toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Setzt die Buttons für Verbunden und getrennt
	 */
	private void setConnected(boolean connected) {
		this.connected = connected;
		Button disconnectBluetooth = (Button) findViewById(R.id.bluetoothDisconnectButton);
		disconnectBluetooth.setEnabled(connected);
		Button connectBluetooth = (Button) findViewById(R.id.bluetoothConnectButton);
		connectBluetooth.setEnabled(!connected);
	}

	/**
	 * 
	 * Stellt eine Verbindung zum Bluetoothchip her.
	 */
	public void connectBluetooth(View view) {
		try {
			bluetoothConnector.connectBluetooth();
			in = bluetoothConnector.getIn();
			setConnected(true);
			new BluetoothReceiver().execute("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Trennt die Verbindung zum Bluetoothchip
	 */
	public void disconnectBluetooth(View view) {
		try {
			bluetoothConnector.disconnectBluetooth();
			setConnected(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sendet 3 Pulse
	 */
	public void sendThreePulses(View view) {
		long startTime = System.currentTimeMillis();
		CommandManager.setPulseForTime(0, 30, startTime, 3, 1000, 1000, 0);
		
//		CommandToActions.setIntensityForTime(0, 50, startTime, 1000);
//		//CommandToActions.setIntensityForTime(0, 0, startTime + 1000, 1000);
//		CommandToActions.setIntensityForTime(0, 50, startTime + 2000, 1000);
//		//CommandToActions.setIntensityForTime(0, 0, startTime + 3000, 1000);
//		CommandToActions.setIntensityForTime(0, 50, startTime + 4000, 1000);
//		//CommandToActions.setIntensityForTime(0, 0, startTime + 5000, 1000);
		
		CommandManager.setPulseForTime(1, 127, startTime + 6000, 5, 1000, 2000, 0);
	}

	@Override
	public void onPause() {
		if (connected) {
			oldState = true;
		}
		disconnectBluetooth(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (oldState) {
			connectBluetooth(null);
			oldState = false;
		}
	}

	/**
	 * Sichert die Eingaben in dem Textfeld und dem Eingabetext. Sichert den
	 * Verbindungszustand
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(CONNECTION_STATUS, oldState);

		EditText text = (EditText) findViewById(R.id.bluetooth_send_text);
		outState.putString(SEND_TEXT, text.getText().toString());

		TextView textView = (TextView) findViewById(R.id.receiveText);
		outState.putString(CONSOLE_TEXT, textView.getText().toString());
	}

	/**
	 * Stellt die gesicherten Zustände wieder her.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Alten Stand widerherstellen
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(CONNECTION_STATUS)) {
				this.connectBluetooth(null);
			}
			EditText text = (EditText) findViewById(R.id.bluetooth_send_text);
			text.setText(savedInstanceState.getString(SEND_TEXT));

			TextView textView = (TextView) findViewById(R.id.receiveText);
			textView.setText(savedInstanceState.getString(CONSOLE_TEXT));
		}
		if (connected) {
			Button connectBluetooth = (Button) findViewById(R.id.bluetoothConnectButton);
			connectBluetooth.setEnabled(false);
			Button disconnectBluetooth = (Button) findViewById(R.id.bluetoothConnectButton);
			disconnectBluetooth.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth__console, menu);
		return true;
	}

	/**
	 * Empfängt und verarbeitet die Daten, die über die Bluetoothverbindung zum
	 * Smartphone gesendet werden.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class BluetoothReceiver extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			while (connected) {
				if (in != null) {
					try {
						int readablebytes = in.available();
						String erg = new String();
						for (int i = 0; i < readablebytes; i++) {
							char c = (char) in.read();
							erg = erg + c;
						}
						if (readablebytes > 0) {
							System.out.println("ERGEBNIS:" + erg);
							this.publishProgress(erg);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException io) {
					io.printStackTrace();
				}
			}
			return null;
		}

		/**
		 * Erlaubt Zwischenergebnisse dem UI-Thread zuzuführen. Hier werden die
		 * erhaltenen Daten in das Textfeld geschrieben.
		 */
		@Override
		protected void onProgressUpdate(String... updates) {
			TextView text = (TextView) findViewById(R.id.receiveText);
			if (updates.length > 0) {
				text.append(updates[0]);
			}
		}
	}
}