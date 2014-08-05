package de.duente.navigation.study;

/**
 * Diese Klasse repr�sentiert ein Level f�r die Studie. Ein Level enth�lt eine
 * ID, eine Intensit�t und einen Kanal.
 * 
 * @author Tim D�nte
 * 
 */
public class Level implements Comparable<Level> {
	public static final double ANGLE_DISTANCE = 5.0;

	public static int[] intensitys;

	public int intensity;
	public int channel;
	public double angle;
	private int id;

	/**
	 * Erstellt ein neues Level mit vorgebener ID.
	 * 
	 * @param id
	 *            Wert von 0-7;
	 */
	public Level(int id) {
		this.id = id - 4;

		channel = this.id < 0 ? 0 : 1;
		if (this.id > 0) {
			intensity = (int) intensitys[4 + this.id];

			angle = this.id * ANGLE_DISTANCE;
		}

		else if (this.id < -1) {
			intensity = (int) intensitys[Math.abs(this.id) - 1];
			angle = (this.id + 1) * ANGLE_DISTANCE;
		} else {
			intensity = 0;
			angle = 0.0;
		}
		System.out.println("intensity: " + intensity + " id: " + this.id);
	}

	@Override
	public int compareTo(Level another) {
		return id - another.id;
	}

	/**
	 * Gibt true zur�ck, wenn die Intensit�ten 0 sind. F�r Baseline.
	 * 
	 * @return true wenn id = 0 oder -1 ist sonst false.
	 */
	public boolean isMiddle() {
		return (id == 0 || id == -1);
	}

	@Override
	public String toString() {
		return "" + id;
	}
}
