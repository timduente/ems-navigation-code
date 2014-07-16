package de.duente.navigation.study;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import com.example.navigation.R;
import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.Button;
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
	private TextView textLeft;
	private TextView textRight;
	private TextView textCounter;
	private Button startSignal;

	private final static int ITERATION_COUNT = 5;
	private final static int DIFFERENT_LEVEL_COUNT = 8;
	private final static int PARTICIPANT_COUNT = 30;
	private ArrayList<Level> levels = new ArrayList<Level>();

	private final static int TIME_AFTER_SIGNAL_END = 3000;
	private final static int TIME_SIGNAL = 3000;
	private final static String FILE_HEADER = "Teilnehmer ID;Zaehler;Intensitaets ID;X;Y;Z;Frame ID;Pitch;Sendedatum;Ankunftsdatum;Signal an;Datum Signal an;Datum Signal aus;\n";
	private final static String START = "START";
	private final static String STOP = "STOP";
	private final static String MOTIVE_SERVER_IP = "192.168.3.9";
	private final static String TRACKING_DATEFORMAT = "dd-MM-yyyy kk:mm:ss:";

	private InputStream in;
	private boolean connected = false;
	private int signalStarted = 0;
	private int counter = 0;
	private long signalStartTime = 0;
	private long signalStopTime = 0;

	private long signalStartTimeMillis;
	private long signalStopTimeMillis;
	private RandomLatinSquare randomLatinSquare = new RandomLatinSquare(DIFFERENT_LEVEL_COUNT);

	private String[] state = new String[PARTICIPANT_COUNT];

	Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			stopTracking(null);
		}
	};

	Handler soundHandler = new Handler();
	private Runnable soundRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Uri notification = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				Ringtone r = RingtoneManager.getRingtone(
						getApplicationContext(), notification);
				r.play();
				Thread.sleep(1200);
				r.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
				signalStartTime = 0;
				signalStopTime = 0;
				initialiazeRandomList();
				changeFile();
				counter = 0;
				updateView();
			}
		});

		trackView = (TrackingView) findViewById(R.id.trackingView1);
		courseID = (TextView) findViewById(R.id.courseID);
		textLeft = (TextView) findViewById(R.id.textLeft);
		textRight = (TextView) findViewById(R.id.textRight);
		textCounter = (TextView) findViewById(R.id.textCount);
		startSignal = (Button) findViewById(R.id.startSignal);

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
		
		initialiazeRandomList();

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
	}
	
	private void initialiazeRandomList(){
		int[] randomizedNumbers = randomLatinSquare.randomize(10);
		for (int j = 0; j < ITERATION_COUNT; j++) {
			for (int i = 0; i < DIFFERENT_LEVEL_COUNT; i++) {
				if(j%2 == 0)
					levels.add(new Level(randomizedNumbers[j*DIFFERENT_LEVEL_COUNT + i]));
				else{
					levels.add(new Level(randomizedNumbers[j*DIFFERENT_LEVEL_COUNT +DIFFERENT_LEVEL_COUNT- 1-  i]));
				}
				System.out.println(levels.get(i * ITERATION_COUNT + j));
			}
		}
	}

	private void updateView() {
		if (counter >= levels.size()) {
			spinner.setSelection(spinner.getSelectedItemPosition());
		} else {
			textCounter.setText("" + counter);
			courseID.setText(levels.get(counter).toString());
			if (levels.get(counter).channel == 0) {
				textLeft.setVisibility(TextView.VISIBLE);
				textRight.setVisibility(TextView.INVISIBLE);
			} else {
				textLeft.setVisibility(TextView.INVISIBLE);
				textRight.setVisibility(TextView.VISIBLE);
			}
		}

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

		if (!file.exists() || file.length() <= FILE_HEADER.length()) {
			try {
				fileWriter = new FileWriter(file);
				fileWriter.append(FILE_HEADER);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (spinner.getSelectedItemPosition() < state.length) {
				spinner.setSelection(spinner.getSelectedItemPosition() + 1);
			}
		}
	}

	/**
	 * Startet die Trackingaufnahme. Alle Positionsdaten werden getrackt.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wird.
	 */
	public void startTracking(View view) {
		if (counter >= levels.size()) {
			return;
		}
		trackView.clear();
		trackingEnabled = true;
		view.setEnabled(false);
		if (counter < levels.size()) {
			startSignal.setEnabled(true);
		}
	}

	/**
	 * Startet das EMS-Signal. Dieses wird übertragen. Darf erst nach dem
	 * Starten des Trackings aufgerufen werden.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wird.
	 */
	public void startSignal(View view) {
		signalStarted = 1;

		CommandManager.setIntensityForTime(levels.get(counter).channel,
				levels.get(counter).intensity, TIME_SIGNAL);

		signalStartTimeMillis = System.currentTimeMillis();

		new TimeReceiver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				START);

		startSignal.setEnabled(false);
	}

	private void stopSignal() {
		signalStarted = 0;
		updateView();
	}

	/**
	 * Beendet das Tracking
	 * 
	 * @param view
	 *            View Objekt, dass diese Methode aufruft.
	 */
	public void stopTracking(View view) {
		trackingEnabled = false;
		counter++;
		findViewById(R.id.startTracking).setEnabled(true);
		this.updateView();
		signalStopTime = 0;
		try {
			if (fileWriter != null) {
				fileWriter.append("\n");
			}
		} catch (IOException ioEx) {
			// TODO: Auto Catch
			ioEx.printStackTrace();
		}
		if (counter > levels.size()) {
			soundHandler.post(soundRunnable);
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
	 * Diese Klasse bekommt den Zeitwert von einem Zeitserver.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class TimeReceiver extends AsyncTask<String, String, String> {
		private final static int TIMEOUT_MILLIS = 500;

		@Override
		protected String doInBackground(String... params) {
			if (params.length == 1) {
				SntpClient client = new SntpClient();
				long sendTimeMillis = System.currentTimeMillis();
				long now = 0;
				if (client.requestTime(MOTIVE_SERVER_IP, TIMEOUT_MILLIS)) {
					now = client.getNtpTime() + SystemClock.elapsedRealtime()
							- client.getNtpTimeReference();
				}

				if (params[0].equals(START)) {
					signalStartTime = now
							- (sendTimeMillis - signalStartTimeMillis);
				} else if (params[0].equals(STOP)) {
					signalStopTime = now
							- (sendTimeMillis - signalStopTimeMillis);
				}

			}
			return null;
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

		@Override
		protected String doInBackground(String... arg0) {
			while (connected) {
				if (in != null) {
					try {
						if (in.available() > 0) {
							String erg = new String();
							char c;
							while ((c = (char) in.read()) != ';') {
								erg = erg + c;
							}
							if (erg.length() > 0) {
								System.out.println("ERGEBNIS:" + erg);
								this.publishProgress(erg);
							}
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
				if (updates[0].equals(STOP)) {
					signalStopTimeMillis = System.currentTimeMillis();
					new TimeReceiver().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, STOP);
					stopSignal();
					handler.postDelayed(runnable, TIME_AFTER_SIGNAL_END);
				}
			}
		}
	}

	/**
	 * Empfängt die UDP-Pakete in denen sich die Tracking Informationen
	 * befinden. Fragt nach dem Empfangen die Zeit beim Zeitserver nach.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class UDPReceiver extends AsyncTask<String, String, String> {
		private final static int LISTENING_PORT = 9045;
		private final static int PACKET_SIZE = 96;
		private final static int TIMEOUT_MILLIS = 250;
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
			String deviceTime;

			DatagramPacket packet;
			byte[] buffer = new byte[PACKET_SIZE];
			String values;

			while (run) {
				try {
					packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					values = new String(buffer).trim();
				} catch (IOException e) {
					values = "ERROR;";
					e.printStackTrace();
				}
				deviceTime = "##TIMEOUT##;";
				if (client.requestTime(MOTIVE_SERVER_IP, TIMEOUT_MILLIS)) {
					long now = client.getNtpTime()
							+ SystemClock.elapsedRealtime()
							- client.getNtpTimeReference();
					deviceTime = DateFormat.format(TRACKING_DATEFORMAT, now)
							+ String.format("%03d;", now % 1000);
				}
				publishProgress(values, deviceTime);

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
					String startTimeSignal = DateFormat.format(
							TRACKING_DATEFORMAT, signalStartTime)
							+ String.format("%03d;", signalStartTime % 1000);
					String stopTimeSignal = DateFormat.format(
							TRACKING_DATEFORMAT, signalStopTime)
							+ String.format("%03d;", signalStopTime % 1000);

					String[] split = progress[0].split(";");
					String dateOfPictureInMillis = split[split.length - 1];

					fileWriter
							.append(spinner.getSelectedItem().toString()
									+ ";"
									+ counter
									+ ";"
									+ levels.get(counter)
									+ ";"
									+ progress[0]
									+ progress[1]
									+ ((dateOfPictureInMillis
											.compareTo(startTimeSignal) >= 0) ? signalStarted
											: 0) + ";" + startTimeSignal
									+ stopTimeSignal + "\n");

					trackView.setSignalActive(((dateOfPictureInMillis
							.compareTo(startTimeSignal) >= 0) ? signalStarted
							: 0) == 1);
					trackView.setCoordinateToDraw(Float.parseFloat(values[0]),
							Float.parseFloat(values[1]),
							Float.parseFloat(values[2]));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}