package de.duente.navigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;

import com.example.navigation.*;

import de.duente.navigation.actions.CommandManager;
import de.duente.navigation.bluetooth.BluetoothConnector;
import de.duente.navigation.route.GeoPoint;
import de.duente.navigation.route.Route;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowPosition extends Activity {

	public final static String MANEUVER_FOLLOW = "follow";
	private final static String MANEUVER_RIGHT = "turn-right";
	private final static String MANEUVER_LEFT = "turn-left";
	private final static String MANEUVER_SLIGHT_RIGHT = "turn-slight-right";
	private final static String MANEUVER_SLIGHT_LEFT = "turn-slight-left";
	public final static String MANEUVER_FINISH = "finish";

	private final static int EMS_CHANNEL_RIGHT = 0;
	private final static int EMS_CHANNEL_LEFT = 1;

	public static final int REQUEST_CODE_CALIBRATION = 212;

	private Route route;
	private float[] intensity = new float[2];

	private final static int REQUEST_ENABLE_BT = 1;

	// Mac Adresse des RN42 Bluetoothchips
	private final String mac = "00:06:66:67:3E:4B";

	private BluetoothConnector bluetoothConnector;
	private CommandManager commandManager;
	private BluetoothAdapter adapter;

	private final LocationListener locationListener = new LocationListener() {
		Location lastLocation = null;

		@Override
		public void onLocationChanged(Location location) {
			TextView tView = (TextView) findViewById(R.id.textView);
			double distance = -1.0;
			if (route != null && route.getSize() > 0) {
				route.updateNextWayPoint(location);

				// Damit nachdem man um eine Kurve ist nicht weiterhin diese
				// angezeigt wird.
				if (route.getActStep().getStart()
						.distanceTo(new GeoPoint(location)) <= 8) {
					updateImage(route.getActStep().getText());
				} else {
					updateImage(MANEUVER_FOLLOW);
				}

				updateArduino(route.getActStep().getText(),
						route.getDistanceToStep(route.getActStepNumber(),
								location));
				if (lastLocation != null) {
					// Richtung berechnen aus letzer Location und location.
					// double actDirection = GeoPoint.calculateAngle(from, to);

				}
				distance = route.getDistanceToStep(route.getActStepNumber(),
						location);
			}

			tView.setText("Latitude: " + location.getLatitude()
					+ "\nLongitude: " + location.getLongitude() + "\nAbstand: "
					+ String.format("%.2fm", distance));
			lastLocation = location;
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// not used
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// not used
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// not used
		}
	};

	AlertDialog alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_position);

		LocationManager lm = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates("gps", 5000, // 5 Sekunden
				1, // 1m
				locationListener);
		ImageView imView = (ImageView) findViewById(R.id.nextStepImage);
		imView.setVisibility(ImageView.INVISIBLE);

		// GPS einschalten
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		sendBroadcast(intent);

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

		bluetoothConnector = new BluetoothConnector(mac, adapter);
		commandManager = new CommandManager(bluetoothConnector);
		new Thread(commandManager).start();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Verbindung zum Arduino fehlgeschlagen!");
		builder.setMessage("Verbindungsversuch erneut starten?");

		builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new BluetoothConnection().execute(bluetoothConnector);
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
		new BluetoothConnection().execute(bluetoothConnector);

	}

	public void connectBluetooth(View view) {
		new BluetoothConnection().execute(bluetoothConnector);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_position, menu);
		return true;
	}

	private void updateArduino(String direction, double distanceToNextStep) {
		if (direction.equals(MANEUVER_RIGHT)
				|| direction.equals(MANEUVER_SLIGHT_RIGHT)) {
			if (distanceToNextStep <= 550.0) {
				CommandManager.setPulseForTime(EMS_CHANNEL_RIGHT,
						(int) intensity[EMS_CHANNEL_RIGHT],
						System.currentTimeMillis(),
						(int) distanceToNextStep / 100, 1000, 1000);
			}
		} else if (direction.equals(MANEUVER_LEFT)
				|| direction.equals(MANEUVER_SLIGHT_LEFT)) {
			if (distanceToNextStep <= 550.0) {
				CommandManager.setPulseForTime(EMS_CHANNEL_LEFT,
						(int) intensity[EMS_CHANNEL_LEFT],
						System.currentTimeMillis(),
						(int) distanceToNextStep / 100, 1000, 1000);
			}
		}
	}

	private void updateImage(String direction) {
		ImageView imView = (ImageView) findViewById(R.id.nextStepImage);
		imView.setVisibility(ImageView.VISIBLE);
		if (direction.equals(MANEUVER_RIGHT)) {
			imView.setImageResource(R.drawable.right);
		} else if (direction.equals(MANEUVER_LEFT)) {
			imView.setImageResource(R.drawable.left);
		} else if (direction.equals(MANEUVER_FOLLOW)) {
			imView.setImageResource(R.drawable.forward);
		} else if (direction.equals(MANEUVER_SLIGHT_LEFT)) {
			imView.setImageResource(R.drawable.slight_left);
		} else if (direction.equals(MANEUVER_SLIGHT_RIGHT)) {
			imView.setImageResource(R.drawable.slight_right);
		} else if(direction.equals(MANEUVER_FINISH)) {
			imView.setImageResource(R.drawable.finish);
		}else{
			imView.setVisibility(ImageView.INVISIBLE);
		}

	}

	public void fakeGPS(View view) {
		if (route != null) {
			Location location = new Location("Tim GPS Provider");
			if (route.getActStepNumber() < route.getSize() - 1) {
				location.setLatitude(route
						.getStep(route.getActStepNumber() + 1).getStart()
						.getLatitude());
				location.setLongitude(route
						.getStep(route.getActStepNumber() + 1).getStart()
						.getLongitude());
			}else{
				location.setLatitude(route
						.getStep(route.getActStepNumber()).getStart()
						.getLatitude());
				location.setLongitude(route
						.getStep(route.getActStepNumber()).getStart()
						.getLongitude());
			}
			// if (route.getActWayPoint() < route.getSize() - 1)
			// route.setActWayPoint(route.getActWayPoint() + 1);
			locationListener.onLocationChanged(location);

		}
	}

	public void findRoute(View view) {
		route = null;
		EditText start = (EditText) findViewById(R.id.startLocation);
		EditText destination = (EditText) findViewById(R.id.endLocation);

		new NetworkConnection().execute(start.getText().toString(), destination
				.getText().toString());

	}

	@Override
	public void onDestroy() {

		// GPS ausschalten
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", false);
		sendBroadcast(intent);

		// Bluetooth ausschalten
		// if (adapter.isEnabled()) {
		// adapter.disable();
		// }
		super.onDestroy();
	}

	/**
	 * Startet die Kalibrierungsaktivitaet. Uebergibt die letzte Werte.
	 * 
	 * @param view
	 *            view der diese Methode aufruft
	 */
	public void startCalibrationActivity(View view) {
		Intent intent = new Intent(this, Calibration.class);
		intent.putExtra(Calibration.CALIBRATION_VALUES, intensity);
		intent.putExtra(Calibration.BLUETOOTH_MANAGED_BY_STARTING_ACTIVITY,
				true);
		startActivityForResult(intent, REQUEST_CODE_CALIBRATION);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CALIBRATION && resultCode == RESULT_OK
				&& data.hasExtra(Calibration.CALIBRATION_VALUES)) {

			intensity = data.getFloatArrayExtra(Calibration.CALIBRATION_VALUES);
		}
	}

	/**
	 * Stellt eine Verbindung zum Arduino her. Der Verbindungsaufbau kann lange
	 * dauern. Hierdurch wird der UI-Thread entlastet und es ist möglich eine
	 * Schleife zu machen, sodass das Verbinden mehrmals ausgeführt werden kann,
	 * wenn es fehlschlägt.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	private class BluetoothConnection extends
			AsyncTask<BluetoothConnector, Void, Boolean> {

		@Override
		protected Boolean doInBackground(
				BluetoothConnector... bluetoothConnector) {
			try {
				bluetoothConnector[0].connectBluetooth();
				return true;
			} catch (IOException e) {
				// e.printStackTrace();
				return false;
			}
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (result) {
				findViewById(R.id.connectArduino).setEnabled(false);
			} else {
				alert.show();
			}
		}
	}

	/**
	 * Private Klasse, die zuständing für den Netzwerkanfrage und für das
	 * Rausfischen von JSON Daten zuständig ist.
	 * 
	 * @author Tim Dünte
	 * 
	 */

	private class NetworkConnection extends AsyncTask<String, Void, String> {

		private final static String URL_BEGIN = "http://maps.googleapis.com/maps/api/directions/json?origin=";
		private final static String URL_DESTINATION = "&destination=";
		private final static String URL_END = "&sensor=true&mode=walking&language=german&region=de";

		@Override
		protected String doInBackground(String... params) {
			URL url;
			String line = null;
			String completeString = new String();
			String origin = new String();
			String destination = new String();

			if (params.length != 2) {
				return null;
			}

			origin = params[0].replace(" ", "+").replace("ß", "ss");
			destination = params[1].replace(" ", "+").replace("ß", "ss");

			StringBuilder urlToBuild = new StringBuilder();
			urlToBuild.append(URL_BEGIN);
			urlToBuild.append(origin);
			urlToBuild.append(URL_DESTINATION); // Übergabe auch möglich als
												// destination=52.345,9.234 (Geo
												// Koordinaten).
			urlToBuild.append(destination);
			urlToBuild.append(URL_END);

			try {
				System.out.println(urlToBuild.toString());
				url = new URL(urlToBuild.toString());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));

				while ((line = in.readLine()) != null) {
					completeString = completeString + line;
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return completeString;
		}

		@Override
		protected void onPostExecute(String result) {
			// TextView tView = (TextView) findViewById(R.id.routInfotext);
			// String text = new String();

			route = new Route();
			try {
				GoogleNavigationAPI_JSON_Parser parser = new GoogleNavigationAPI_JSON_Parser(
						result);
				route = parser.parseJsonStringInRoute();

				TextView start = (TextView) findViewById(R.id.startLocation);
				TextView end = (TextView) findViewById(R.id.endLocation);

				start.setText(route.getStartLocation());
				end.setText(route.getEndLocation());

				route.printMe();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// tView.setText(text);
		}
	}
}
