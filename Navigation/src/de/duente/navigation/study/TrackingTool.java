package de.duente.navigation.study;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import com.example.navigation.R;
import de.duente.navigation.Calibration;
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
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
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
	private File file;
	private FileWriter fileWriter;

	private Spinner spinner;
	private TrackingView trackView;
	private TextView courseID;
	private TextView textLeft;
	private TextView textRight;
	private TextView textMiddle;
	private TextView textCounter;
	private TextView connectionOn;
	private Button startSignal;
	private Button startTracking;

	private final static int PARTICIPANT_COUNT = 30;

	private final static int TIME_SIGNAL = 30000;
	private final static String FILE_HEADER = "Teilnehmer ID;Zaehler;Intensitaets ID;X;Y;Z;Frame ID;YAW Rotation um die Hoehenachse (Y);Sendedatum;Ankunftsdatum;Signal an;Datum Signal an;Datum Signal aus;";
	private final static String START = "START";
	private final static String STOP = "STOP";
	private final static String MOTIVE_SERVER_IP = "192.168.3.9";
	private final static String TRACKING_DATEFORMAT = "dd-MM-yyyy kk:mm:ss:";
	public static final int REQUEST_CODE_CALIBRATION = 212;
	private boolean calibrationStarted = false;

	private int signalStarted = 0;
	private long signalStartTime = 0;
	private long signalStopTime = 0;

	private long signalStartTimeMillis;
	private long signalStopTimeMillis;

	private String[] state = new String[PARTICIPANT_COUNT];

	private Participant participant;
	private AlertDialog alertParticipantNeedsBreak;
	private AlertDialog alertBluetoothConnectionFailed;

	private UDPReceiver udpReceiver;
	private BluetoothReceiver bluetoothReceiver;
	private BluetoothConnectionChecker bluetoothConnectionChecker;

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
				participant = new Participant(arg2);
				signalStartTime = 0;
				signalStopTime = 0;
				changeFile();
				updateView();

			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Pause");
		builder.setMessage("Pause machen. Teilnehmer Wasser und Kekse anbieten. Maske absetzen.");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});

		alertParticipantNeedsBreak = builder.create();

		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

		builder2.setTitle("Verbindung zum Arduino fehlgeschlagen!");
		builder2.setMessage("Verbindungsversuch erneut starten?");

		builder2.setPositiveButton("JA", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				reconnectBluetooth();
				dialog.dismiss();
			}

		});

		builder2.setNegativeButton("NEIN",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Code der ausgeführt wird wenn NEIN geklickt wurde
						dialog.dismiss();
					}

				});
		alertBluetoothConnectionFailed = builder2.create();

		trackView = (TrackingView) findViewById(R.id.trackingView1);
		courseID = (TextView) findViewById(R.id.courseID);
		textLeft = (TextView) findViewById(R.id.textLeft);
		textRight = (TextView) findViewById(R.id.textRight);
		textMiddle = (TextView) findViewById(R.id.textMiddle);
		textCounter = (TextView) findViewById(R.id.textCount);
		connectionOn = (TextView) findViewById(R.id.textConnectionOn);
		startSignal = (Button) findViewById(R.id.startSignal);
		startTracking = (Button) findViewById(R.id.startTracking);

		// Tracking Informationsempfaenger starten
		udpReceiver = new UDPReceiver();
		udpReceiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

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
	}

	private void updateConnectionStatus() {
		if (bluetoothConnector.isConnected()) {
			connectionOn.setBackgroundColor(Color.GREEN);
		} else {
			connectionOn.setBackgroundColor(Color.RED);
			alertBluetoothConnectionFailed.show();
		}
	}

	private void updateView() {

		if (!participant.isInitialized()) {
			textLeft.setVisibility(TextView.INVISIBLE);
			textRight.setVisibility(TextView.INVISIBLE);
			textMiddle.setVisibility(TextView.INVISIBLE);
			textCounter.setText("-1");
			courseID.setText("Kalibrieren");

		} else if (!participant.isStudyDone()) {
			textCounter.setText("" + participant.getActualLevelIndex());
			courseID.setText(participant.getActualLevel().toString());

			if (participant.getActualLevel().channel == 0) {
				textLeft.setVisibility(TextView.INVISIBLE);
				textRight.setVisibility(TextView.VISIBLE);
			} else {
				textLeft.setVisibility(TextView.VISIBLE);
				textRight.setVisibility(TextView.INVISIBLE);
			}
			
			if(participant.getActualLevel().isMiddle()){
				textMiddle.setVisibility(TextView.VISIBLE);
			}else{
				textMiddle.setVisibility(TextView.INVISIBLE);
			}

			if (!udpReceiver.trackingEnabled) {
				startTracking.setEnabled(true);
			}
		}
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

		if (!file.exists() || file.length() <= FILE_HEADER.length() + 1) {
			try {
				fileWriter = new FileWriter(file);
				writeLineToFile(FILE_HEADER);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			if (spinner.getSelectedItemPosition() < state.length - 1) {
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
		if (participant.isInitialized() && !participant.isStudyDone()) {
			trackView.clear();
			udpReceiver.trackingEnabled = true;
			view.setEnabled(false);
			startSignal.setEnabled(true);
			startTracking.setEnabled(false);
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
		if (participant.isInitialized() && !participant.isStudyDone()) {
			signalStarted = 1;

			CommandManager.setIntensityForTime(
					participant.getActualLevel().channel,
					participant.getActualLevel().intensity, TIME_SIGNAL);

			signalStartTimeMillis = System.currentTimeMillis();

			new TimeReceiver().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, START);

			startSignal.setEnabled(false);
		}
	}

	/**
	 * Beendet das EMS-Signal
	 * 
	 * @param view
	 */
	public void stopSignal(View view) {
		CommandManager.stopSignal(0);
		CommandManager.stopSignal(1);
		stopTracking(null);
	}

	private void stopSignal() {
		if (signalStarted == 0) {
			return;
		}
		CommandManager.stopSignal(0);
		CommandManager.stopSignal(1);
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
		boolean notDoubleStopHit = udpReceiver.trackingEnabled;
		udpReceiver.trackingEnabled = false;
		findViewById(R.id.startTracking).setEnabled(true);

		if (notDoubleStopHit) {
			participant.nextLevel();
		}
		this.updateView();
		signalStopTime = 0;
		startSignal.setEnabled(false);

		writeLineToFile("");

		if (participant.getActualLevelIndex()
				% Participant.DIFFERENT_LEVEL_COUNT == 0
				&& participant.getActualLevelIndex() > 0) {
			alertParticipantNeedsBreak.setTitle("Pause "
					+ participant.getActualLevelIndex()
					/ Participant.DIFFERENT_LEVEL_COUNT);
			alertParticipantNeedsBreak.show();
			writeLineToFile("");
		}

		if (participant.isInitialized() && participant.isStudyDone()) {
			soundHandler.post(soundRunnable);
		}

	}

	private void writeLineToFile(String dataToWrite) {
		try {
			if (fileWriter != null) {
				fileWriter.append(dataToWrite);
				fileWriter.append("\n");
			}
		} catch (IOException ioEx) {
			// TODO: Auto Catch
			ioEx.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		if (!calibrationStarted) {
			if (bluetoothReceiver != null) {
				bluetoothReceiver.connected = false;
			}
			try {
				bluetoothConnector.disconnectBluetooth();
				updateConnectionStatus();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onDestroy();
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
	public void onResume() {

		if (!calibrationStarted) {
			reconnectBluetooth();

		} else {
			this.calibrationStarted = false;
		}
		super.onResume();
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
	 * Startet den Kalibrierungsdialog
	 * 
	 * @param view
	 *            View-Objekt, dass diese Methode aufruft.
	 */
	public void calibrate(View view) {
		calibrationStarted = true;
		Intent intent = new Intent(this, CalibrationDialog.class);
		if (participant.isInitialized()) {
			intent.putExtra(CalibrationDialog.CALIBRATION_VALUES,
					participant.getIntensitys());
		}
		intent.putExtra(Calibration.BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY,
				true);
		startActivityForResult(intent, REQUEST_CODE_CALIBRATION);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CALIBRATION && resultCode == RESULT_OK
				&& data.hasExtra(Calibration.CALIBRATION_VALUES)) {
			participant.setCalibratedIntensitys(data
					.getIntArrayExtra(Calibration.CALIBRATION_VALUES));
			CalibrationSaver.writeCalibrationValues(data
					.getIntArrayExtra(Calibration.CALIBRATION_VALUES));

			updateView();

		}
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
					signalStopTimeMillis = System.currentTimeMillis();
					new TimeReceiver().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, STOP);
					stopSignal();
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
		private final static int PACKET_SIZE = 80;
		private final static int TIMEOUT_MILLIS = 250;
		boolean run = true;
		boolean trackingEnabled = false;

		// private float[] signalStartCords = new float[3];
		//
		// private double getAngle(float x1, float y1, float x2, float y2) {
		// return Math.atan2(y2 - y1, x2 - x1) * 180.0 / Math.PI;
		// }

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
			((TextView) findViewById(R.id.calibrationTitle))
					.setText(progress[0] + "" + progress[1]);

			if (trackingEnabled) {
				String[] values = progress[0].split(";");

				String startTimeSignal = DateFormat.format(TRACKING_DATEFORMAT,
						signalStartTime)
						+ String.format("%03d;", signalStartTime % 1000);
				String stopTimeSignal = DateFormat.format(TRACKING_DATEFORMAT,
						signalStopTime)
						+ String.format("%03d;", signalStopTime % 1000);

				String[] split = progress[0].split(";");
				String dateOfPictureInMillis = split[split.length - 1];

				boolean isSignalOnInCurrentPicture = (dateOfPictureInMillis
						.compareTo(startTimeSignal) >= 0);

				StringBuilder trackedData = new StringBuilder();
				trackedData.append(spinner.getSelectedItem().toString());
				trackedData.append(';');
				trackedData.append(participant.getActualLevelIndex());
				trackedData.append(';');
				trackedData.append(participant.getActualLevel());
				trackedData.append(';');
				trackedData.append(progress[0]);
				trackedData.append(progress[1]);
				trackedData.append((isSignalOnInCurrentPicture ? signalStarted
						: 0));
				trackedData.append(';');
				trackedData.append(startTimeSignal);
				trackedData.append(stopTimeSignal);

				writeLineToFile(trackedData.toString());

				trackView
						.setSignalActive(((isSignalOnInCurrentPicture) ? signalStarted
								: 0) == 1);

				trackView.setCoordinateToDraw(Float.parseFloat(values[0]),
						Float.parseFloat(values[1]),
						Float.parseFloat(values[2]));
			}
		}
	}
}