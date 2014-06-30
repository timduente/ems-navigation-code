package de.duente.navigation.study;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**Für die Studien trackt die Positionen des Benutzers.
 * 
 * @author Tim Dünte
 *
 */
public class TrackingTool extends Activity {

	private boolean trackingEnabled = false;
	private File file;
	private FileWriter fileWriter;
	private Spinner spinner;
	private int signalStarted = 0;
	private TrackingView trackView;

	private final static int ITERATION_COUNT = 5;
	private final static int DIFFERENT_LEVEL_COUNT = 7;
	private final static int PARTICIPANT_COUNT = 20;
	private int counter = 0;
	private ArrayList<Level> levels = new ArrayList<Level>();

	private InputStream in;
	private boolean connected = false;

	private String[] state = new String[PARTICIPANT_COUNT];
	
	Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			stopTracking(null);		
		}	
	};
	

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
		setContentView(R.layout.activity_tracking_tool);

		// Initialisierung Spinner
		for (int i = 0; i < state.length; i++) {
			state[i] = "" + i;
		}
		spinner = (Spinner) findViewById(R.id.spinnerParticipants);
		ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, state);
		spinner.setAdapter(adapter_state);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// nichts passiert.
			}

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Collections.shuffle(levels);
				for(Level l : levels){
					System.out.println(l);
				}
				counter = 0;
				findViewById(R.id.startSignal).setEnabled(true);
			}
		});

		trackView = (TrackingView) findViewById(R.id.trackingView1);

		// Tracking Informationsempfaenger starten
		new UDPReceiver().execute("");

		// Bluetooth initialisieren
		// Pruefen ob ein Bluetoothadapter vorhanden ist.
		adapter = BluetoothAdapter.getDefaultAdapter();
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

		for (int i = 0; i < DIFFERENT_LEVEL_COUNT; i++) {
			for (int j = 0; j < ITERATION_COUNT; j++) {
				levels.add(new Level(i));
				//System.out.println(levels.get(i * ITERATION_COUNT + j));
			}
		}

		Collections.shuffle(levels);
		for(Level l : levels){
			System.out.println(l);
		}

		bluetoothConnector = new BluetoothConnector(mac, adapter);
		commandManager = new CommandManager(bluetoothConnector);
		new Thread(commandManager).start();
		in = bluetoothConnector.getIn();
		connected = true;

		new BluetoothReceiver().execute("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tracking_tool, menu);
		return true;
	}

	public void startTracking(View view) {

		trackView.clear();
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!isExternalStorageWritable()) {
			System.out.println("Externer Speicher ist nicht verfügbar");
		}
		File path = Environment.getExternalStorageDirectory();
		File pathToLogs = new File(path, "logs");
		pathToLogs.mkdir();

		file = new File(pathToLogs, spinner.getSelectedItem().toString()
				+ ".csv");
		System.out.println(file.getAbsolutePath());
		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		trackingEnabled = true;
		view.setEnabled(false);
	}

	public void startSignal(View view) {
		signalStarted = 1;
		int signalTime = 3000;
		CommandManager.setIntensityForTime(levels.get(counter).channel,
				levels.get(counter).intensity, signalTime);
		counter++;
		if (counter > levels.size()) {
			findViewById(R.id.startSignal).setEnabled(false);
		}

	}

	public void stopSignal() {
		signalStarted = 0;
		findViewById(R.id.startSignal).setEnabled(true);
	}

	public void stopTracking(View view) {
		trackingEnabled = false;
		findViewById(R.id.startTracking).setEnabled(true);
		try {
			if (fileWriter != null) {
				fileWriter.append("\n");
			}
		} catch (IOException ioEx) {
			// TODO: Auto Catch
			ioEx.printStackTrace();
		}
	}

	/** Checks if external storage is available for read and write 
	 * 
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
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
					Thread.sleep(10);
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
			if (updates.length > 0) {
				if(updates[0].equals("STOP")){
					stopSignal();
					handler.postDelayed(runnable, 1000);
				}
			}
			
		}
	}

	/**
	 * Empfängt die UDP-Pakete in denen sich die Tracking Informationen
	 * befinden.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class UDPReceiver extends AsyncTask<String, String, String> {
		private final static int LISTENING_PORT = 9045;
		public boolean run = true;

		@Override
		protected String doInBackground(String... params) {
			DatagramSocket socket;
			try {
				socket = new DatagramSocket(LISTENING_PORT);
				while (run) {
					byte[] buffer = new byte[1000];
					DatagramPacket packet = new DatagramPacket(buffer,
							buffer.length);
					socket.receive(packet);
					String values = new String(buffer).trim();

					// System.out.println();
					publishProgress(values);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			((TextView) findViewById(R.id.textView1)).setText(progress[0]);

			if (trackingEnabled) {
				String[] values = progress[0].split(";");
				try {
					fileWriter.append(spinner.getSelectedItem().toString()
							+ ";" + progress[0] + "" + signalStarted + ";\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				trackView.setCoordinateToDraw(Float.parseFloat(values[0]),
						Float.parseFloat(values[1]),
						Float.parseFloat(values[2]));
			}
		}
	}
}