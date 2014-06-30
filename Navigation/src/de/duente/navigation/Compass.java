package de.duente.navigation;

import java.util.List;

import com.example.navigation.R;

import de.duente.navigation.route.GeoPoint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class Compass extends Activity {
	SurfaceView surfaceView;
	float[] gravity = new float[3];
	float[] geomagnetic = new float[3];
	float[] rotationMatrix = new float[9];
	float[] inclinationMatrix = new float[9];
	float[] backOfSmartphoneCalc = new float[3];
	private final float[] mRotationMatrix = new float[9];
	private final float[] orhtProjection = { 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 0.0f, 0.0f };

	float[] rotDirection = { 1.0f, 0.0f, 0.0f };
	GeoPoint from = new GeoPoint();
	GeoPoint to = new GeoPoint();

	float[] orientation = new float[3];
	
	float winkel = 0.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compass);
		mRotationMatrix[0] = 1;
		mRotationMatrix[4] = 1;
		mRotationMatrix[8] = 1;
		LocationListener locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				from = new GeoPoint(location.getLatitude(),
						location.getLongitude());
				to = new GeoPoint(52.300456, 9.482242299999999);
				double angle = GeoPoint.calculateAngle(from, to);
				// System.out.println("Winkel von Nord aus: "
				// + angle);

				CompassView canvasView = (CompassView) findViewById(R.id.canvasView1);
				canvasView.setDirectionAngle(angle);
				// System.out.println("Winkel in Rad" + Math.toRadians(angle));
			}

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub

			}
		};

		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates("gps", 5000, // 5 Sekunden
				1, // 1m
				locationListener);

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// Magnetfeldsensor
		List<Sensor> sensorList = sensorManager
				.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor magneticSensor = sensorList.get(0);

		SensorEventListener magneticListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSensorChanged(SensorEvent sensorEvent) {
				geomagnetic = sensorEvent.values;
				CompassView canvasView = (CompassView) findViewById(R.id.canvasView1);

				// System.out.println("MAGNETIC: x:" + sensorEvent.values[0]
				// + " y:" + sensorEvent.values[1] + " z:"
				// + sensorEvent.values[2]);

				float northVector[] = new float[3];
				multMatrixVector(mRotationMatrix, sensorEvent.values,
						northVector);

				// System.out.println("Ergebnis: " + northVector[0] + ","
				// + northVector[1]);

				// von Vektor (0,1,0) Nordrichtung
				double angle = Math
						.toRadians(GeoPoint.calculateAngle(from, to));
				float[] direction = new float[3];
				direction[0] = (float) -Math.sin(-angle);
				direction[1] = (float) Math.cos(-angle);
				direction[2] = 0.0f;
				float[] test = new float[3];
				rotateVector3D(direction, orientation[2], orientation[1], orientation[0],test );
				
				System.out.println("direction: "+direction[0] +","
				+direction[1]);
				System.out.println("rotatedDirection: "+ test[0] +","
						+test[1] + ", " + test[2]);
				
				float[] test2 = new float[3];
				//multMatrixVector(rotationMatrix, test, test2);
				
				canvasView.setCDirection(test[0], test[1]);

				canvasView.setNorth(sensorEvent.values[0],
						sensorEvent.values[1]);

				// Repaint anfordern
				canvasView.invalidate();

			}

		};

		// Listener, Sensor, SensorDelay
		sensorManager.registerListener(magneticListener, magneticSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		// Gravitationssensor
		sensorList = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
		Sensor gravitySensor = sensorList.get(0);

		SensorEventListener gravityListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSensorChanged(SensorEvent sensorEvent) {
				CompassView canvasView = (CompassView) findViewById(R.id.canvasView1);
				canvasView.invalidate();
				canvasView.setGravity(sensorEvent.values);
				canvasView.invalidate();
				gravity = sensorEvent.values;

				// System.out.println("GRAVITY: x:" + sensorEvent.values[0]
				// + " y:" + sensorEvent.values[1] + " z:"
				// + sensorEvent.values[2]);
				//float[] values = new float[3];
				SensorManager.getRotationMatrix(rotationMatrix,
						inclinationMatrix, gravity, geomagnetic);
				
				float[] backOfSmartphone = {0.0f, 0.0f, 1.0f};

				
				Compass.multMatrixVector(rotationMatrix, backOfSmartphone, backOfSmartphoneCalc);
				
				System.out.println("Rotation von (0,0,1) = ("+backOfSmartphoneCalc[0] + "," + backOfSmartphoneCalc[1] + ","+ backOfSmartphoneCalc[2]+")");
				
				SensorManager.getOrientation(rotationMatrix, orientation);

				//
				System.out.println("Rotation um Z: "
						+ Math.toDegrees(orientation[0]) + "\nRotation um X: "
						+ Math.toDegrees(orientation[1]) + "\nRotation um Y: "
						+ Math.toDegrees(orientation[2]));

				/*
				 * for (int i = 0; i < 3; i++) { System.out.println(i +
				 * "Rotation: " + rotationMatrix[0 + i] + " , " +
				 * rotationMatrix[1 + i] + " , " + rotationMatrix[2 + i]); }
				 * System.out.println("Orientation: " + values[0] + " , " +
				 * values[1] + " , " + values[2]);
				 */
			}

		};
		sensorManager.registerListener(gravityListener, gravitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		// Gravitationssensor
		sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		Sensor rotationVectorSensor = sensorList.get(0);
		SensorEventListener rotationVectorListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int acurracy) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSensorChanged(SensorEvent sensorEvent) {

				// System.out.println("RotationVector: " + sensorEvent.values[0]
				// + " , " +
				// sensorEvent.values[1] + " , " + sensorEvent.values[2]);
				SensorManager.getRotationMatrixFromVector(mRotationMatrix,
						sensorEvent.values);

			}

		};
		sensorManager.registerListener(rotationVectorListener,
				rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	public static void multMatrixVector(float[] matrix, float[] vector, float[] values) {
		if (matrix.length == 9 && vector.length == 3 && values.length == 3) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					values[i] = values[i] + vector[j] * matrix[i * 3 + j];
				}
			}
		}
	}

	private void rotateVector3D(float[] vector, float angleX, float angleY,
			float angleZ, float[] values) {
		// X-Rotationsmatrix bauen:
		float[] xRot = new float[9];
		float sinA = (float) Math.sin(angleX);
		float cosA = (float) Math.cos(angleX);
		xRot[0] = 1;
		xRot[4] = cosA;
		xRot[8] = cosA;
		xRot[5] = -sinA;
		xRot[7] = sinA;
		float[] afterXRot = new float[3];
		multMatrixVector(xRot,vector,afterXRot );
		
		// Y-Rotationsmatrix bauen:
		float[] yRot = new float[9];
		float sinB = (float) Math.sin(angleY);
		float cosB = (float) Math.cos(angleY);
		yRot[4] = 1;
		yRot[0] = cosB;
		yRot[8] = cosB;
		yRot[6] = -sinB;
		yRot[2] = sinB;
		float[] afterYRot = new float[3];
		multMatrixVector(yRot,afterXRot,afterYRot );
		
		// Z-Rotationsmatrix bauen:
		float[] zRot = new float[9];
		float sinC = (float) Math.sin(angleZ);
		float cosC = (float) Math.cos(angleZ);
		zRot[8] = 1;
		zRot[0] = cosC;
		zRot[4] = cosC;
		zRot[1] = -sinC;
		zRot[3] = sinC;
		
		multMatrixVector(zRot,afterYRot,values );
	}
	
	
	public void handyInShorts(View view){
		
	}
	
	public void handyInMyDirection(View view){
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		System.out.println("Rotation: " + disp.getRotation());
		CompassView canvasView = (CompassView) findViewById(R.id.canvasView1);
		canvasView.setRotation(disp.getRotation());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compass, menu);
		return true;
	}

}
