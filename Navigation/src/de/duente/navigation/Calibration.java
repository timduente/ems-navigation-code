package de.duente.navigation;

import java.io.IOException;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Stellt ein Interface und Funktionen zur Verfuegung um das
 * EMS-Navigationssystem zu kalibrieren.
 * 
 * @author Tim Dünte
 * 
 */
public class Calibration extends Activity {
	public static final String CALIBRATION_VALUES = "calibrationValues";
	public static final String BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY = "Bluetooth is managed by the activity, which starts this Activity";
	
private static final float STEPS_IN_PERCENT = 4.0f;

	// Floatingpoint insgesammt 6 Stellen 0 Nachkomma stellen mit Prozentzeichen
	// da hinter.
	private static final String NUMBER_FORMAT = "%6.0f%%";

	private String[] state = { "0", "1" };
	private float[] calibrationSettings = new float[state.length];

	float shownIntensity = 0.0f;
	int times = 0;

	// GUI Elemente
	private Spinner spinner;
	private TextView textCurrentIntensityInPercent;
	private TextView textLastIntensityPercent;

	// Handler und dazugehöriges Runnable
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			shownIntensity = shownIntensity + STEPS_IN_PERCENT;
			
			textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT,
					shownIntensity));
			String selectedChannel = (String) spinner.getSelectedItem();
			CommandManager.setIntensityForTime(
					Integer.parseInt(selectedChannel),(int) shownIntensity, 500);

			if (shownIntensity < 100.0f) {
				handler.postDelayed(this, 1000);
			}else{
				stopCalibration(null);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);

		// Die alten Kalibrierungswerte koennen uebergeben werden. Damit wird
		// auch
		// die Anzahl der Kanäle uebergeben.
		Intent startIntent = getIntent();
		if (startIntent.hasExtra(CALIBRATION_VALUES)) {
			calibrationSettings = startIntent
					.getFloatArrayExtra(Calibration.CALIBRATION_VALUES);

			state = new String[calibrationSettings.length];
			for (int i = 0; i < state.length; i++) {
				state[i] = i + "";
			}
		}

		spinner = (Spinner) findViewById(R.id.spinnerChannel);
		textCurrentIntensityInPercent = (TextView) findViewById(R.id.textIntensityPercent);
		textLastIntensityPercent = (TextView) findViewById(R.id.textLastIntensityPercent);

		textCurrentIntensityInPercent.setText(String
				.format(NUMBER_FORMAT, 0.0f));
		textLastIntensityPercent.setText(String.format(NUMBER_FORMAT, 0.0f));

		ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, state);

		spinner.setAdapter(adapter_state);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				String selectedChannel = (String) spinner.getSelectedItem();
				textCurrentIntensityInPercent.setText(String.format(
						NUMBER_FORMAT, 0.0f));
				textLastIntensityPercent.setText(String.format(NUMBER_FORMAT,
						calibrationSettings[Integer.parseInt(selectedChannel)]));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// nichts passiert.
			}
		});

		// Wenn die Aktivitaet alleine gestartet wird muss sie sich selbst um
		// eine Bluetoothverbindung kümmern. Ansonsten ist diese schon in der
		// anderen Activity initialisiert. TODO: Loeschen
		if (!startIntent.getBooleanExtra(
				BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY, false)) {

			final int REQUEST_ENABLE_BT = 1;
			// Mac Adresse des RN42 Bluetoothchips
			final String mac = "00:06:66:67:3E:4B";

			BluetoothConnector bluetoothConnector;
			CommandManager commandManager;

			// Pruefen ob ein Bluetoothadapter vorhanden ist.
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter == null) {
				// Device does not support Bluetooth
				finish(); // exit
			}

			// Bluetoothadapter einschalten, falls er ausgeschaltet ist.
			if (adapter != null && !adapter.isEnabled()) {
				// make sure the device's bluetooth is enabled
				Intent enableBluetooth = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
			}

			bluetoothConnector = new BluetoothConnector(mac, adapter);
			commandManager = new CommandManager(bluetoothConnector);
			new Thread(commandManager).start();

			try {
				bluetoothConnector.connectBluetooth();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calibration, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		// Ergebnisse der Kalibrierung werden in einem Intent übergeben.
		Intent resultIntent = new Intent();
		resultIntent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		setResult(RESULT_OK, resultIntent);
		super.onBackPressed();
	}

	/**
	 * Stoppt das Steigern der Intensitaet. Die zuletzt angezeigte Intensitaet
	 * wird gesichert.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void stopCalibration(View view) {
		handler.removeCallbacks(runnable);
		spinner.setEnabled(true);
		findViewById(R.id.buttonStartCalibration).setEnabled(true);

		String s = (String) spinner.getSelectedItem();
		int channelNumber = Integer.parseInt(s);
		calibrationSettings[channelNumber] = shownIntensity;
		textLastIntensityPercent.setText(String.format(NUMBER_FORMAT,
				shownIntensity));
		
		CommandManager.setMaxIntensityForChannel(channelNumber, (int)shownIntensity);
	}

	/**
	 * Startet das Steigern der Intensitaet. Laeuft bis 100% oder bis Stopp
	 * gedrueckt wurde.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void startCalibration(View view) {
		String s = (String) spinner.getSelectedItem();
		int channelNumber = Integer.parseInt(s);
		CommandManager.setMaxIntensityForChannel(channelNumber, 100);
		
		shownIntensity = 0.0f;
		times = 0;
		handler.postDelayed(runnable, 100);
		spinner.setEnabled(false);
		view.setEnabled(false);

	}

	/**
	 * Aktiviert den Kanal fuer eine Sekunde mit der zuletzt kalibrierten
	 * Intensitaet.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void testIntensity(View view) {
		String s = (String) spinner.getSelectedItem();
		int channel = Integer.parseInt(s);
		CommandManager.setIntensityForTime(channel,
				100, 1000);
	}

}