package de.duente.navigation.study;

import java.util.ArrayList;

public class Participant {
	
	private final static int ITERATION_COUNT = 5;
	public final static int DIFFERENT_LEVEL_COUNT = 8;
	
	private static final RandomLatinSquare randomLatinSquare = new RandomLatinSquare(
			DIFFERENT_LEVEL_COUNT);
	
	private ArrayList<Level> levels;
	
	private int id;
	private int[] calibratedIntensitys;
	private int actualLevel;
	private boolean initialized;
	
	
	
	public Participant(int id){
		this.id = id;		
		actualLevel = -1;
		initialized = false;
	}
	
	public boolean isInitialized(){
		return initialized;
	}
	
	public void setCalibratedIntensitys(int[] intensitys){
		calibratedIntensitys = intensitys;
		for(int i = 0; i<intensitys.length; i++){
			System.out.println("Intensität an : " + i + " = " + intensitys[i]);
		}
		levels = new ArrayList<Level>();
		initialiazeRandomList();
		actualLevel = 0;
		initialized = true;
	}
	
	public int[] getIntensitys(){
		return calibratedIntensitys;
	}
	
	public Level getActualLevel(){
		return levels.get(actualLevel);
	}
	
	public int getActualLevelIndex(){
		return actualLevel;
	}
	
	public boolean isStudyDone(){
		return actualLevel >= levels.size();
	}
	
	public boolean isLastSet(){
		return (levels.size() - DIFFERENT_LEVEL_COUNT) <= actualLevel;
	}
	
	public void nextLevel(){
		if(actualLevel < levels.size()){
			actualLevel++;
		}
	}
	
	
	private void initialiazeRandomList() {
		Level.intensitys = calibratedIntensitys;
		int[] randomizedNumbers = randomLatinSquare.randomize(10);
		levels = new ArrayList<Level>();
		for (int j = 0; j < ITERATION_COUNT; j++) {
			for (int i = 0; i < DIFFERENT_LEVEL_COUNT; i++) {
				if (j % 2 == 0)
					levels.add(new Level(randomizedNumbers[j
							* DIFFERENT_LEVEL_COUNT + i]));
				else {
					levels.add(new Level(randomizedNumbers[j
							* DIFFERENT_LEVEL_COUNT + DIFFERENT_LEVEL_COUNT - 1
							- i]));
				}
				// System.out.println(levels.get(i * ITERATION_COUNT + j));
			}
		}
	}
	
	@Override
	public String toString(){
		return "Participant_" + id;
	}

}
