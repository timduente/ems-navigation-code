package de.duente.navigation.route;

import android.location.Location;

public class GeoPoint {
	double longitude;

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	double latitude;

	public GeoPoint() {
		this.latitude = 0.0;
		this.longitude = 0.0;
	}

	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public GeoPoint(Location location){
		this.latitude = location.getAltitude();
		this.longitude = location.getLongitude();
	}

	
//	public double distanceTo(GeoPoint geoPoint) {
//		return 0.0;
//	}

	@Override
	public String toString() {
		return "lat.: " + latitude + ";lon.: " + longitude;
	}

	private static double calculateOrthodrome(double phiA, double phiB,
			double lamdaA, double lambdaB) {
		return Math.acos(Math.sin(phiA) * Math.sin(phiB) + Math.cos(phiA)
				* Math.cos(phiB) * Math.cos(lambdaB - lamdaA));
	}

	/**
	 * Berechnet den Richtungswinkel für zwei GeoPoint Objekte in Bezug auf den
	 * Nordvektor.
	 * 
	 * @param from
	 *            Momentane Position
	 * @param to
	 *            Ziel
	 * @return gibt den Winkel in Grad zurück
	 */
	public static double calculateAngle(GeoPoint from, GeoPoint to) {
		double phiB = Math.toRadians(to.latitude);
		double phiA = Math.toRadians(from.latitude);
		double lambdaA = Math.toRadians(from.longitude);
		double lambdaB = Math.toRadians(to.longitude);

		double e = calculateOrthodrome(phiA, phiB, lambdaA, lambdaB);
		double rad = Math.acos((Math.sin(phiB) - Math.sin(phiA) * Math.cos(e))
				/ (Math.cos(phiA) * Math.sin(e)));
		return Math.toDegrees(rad);
	}
}
