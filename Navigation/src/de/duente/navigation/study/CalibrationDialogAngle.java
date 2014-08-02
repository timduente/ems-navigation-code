package de.duente.navigation.study;

import com.example.navigation.R;
import de.duente.navigation.actions.CommandManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CalibrationDialogAngle extends Activity implements
		OnSeekBarChangeListener {

	private TextView calibrationTitle;
	private int channel;
	private int[] calibrationSettings;

	private SeekBar[] alpha;
	private TextView[] labels;
	private Button[] testButtons;

	private AlertDialog alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration_dialog_angle);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Wirklich fortfahren?");
		builder.setMessage("Hast du alle Werte gemessen? Winkel, Spannungen und Ströme?");

		builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				forward();
				dialog.dismiss();
			}

		});

		builder.setNegativeButton("NEIN",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Code der ausgeführt wird wenn NEIN geklickt wurde
						dialog.dismiss();
					}

				});
		alert = builder.create();

		Intent startIntent = getIntent();

		calibrationTitle = (TextView) findViewById(R.id.angleCalibTitle);
		alpha = new SeekBar[3];
		labels = new TextView[3];
		testButtons = new Button[3];

		alpha[0] = (SeekBar) findViewById(R.id.seekBar1);
		alpha[1] = (SeekBar) findViewById(R.id.seekBar2);
		alpha[2] = (SeekBar) findViewById(R.id.seekBar3);

		labels[0] = (TextView) findViewById(R.id.textView4);
		labels[1] = (TextView) findViewById(R.id.textView5);
		labels[2] = (TextView) findViewById(R.id.textView6);

		testButtons[0] = (Button) findViewById(R.id.button1);
		testButtons[1] = (Button) findViewById(R.id.button3);
		testButtons[2] = (Button) findViewById(R.id.button5);

		if (startIntent.getBooleanExtra(
				CalibrationDialog.CALIBRATING_LEFT_CHANNEL, true)) {
			calibrationTitle.setText(R.string.calibrate_title_left);
			channel = 0;
		} else {
			calibrationTitle.setText(R.string.calibrate_title_right);
			channel = 1;
		}
		calibrationSettings = startIntent
				.getIntArrayExtra(CalibrationDialog.CALIBRATION_VALUES);

		for (int i = 0; i < alpha.length; i++) {		
			alpha[i].setProgress(calibrationSettings[(i + 1)
					+ CalibrationDialog.resultCount * channel]);
			alpha[i].setOnSeekBarChangeListener(this);
		}
		updateView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TrackingTool.REQUEST_CODE_CALIBRATION
				&& data.hasExtra(CalibrationDialog.CALIBRATION_VALUES)) {		
			calibrationSettings = data.getIntArrayExtra(CalibrationDialog.CALIBRATION_VALUES);
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
		stop(null);
		// Ergebnisse der Kalibrierung werden in einem Intent übergeben.
		for (int i = 0; i < alpha.length; i++) {
			calibrationSettings[(i + 1) + CalibrationDialog.resultCount
					* channel] = alpha[i].getProgress();
		}
		Intent resultIntent = new Intent();
		resultIntent.putExtra(CalibrationDialog.CALIBRATION_VALUES,
				calibrationSettings);
		setResult(RESULT_CANCELED, resultIntent);
		super.onBackPressed();
	}

	public void back(View view) {
		
		onBackPressed();
	}

	private void forward() {
		stop(null);
		Intent intent = new Intent(this, CalibrationDialog.class);
		for (int i = 0; i < alpha.length; i++) {
			calibrationSettings[(i + 1) + CalibrationDialog.resultCount
					* channel] = alpha[i].getProgress();
		}
		if (channel == 0) {
			intent.putExtra(CalibrationDialog.CALIBRATING_LEFT_CHANNEL, false);
			intent.putExtra(CalibrationDialog.CALIBRATION_VALUES,
					calibrationSettings);
			startActivityForResult(intent,
					TrackingTool.REQUEST_CODE_CALIBRATION);
		} else if (channel == 1) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(CalibrationDialog.CALIBRATION_VALUES,
					calibrationSettings);
			setResult(RESULT_OK, resultIntent);
			finish();
		}
	}

	public void forward(View view) {
		alert.show();
	}

	public void test(View view) {
		for (int i = 0; i < testButtons.length; i++) {
			if (view == testButtons[i]) {
				CommandManager.setIntensityForTime(channel,
						alpha[i].getProgress(), 30000);
			}
		}
	}

	public void stop(View view) {
		CommandManager.stopSignal(channel);
	}
	
	public void updateView(){
		for (int i = 0; i < alpha.length; i++) {
			labels[i].setText(alpha[i].getProgress() + "%");
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		updateView();
		for (int i = 0; i < alpha.length; i++) {
			if (alpha[i] == seekBar) {
				CommandManager.setIntensityForTime(channel, progress, 5000);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// keine Funktion

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// keine Funktion

	}
}
