package de.duente.navigation.study;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CalibrationDialog extends Activity implements OnSeekBarChangeListener {

	public static final String CALIBRATION_VALUES = "calibrationValues";
	public static final String BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY = "Bluetooth is managed by the activity, which starts this Activity";
	public static final String CALIBRATING_LEFT_CHANNEL = "Boolean wenn true, dann wird links kalibriert. Sonst rechts.";

	// Floatingpoint insgesammt 6 Stellen 0 Nachkomma stellen mit Prozentzeichen
	// da hinter.
	private static final String NUMBER_FORMAT = "%3d%%";

	private int[] calibrationSettings = new int[8];
	private int channel;
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
		intensity.setOnSeekBarChangeListener(this);

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
		}
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

	public void back(View view) {
		onBackPressed();
	}

	public void forward(View view) {
		CommandManager.stopSignal(channel);	
		Intent intent = new Intent(this, CalibrationDialogAngle.class);
		intent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		if (channel == 1) {
			intent.putExtra(CalibrationDialog.CALIBRATING_LEFT_CHANNEL, false);
		}
		// intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
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
		CommandManager.setIntensityForTime(channel, 100, 5000);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT, progress));		
		CommandManager.setMaxIntensityForChannel(channel, progress);
		CommandManager.setIntensityForTime(channel, 100, 5000);
		calibrationSettings[channel * resultCount] = progress;
		calibrationSettings[resultCount + channel * resultCount - 1] = 100;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
