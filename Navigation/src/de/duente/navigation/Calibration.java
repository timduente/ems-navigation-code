package de.duente.navigation;

import java.io.IOException;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
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

public class Calibration extends Activity {
	
	private String[] state = { "0", "1" };

	private float[] calibrationSettings = new float[state.length];

	float shownIntensity = 0.0f;
	int intensity = 0;
	int times = 0;

	private Handler handler = new Handler();
	
	private Spinner spinner;
	private TextView textView;
	private TextView textLastIntensityPercent;

	private final static int REQUEST_ENABLE_BT = 1;

	// Mac Adresse des RN42 Bluetoothchips
	private final String mac = "00:06:66:67:3E:4B";

	private BluetoothConnector bluetoothConnector;
	private CommandManager commandManager;
	
	private Runnable runnable = new Runnable() {		
		
		@Override
		public void run() {			
			shownIntensity =  times *  100.0f / 127.0f;
			intensity = (int) shownIntensity;
			
		
			textView.setText(String.format("%6.2f", shownIntensity) + "%");
			
			
			String s = (String)spinner.getSelectedItem();
			System.out.println("Channel der ausgewählt wurde: " + s + "Times: " + times);
			
			//textView.setText(shownIntensity + "%");
			
			CommandManager.setIntensityForTime(Integer.parseInt(s), intensity, 500, 0);
			times = times + 5;
			/* and here comes the "trick" */
			if (shownIntensity < 100.0f) {
				handler.postDelayed(this, 1000);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);

		spinner = (Spinner) findViewById(R.id.spinnerChannel);
		textView = (TextView)findViewById(R.id.textIntensityPercent);
		textLastIntensityPercent = (TextView)findViewById(R.id.textLastIntensityPercent);
		
		textView.setText(String.format("%6.2f%%", 0.0f));
		textLastIntensityPercent.setText(String.format("%6.2f%%", 0.0f));


		ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, state);

		spinner.setAdapter(adapter_state);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	String s = (String)spinner.getSelectedItem();
		    	textView.setText(String.format("%6.2f%%", 0.0f));
				textLastIntensityPercent.setText(String.format("%6.2f%%", calibrationSettings[Integer.parseInt(s)]));
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // your code here
		    }

		});
		

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calibration, menu);
		return true;
	}

	public void stopCalibration(View view) {
		handler.removeCallbacks(runnable);
		spinner.setEnabled(true);
		
		String s = (String)spinner.getSelectedItem();
		calibrationSettings[Integer.parseInt(s)] = shownIntensity;
		textLastIntensityPercent.setText(String.format("%6.2f%%", shownIntensity));
	}

	public void startCalibration(View view) {
		shownIntensity = 0.0f;
		times = 0;
		handler.postDelayed(runnable, 100);
		spinner.setEnabled(false);
		
	}
	
	public void testIntensity(View view){
		String s = (String)spinner.getSelectedItem();
		int channel = Integer.parseInt(s);
		CommandManager.setIntensityForTime(channel, (int)calibrationSettings[channel], 500, 0);
	}
}