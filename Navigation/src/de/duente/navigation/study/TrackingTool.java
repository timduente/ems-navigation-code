package de.duente.navigation.study;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import com.example.navigation.R;
import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Für die Studien trackt die Positionen des Benutzers.
 * 
 * @author Tim Dünte
 * 
 */
public class TrackingTool extends Activity {

	private boolean trackingEnabled = false;
	private File file;
	private FileWriter fileWriter;

	private Spinner spinner;
	private TrackingView trackView;
	private TextView courseID;

	private final static int ITERATION_COUNT = 5;
	private final static int DIFFERENT_LEVEL_COUNT = 7;
	private final static int PARTICIPANT_COUNT = 30;
	private ArrayList<Level> levels = new ArrayList<Level>();

	private final static int TIME_AFTER_SIGNAL_END = 3000;
	private final static int TIME_SIGNAL = 3000;
	private final static String FILE_HEADER = "ParticipantID;Counter;IntensitaetsID;x;y;z;FrameID;Latency???;Sendedatum;Ankunftsdatum;Signal an;\n";

	private InputStream in;
	private boolean connected = false;
	private int signalStarted = 0;
	private int counter = 0;

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
				changeFile();
				counter = 0;
				updateView();
			}
		});

		trackView = (TrackingView) findViewById(R.id.trackingView1);
		courseID = (TextView) findViewById(R.id.courseID);

		// Tracking Informationsempfaenger starten
		new UDPReceiver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

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
				// System.out.println(levels.get(i * ITERATION_COUNT + j));
			}
		}
		// Reihenfolge der Intensitäten randomisieren
		Collections.shuffle(levels);

		bluetoothConnector = new BluetoothConnector(mac, adapter);
		commandManager = new CommandManager(bluetoothConnector);
		try {
			bluetoothConnector.connectBluetooth();
			connected = true;
			in = bluetoothConnector.getIn();
			new BluetoothReceiver().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(commandManager).start();
		updateView();
		changeFile();
	}

	private void updateView() {
		courseID.setText(levels.get(counter).toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tracking_tool, menu);
		return true;
	}

	private void changeFile() {
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

		if (!file.exists()||file.length() <= FILE_HEADER.length()) {
			try {
				fileWriter = new FileWriter(file);
				fileWriter
						.append(FILE_HEADER);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if(spinner.getSelectedItemPosition() < state.length ){
				spinner.setSelection(spinner.getSelectedItemPosition() + 1);
			}
		}
	}

	public void startTracking(View view) {
		trackView.clear();
		trackingEnabled = true;
		view.setEnabled(false);
		if (counter < levels.size()) {
			findViewById(R.id.startSignal).setEnabled(true);
		}
	}

	public void startSignal(View view) {
		signalStarted = 1;
		CommandManager.setIntensityForTime(levels.get(counter).channel,
				levels.get(counter).intensity, TIME_SIGNAL);
		findViewById(R.id.startSignal).setEnabled(false);
	}

	public void stopSignal() {
		signalStarted = 0;
		updateView();
	}

	public void stopTracking(View view) {
		trackingEnabled = false;
		counter++;
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

	@Override
	public void onPause() {
		connected = false;
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	/**
	 * Checks if external storage is available for read and write
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
						String erg = new String();
						char c;
						while ((c = (char) in.read()) != ';') {
							erg = erg + c;
						}
						if (erg.length() > 0) {
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
				if (updates[0].equals("STOP")) {
					stopSignal();
					handler.postDelayed(runnable, TIME_AFTER_SIGNAL_END);
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
			} catch (SocketException soe) {
				soe.printStackTrace();
				socket = null;
				run = false;
			}
			SntpClient client = new SntpClient();
			CharSequence deviceTime;
			String values;
			byte[] buffer = new byte[1000];
			DatagramPacket packet;
			while (run) {
				try {
					packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					values = new String(buffer).trim();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					values = "ERROR;";
					e.printStackTrace();
				}
				deviceTime = "##TIMEOUT##;";
				if (client.requestTime("192.168.3.9", 500)) {
					long now = client.getNtpTime()
							+ SystemClock.elapsedRealtime()
							- client.getNtpTimeReference();
					deviceTime = DateFormat.format("dd-MM-yyyy hh:mm:ss:", now)
							+ String.format("%03d;", now % 1000);
				}
				publishProgress(values, deviceTime.toString());

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			// progress[0] String vom Motive mit Daten
			// progress[1] Zeit an dem das Paket angekommen ist.
			((TextView) findViewById(R.id.textView1)).setText(progress[0] + ""
					+ progress[1]);

			if (trackingEnabled) {
				String[] values = progress[0].split(";");
				try {
					fileWriter.append(spinner.getSelectedItem().toString()
							+ ";" + counter + ";" + levels.get(counter) + ";" + progress[0]
							+ progress[1] + "" + signalStarted + ";\n");
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