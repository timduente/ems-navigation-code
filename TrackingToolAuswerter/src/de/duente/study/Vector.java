package de.duente.study;

import java.util.ArrayList;

/**
 * Diese Klasse repräsentiert einen 2D-Vektor.
 * 
 * @author Tim Dünte
 * 
 */
public class Vector {
	// 0 ist X, 1 ist Z
	private float[] position = new float[2];
	private float length = 0.0f;
	long firstFrame;
	long lastFrame;

	/**
	 * Gibt die Länge des Winkels zurück.
	 * 
	 * @return Lange des Winkels in Metern.
	 */
	public float getLength() {
		return length;
	}

	/**
	 * Erstellt einen 2D Vektor aus 4 Koordinaten. Der entstehende Vektor ist
	 * der Differenzvektor.
	 * 
	 * @param x1
	 *            X-Koordinate von Vektor 1
	 * @param z1
	 *            Z-Koordinate von Vektor 1
	 * @param x2
	 *            X-Koordinate von Vektor 2
	 * @param z2
	 *            Z-Koordinate von Vektor 2
	 * 
	 */
	public Vector(float x1, float z1, float x2, float z2, long firstFrame,
			long lastFrame) {
		position[0] = x2 - x1;
		position[1] = z2 - z1;
		length = (float) Math.sqrt(position[0] * position[0] + position[1]
				* position[1]);
		this.firstFrame = firstFrame;
		this.lastFrame = lastFrame;

		if (this.length == 0.0f) {
			System.err.println("Fehler: Frame ids: " + firstFrame + " x1: "
					+ x1 + " z1: " + z1 + " bis " + lastFrame + " x2: " + x2
					+ " z2: " + z2);
		}
	}

	/**
	 * Erstellt einen 2D Vektor über die direkte Angabe der Koordinaten.
	 * 
	 * @param x
	 *            X-Koordinate
	 * @param z
	 *            Z-Koordinate
	 */
	public Vector(float x, float z) {
		position[0] = x;
		position[1] = z;
		length = (float) Math.sqrt(position[0] * position[0] + position[1]
				* position[1]);
		if (Float.isNaN(length)) {
			System.err.println("NAN-ERROR" + this);
		}
		firstFrame = 0;
		lastFrame = 0;
	}

	/**
	 * Berechnet den Winkel zwischen zwei Vector Objekten. Vector1 ist dabei der
	 * Bezugsvektor. Winkel gegen den Uhrzeigersinn ist negativ. Winkel mit dem
	 * Uhrzeigersinn ist positiv.
	 * 
	 * @param vector1
	 *            Bezugsvektor
	 * @param vector2
	 *            Zweiter Vektor, dessen Winkel zum ersten bestimmt werden soll.
	 * @return Winkel in Grad
	 */
	public static float getAngleBetweenVecs(Vector vector1, Vector vector2) {
		double cosinus = (vector1.position[0] * vector2.position[0] + vector1.position[1]
				* vector2.position[1])
				/ (vector1.length * vector2.length);

		// Fehler in der Winkelberechnung durch Ungenauigkeiten werden
		// ausgegelichen, damit keine NAN Values entstehen.
		if (cosinus > 1.0f && cosinus < 1.0001f) {
			cosinus = 1.0f;
		} else if (cosinus < -1.0f && cosinus > -1.0001f) {
			cosinus = -1.0f;
		}

		float angle = (float) Math.toDegrees(Math.acos(cosinus));

		if (Float.isNaN(angle)) {
			System.err.println("NAN-ERROR" + cosinus);
		}

		// Um den Winkeln eine Orientierung zu geben.
		if ((vector1.position[0] * vector2.position[1] - vector1.position[1]
				* vector2.position[0]) > 0) {
			return angle;

		} else {
			return -angle;
		}
	}

	public static float getVelocity(ArrayList<Vector> vectors) {
		if (vectors.size() == 0) {
			return 0.0f;
		}
		float totalLength = 0.0f;
		for (int i = 0; i < vectors.size(); i++) {
			totalLength = totalLength + vectors.get(i).length;
		}

		float velocity = totalLength
				/ ((vectors.get(vectors.size() - 1).lastFrame - vectors.get(0).firstFrame) * 0.033333333f);
		// System.out.println("Länge: " + totalLength + " ;Frames: "
		// +(vectors.get(vectors.size()-1).lastFrame-vectors.get(0).firstFrame));

		return velocity;
	}

	@Override
	public String toString() {
		return "Position: (" + position[0] + ", " + position[1] + ") Laenge: "
				+ length;
	}
}
