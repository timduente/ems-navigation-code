package de.duente.navigation.study;

public class Level implements Comparable<Level> {
	public static final double ANGLE_DISTANCE = 5.0;

	public static int[] intensitys;

	public int intensity;
	public int channel;
	public double angle;
	private int id;

	public Level(int id) {
		this.id = id - 4;

		channel = this.id < 0 ? 0 : 1;
		if (this.id > 0) {
			intensity = (int)intensitys[4 + this.id];

			angle = this.id * ANGLE_DISTANCE;
		}

		else if (this.id < -1) {	
			intensity = (int)intensitys[Math.abs(this.id) -1];
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

	@Override
	public String toString() {
		return "" + id;
	}
}
