package de.duente.navigation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.navigation.R;

import de.duente.navigation.bluetooth.BluetoothConnector;
import de.duente.navigation.commands.CommandManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class EMSNavigation extends Activity implements OnTouchListener {

	private final static String STOP = "STOP";
	
	private Button buttonRightOn;
	private Button buttonLeftOn;
	private TextView connectionOn;
	private TextView signalOn;

	private AlertDialog alertBluetoothConnectionFailed;
	
	private BluetoothConnectionChecker bluetoothConnectionChecker;
	private BluetoothReceiver bluetoothReceiver;

	// Bluetoothverbindung:
	private final static int REQUEST_ENABLE_BT = 1;

	// Mac Adresse des RN42 Bluetoothchips
	private final String mac = "00:06:66:67:3E:4B";

	private BluetoothConnector bluetoothConnector;
	private CommandManager commandManager;
	private BluetoothAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emsnavigation);

		buttonRightOn = (Button) findViewById(R.id.buttonRightOn);
		buttonLeftOn = (Button) findViewById(R.id.buttonLeftOn);
		connectionOn = (TextView) findViewById(R.id.connectionState);
		signalOn = (TextView) findViewById(R.id.textSignalState);

		buttonRightOn.setOnTouchListener(this);
		buttonLeftOn.setOnTouchListener(this);

		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

		builder2.setTitle("Failed to connect to Arduino!");
		builder2.setMessage("Try to connect to Arduino again?");

		builder2.setPositiveButton("YES", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				reconnectBluetooth();
				dialog.dismiss();
			}
		});

		builder2.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Code der ausgeführt wird wenn NEIN geklickt wurde
						dialog.dismiss();
					}

				});
		alertBluetoothConnectionFailed = builder2.create();

		// Bluetooth initialisieren
		// Pruefen ob ein Bluetoothadapter vorhanden ist.
		adapter = BluetoothAdapter.getDefaultAdapter();

		// Bluetoothadapter einschalten, falls er ausgeschaltet ist.
		if (!adapter.isEnabled()) {
			// make sure the device's bluetooth is enabled
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
		}

		bluetoothConnector = new BluetoothConnector(mac, adapter);
		bluetoothConnectionChecker = new BluetoothConnectionChecker();
		bluetoothConnectionChecker.checking = true;
		bluetoothConnectionChecker.executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
		commandManager = new CommandManager(bluetoothConnector);
		new Thread(commandManager).start();
		
		updateSignalOnState();

	}

	public void updateConnectionStatus() {
		if (bluetoothConnector.isConnected()) {
			connectionOn.setBackgroundColor(Color.GREEN);
		} else {
			connectionOn.setBackgroundColor(Color.RED);
			alertBluetoothConnectionFailed.show();
		}
	}

	public void updateSignalOnState() {
		signalOn.setText("Signal OFF");
		signalOn.setBackgroundColor(Color.RED);
	}

	private void reconnectBluetooth() {
		try {
			bluetoothConnector.connectBluetooth();
			if (bluetoothReceiver != null) {
				bluetoothReceiver.connected = false;
			}
			bluetoothReceiver = new BluetoothReceiver();
			bluetoothReceiver.connected = true;
			bluetoothReceiver.setIn(bluetoothConnector.getIn());
			bluetoothReceiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					"");

		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("Creating BluetoothConnection failed!");
		}
		updateConnectionStatus();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == buttonRightOn) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				CommandManager.setIntensityForTime(1, 100, 60000);
				signalOn.setText("Signal ON");
				signalOn.setBackgroundColor(Color.GREEN);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				CommandManager.stopSignal(1);
			}
		} else if (v == buttonLeftOn) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				CommandManager.setIntensityForTime(0, 100, 60000);
				signalOn.setText("Signal ON");
				signalOn.setBackgroundColor(Color.GREEN);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				CommandManager.stopSignal(0);
			}
		}
		v.performClick();
		return false;
	}
	
	/**
	 * Diese Klasse überprüft jede Sekunde den Zustand des BluetoothConnectors
	 * indem sie updateConnectionStatus aufruft.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class BluetoothConnectionChecker extends
			AsyncTask<Void, Void, Void> {
		boolean checking = false;

		@Override
		protected Void doInBackground(Void... params) {
			while (checking) {
				this.publishProgress((Void[]) null);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
			updateConnectionStatus();
		}
	}
	
	/**
	 * Empfängt und verarbeitet die Daten, die über die Bluetoothverbindung zum
	 * Smartphone gesendet werden.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class BluetoothReceiver extends AsyncTask<String, String, String> {
		boolean connected = false;
		private InputStream in = null;
		private InputStreamReader inputReader;

		public void setIn(InputStream in) {
			this.in = in;
			inputReader = new InputStreamReader(in);
		}

		@Override
		protected String doInBackground(String... arg0) {

			while (connected) {
				if (inputReader != null) {
					try {
						if (inputReader.ready()) {
							StringBuilder messageFromArduino = new StringBuilder();
							char c;
							while ((c = (char) inputReader.read()) != ';') {
								messageFromArduino.append(c);
							}
							publishProgress(messageFromArduino.toString());
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				if (inputReader != null) {
					inputReader.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException io) {
				io.printStackTrace();
			}

			return null;
		}

		/**
		 * Erlaubt Zwischenergebnisse dem UI-Thread zuzuführen. Hier werden die
		 * erhaltenen Daten in das Textfeld geschrieben.
		 */
		@Override
		protected void onProgressUpdate(String... updates) {
			if (updates.length > 0) {
				if (updates[0].equals(STOP)) {
					updateSignalOnState();
				}
			}
		}
	}
}
