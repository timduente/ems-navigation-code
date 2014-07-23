package de.duente.navigation.study;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

public class CalibrationDialog extends Activity {

	public static final String CALIBRATION_VALUES = "calibrationValues";
	public static final String BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY = "Bluetooth is managed by the activity, which starts this Activity";
	public static final String CALIBRATING_LEFT_CHANNEL = "Boolean wenn true, dann wird links kalibriert. Sonst rechts.";

	// Floatingpoint insgesammt 6 Stellen 0 Nachkomma stellen mit Prozentzeichen
	// da hinter.
	private static final String NUMBER_FORMAT = "%3d%%";

	private int[] calibrationSettings = new int[8];
	private int channel;
	public final static int resultCount = 4;

	int shownIntensity = 0;

	// GUI Elemente
	private TextView calibrationTitle;
	private TextView textCurrentIntensityInPercent;
	private TextView textLastIntensityPercent;

	// Handler und dazugehöriges Runnable
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		int factor = 1;

		@Override
		public void run() {
			if (shownIntensity >= 40)
				factor = 4;
			else if (shownIntensity >= 20) {
				factor = 2;
			}
			shownIntensity = shownIntensity + factor;

			textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT,
					shownIntensity));
			CommandManager.setIntensityForTime(channel, (int) shownIntensity,
					500);

			if (shownIntensity < 100) {
				handler.postDelayed(this, 1000);
			} else {
				stopCalibration(null);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration_dialog);
		Intent startIntent = getIntent();
		calibrationTitle = (TextView) findViewById(R.id.calibrationTitle);
		textCurrentIntensityInPercent = (TextView) findViewById(R.id.textIntensityPercent);
		textLastIntensityPercent = (TextView) findViewById(R.id.textLastIntensityPercent);

		textCurrentIntensityInPercent.setText(String.format(NUMBER_FORMAT, 0));
		textLastIntensityPercent.setText(String.format(NUMBER_FORMAT, 0));

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
			textLastIntensityPercent.setText(String.format(NUMBER_FORMAT,
					calibrationSettings[channel * resultCount]));
			;
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
		// Ergebnisse der Kalibrierung werden in einem Intent übergeben.
		Intent resultIntent = new Intent();
		// resultIntent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		setResult(RESULT_CANCELED, resultIntent);
		super.onBackPressed();
	}

	public void back(View view) {
		onBackPressed();
	}

	public void forward(View view) {
		Intent intent = new Intent(this, CalibrationDialogAngle.class);
		intent.putExtra(CALIBRATION_VALUES, calibrationSettings);
		if (channel == 1) {
			intent.putExtra(CalibrationDialog.CALIBRATING_LEFT_CHANNEL, false);
		}
		// intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivityForResult(intent, TrackingTool.REQUEST_CODE_CALIBRATION);
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
		findViewById(R.id.buttonStartCalibration).setEnabled(true);

		calibrationSettings[channel * resultCount] = shownIntensity;
		calibrationSettings[resultCount + channel * resultCount - 1] = 100;
		textLastIntensityPercent.setText(String.format(NUMBER_FORMAT,
				shownIntensity));

		CommandManager.setMaxIntensityForChannel(channel, (int) shownIntensity);
	}

	/**
	 * Startet das Steigern der Intensitaet. Laeuft bis 100% oder bis Stopp
	 * gedrueckt wurde.
	 * 
	 * @param view
	 *            View von dem diese Methode aufgerufen wurde.
	 */
	public void startCalibration(View view) {
		CommandManager.setMaxIntensityForChannel(channel, 100);
		shownIntensity = 0;
		handler.postDelayed(runnable, 100);
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
		CommandManager.setIntensityForTime(channel, 100, 5000);
	}
}
