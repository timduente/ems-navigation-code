package de.duente.navigation;

import java.io.IOException;

import com.example.navigation.R;

import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class DummyApp extends Activity {

	private final static int REQUEST_ENABLE_BT = 1;

	// Mac Adresse des RN42 Bluetoothchips
	private final String mac = "00:06:66:67:3E:4B";

	private BluetoothConnector bluetoothConnector;
	private CommandManager commandManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dummy_app);

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
		getMenuInflater().inflate(R.menu.dummy_app, menu);
		return true;
	}

	public void sendSignalChannel0(View view) {
		long startTime = System.currentTimeMillis();
		EditText eT = (EditText) findViewById(R.id.pulseCount);
		EditText eT2 = (EditText) findViewById(R.id.pulsLength);
		EditText eT3 = (EditText) findViewById(R.id.pulseOff);

		int pulseOff = Integer.parseInt(eT3.getText().toString());
		int length = Integer.parseInt(eT2.getText().toString());
		int pulse = Integer.parseInt(eT.getText().toString());
		int intensity = 0;

		switch (view.getId()) {

		case R.id.b0_42:
			intensity = 33;
			break;
		case R.id.b0_84:
			intensity = 66;
			break;
		case R.id.b0_127:
			intensity = 100;
			break;
		}
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
		vibe.vibrate(100);
		CommandManager.setPulseForTime(0, intensity, startTime, pulse, length,
				pulseOff);
	}

	public void sendSignalChannel1(View view) {
		long startTime = System.currentTimeMillis();
		EditText eT = (EditText) findViewById(R.id.pulseCount);
		EditText eT2 = (EditText) findViewById(R.id.pulsLength);
		EditText eT3 = (EditText) findViewById(R.id.pulseOff);

		int pulseOff = Integer.parseInt(eT3.getText().toString());

		int length = Integer.parseInt(eT2.getText().toString());
		int pulse = Integer.parseInt(eT.getText().toString());
		int intensity = 0;
		
		switch (view.getId()) {

		case R.id.b1_42:
			intensity = 33;
			break;
		case R.id.b1_84:
			intensity = 66;
			break;
		case R.id.b1_127:
			intensity = 100;
			break;
		}
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
		vibe.vibrate(100);

		CommandManager.setPulseForTime(1, intensity, startTime, pulse, length,
				pulseOff);
	}

}
