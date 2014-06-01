package de.duente.navigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.navigation.*;

import de.duente.navigation.actions.CommandManager;
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

	private final static String MANEUVER_FOLLOW = "follow";
	private final static String MANEUVER_RIGHT = "turn-right";
	private final static String MANEUVER_LEFT = "turn-left";
	
	private final static int EMS_CHANNEL_RIGHT = 0;
	private final static int EMS_CHANNEL_LEFT = 1;
	
	private Route route = new Route();

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
			if (route.getSize() > 0) {
				route.updateNextWayPoint(location);
				updateImage(route.getActStep().getText());
				updateArduino(route.getActStep().getText(), route.getDistanceToStep(route.getActWayPoint(), location));
				if(lastLocation != null){
					//Richtung berechnen aus letzer Location und location.
					//double actDirection = GeoPoint.calculateAngle(from, to);
					
				}
			}

			tView.setText("Latitude: " + location.getLatitude()
					+ "\nLongitude: " + location.getLongitude() + "\nAbstand: "
					+ route.getDistanceToStep(route.getSize() - 1, location)
					+ "m");
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
	
	private void updateArduino(String direction, double distanceToNextStep){
		if (direction.equals(MANEUVER_RIGHT)) {
			if(distanceToNextStep <= 550.0){
				CommandManager.setPulseForTime(EMS_CHANNEL_RIGHT, 50, System.currentTimeMillis(), (int)distanceToNextStep / 100, 1000, 1000, 0);
			}		
		} else if (direction.equals(MANEUVER_LEFT)) {
			if(distanceToNextStep <= 550.0){
				CommandManager.setPulseForTime(EMS_CHANNEL_LEFT, 50, System.currentTimeMillis(), (int)distanceToNextStep / 100, 1000, 1000, 0);
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
		} else {
			imView.setVisibility(ImageView.INVISIBLE);
		}

	}

	public void findRoute(View view) {
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
				//e.printStackTrace();
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
		private final static String JSON_ENTRY_STATUS = "status";
		private final static String JSON_ENTRY_ROUTES = "routes";
		private final static String JSON_ENTRY_COPYRIGHTS = "copyrights";
		private final static String JSON_ENTRY_LEGS = "legs";
		private final static String JSON_ENTRY_STARTADDRESS = "start_address";
		private final static String JSON_ENTRY_ENDADDRESS = "end_address";
		private final static String JSON_ENTRY_STARTLOCATION = "start_location";
		private final static String JSON_ENTRY_ENDLOCATION = "end_location";
		private final static String JSON_ENTRY_STEPS = "steps";
		private final static String JSON_ENTRY_MANEUVER = "maneuver";
		private final static String JSON_ENTRY_LATITUDE = "lat";
		private final static String JSON_ENTRY_LONGITUDE = "lng";
		private final static String JSON_ENTRY_HTMLINSTRUCTIONS = "html_instructions";

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
			TextView tView = (TextView) findViewById(R.id.routInfotext);
			String text = new String();
			route = new Route();

			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);

				System.out.println("Status: "
						+ jsonObj.getString(JSON_ENTRY_STATUS));
				// Status kann auf NOT_FOUND oder OK überprüft werden.

				JSONArray routes = jsonObj.getJSONArray(JSON_ENTRY_ROUTES);
				JSONObject googleRoute = (JSONObject) routes.get(0);
				// Länge von JSON Array kann abgefragt werden.

				System.out.println("CopyRights: "
						+ googleRoute.get(JSON_ENTRY_COPYRIGHTS));

				JSONArray legs = googleRoute.getJSONArray(JSON_ENTRY_LEGS);
				// Da wir nur eine Route wollen nehmen wir leg 0
				JSONObject leg = legs.getJSONObject(0);

				TextView start = (TextView) findViewById(R.id.startLocation);
				TextView end = (TextView) findViewById(R.id.endLocation);

				start.setText(leg.getString(JSON_ENTRY_STARTADDRESS));
				end.setText(leg.getString(JSON_ENTRY_ENDADDRESS));

				JSONObject startLocation = leg
						.getJSONObject(JSON_ENTRY_STARTLOCATION);
				JSONObject endLocation = leg
						.getJSONObject(JSON_ENTRY_ENDLOCATION);
				System.out.println("Start Location: "
						+ startLocation.toString());
				JSONArray steps = leg.getJSONArray(JSON_ENTRY_STEPS);

				text = "\nStart: " + startLocation.toString() + "\n";
				for (int i = 0; i < steps.length(); i++) {
					JSONObject step = steps.getJSONObject(i);
					/*
					 * text = text + i + ". START " +
					 * step.getJSONObject("start_location").toString() +
					 * "ENDE: " + step.getJSONObject("end_location").toString()
					 * + "\n";
					 */
					// step.get
					JSONObject strloc = step
							.getJSONObject(JSON_ENTRY_STARTLOCATION);
					JSONObject endloc = step
							.getJSONObject(JSON_ENTRY_ENDLOCATION);
					String maneuver = new String();

					// Wenn es kein maneuver gibt, muss man weiter der route
					// folgen.
					if (step.has(JSON_ENTRY_MANEUVER)) {
						maneuver = step.getString(JSON_ENTRY_MANEUVER);
					} else {
						maneuver = MANEUVER_FOLLOW;
					}
					Step stepi = new Step(new GeoPoint(
							strloc.getDouble(JSON_ENTRY_LATITUDE),
							strloc.getDouble(JSON_ENTRY_LONGITUDE)),
							new GeoPoint(endloc.getDouble(JSON_ENTRY_LATITUDE),
									endloc.getDouble(JSON_ENTRY_LONGITUDE)),
							maneuver);
					stepi.setHtmlInstruction(step
							.getString(JSON_ENTRY_HTMLINSTRUCTIONS));
					route.addStep(stepi);
					// do something über i.
				}

				route.printMe();
				text = text + "\n Tot. Ende: " + endLocation.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tView.setText(text);
		}

	}
}
