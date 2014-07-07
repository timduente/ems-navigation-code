package de.duente.navigation.study;

public class Level implements Comparable<Level>{
	public int intensity;
	public int channel;
	private int id;
	
	public Level(int id){
		this.id = id - 3;
		channel = this.id < 0 ? 0 : 1;
		intensity = Math.abs(this.id) / 3 * 100;
	}
	
	
	@Override
	public int compareTo(Level another) {
		return id - another.id;
	}
	
	@Override
	public String toString(){
		return "" + id;
	}
}
