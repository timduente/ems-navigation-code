package de.duente.navigation.study;

public class Level implements Comparable<Level>{
	public int intensity;
	public int channel;
	private int id;
	
	public Level(int id){
		this.id = id - 4;
		channel = this.id < 0 ? 0 : 1;		
		if(this.id >0){
			intensity = (int) (Math.pow(3.0, this.id - 1) * 11.0);
			if (Math.abs(this.id) == 2) {
				intensity = 100;
			}
		}
		
		if(this.id < -1){
			intensity = (int) (Math.pow(3.0, Math.abs(this.id) - 2) * 11.0);
			if (Math.abs(this.id) == 2) {
				intensity = 100;
			}	
		}
		else{
			intensity = 0;
		}
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
