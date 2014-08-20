package de.duente.navigation.study;

import com.example.navigation.R;

import de.duente.navigation.commands.CommandManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Teil der Kalibrierung. Stellt ein Interface für die Maximalkalibrierung eines
 * Kanals bereit.
 * 
 * @author Tim Dünte
 * 
 */
public class CalibrationDialog extends Activity implements
		OnSeekBarChangeListener {

	public static final String CALIBRATION_VALUES = "calibrationValues";
	public static final String BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY = "Bluetooth is managed by the activity, which starts this Activity";
	public static final String CALIBRATING_LEFT_CHANNEL = "Boolean wenn true, dann wird links kalibriert. Sonst rechts.";

	private static final String NUMBER_FORMAT = "%3d%%";

	private int[] calibrationSettings = new int[8];
	private int channel;
	private boolean reloadCalib = false;
	public final static int resultCount = 4;

	// GUI Elemente
	private TextView calibrationTitle;
	private TextView textCurrentIntensityInPercent;
	private SeekBar intensity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration_dialog);
		Intent startIntent = getIntent();
		calibrationTitle = (TextView) findViewById(R.id.calibrationTitle);
		textCurrentIntensityInPercent = (TextView) findViewById(R.id.textIntensityPercent);
		intensity = (SeekBar) findViewById(R.id.seekBarMaxIntensity);

		textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT, 0));

		if (startIntent.getBooleanExtra(CALIBRATING_LEFT_CHANNEL, true)) {
			calibrationTitle.setText(R.string.calibrate_title_left);
			channel = 0;
		} else {
			calibrationTitle.setText(R.string.calibrate_title_right);
			channel = 1;
		}

		if (startIntent.hasExtra(CalibrationDialog.CALIBRATION_VALUES)) {
			calibrationSettings = startIntent
					.getIntArrayExtra(CALIBRATION_VALUES);
			textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT,
					calibrationSettings[channel * resultCount]));
			intensity.setProgress(calibrationSettings[channel * resultCount]);
		}

		intensity.setOnSeekBarChangeListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TrackingTool.REQUEST_CODE_CALIBRATION
				&& data.hasExtra(CalibrationDialog.CALIBRATION_VALUES)) {

			calibrationSettings = data
					.getIntArrayExtra(CalibrationDialog.CALIBRATION_VALUES);

			if (resultCode == RESULT_OK) {
				Intent resultIntent = new Intent();
				resultIntent
						.putExtra(
								CalibrationDialog.CALIBRATION_VALUES,
								data.getIntArrayExtra(CalibrationDialog.CALIBRATION_VALUES));
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		}
	}

	@Override
	public void onBackPressed() {
		CommandManager.stopSignal(channel);
		// Ergebnisse der Kalibrierung werden in einem Intent übergeben.
		Intent resultIntent = new Intent();

		resultIntent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		setResult(RESULT_CANCELED, resultIntent);
		super.onBackPressed();
	}

	/**
	 * Die Activity wird beendet mit der Ergebnisflag RESULT_CANCELED
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void back(View view) {
		onBackPressed();
	}

	/**
	 * Die Activity statet den nächsten Kalibrierungsdialog.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void forward(View view) {
		CommandManager.stopSignal(channel);
		Intent intent = new Intent(this, CalibrationDialogAngle.class);
		intent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		if (channel == 1) {
			intent.putExtra(CalibrationDialog.CALIBRATING_LEFT_CHANNEL, false);
		}
		startActivityForResult(intent, TrackingTool.REQUEST_CODE_CALIBRATION);
	}

	/**
	 * Stoppt das Signal
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void stopCalibration(View view) {
		CommandManager.stopSignal(channel);
	}

	/**
	 * Aktiviert den Kanal fuer fünf Sekunden mit der zuletzt kalibrierten
	 * Intensitaet.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void testIntensity(View view) {
		CommandManager.setIntensityForTime(channel, 100, 30000);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT,
				progress));
		CommandManager.setMaxIntensityForChannel(channel, progress);
		if (!reloadCalib) {
			CommandManager.setIntensityForTime(channel, 100, 1000);
			calibrationSettings[resultCount + channel * resultCount - 1] = 100;
		}
		calibrationSettings[channel * resultCount] = progress;

	}

	public void loadLastCalibSet(View view) {
		reloadCalib = true;
		calibrationSettings = CalibrationSaver.readCalibrationValues(calibrationSettings.length);
		intensity.setProgress(calibrationSettings[channel * resultCount]);
		reloadCalib = false;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//Nichts passiert.
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//Nichts passiert.
	}
}
