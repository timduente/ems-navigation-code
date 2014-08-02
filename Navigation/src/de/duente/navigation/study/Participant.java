package de.duente.navigation.study;

import java.util.ArrayList;

/**
 * Diese Klasse verwaltet die Informationen des Benutzers. Ein Studienteilnehmer
 * hat eine Liste von Levels, die nach dem LatinSquare-Verfahren randomisiert
 * sind. Er besitzt eine ID und ein Array mit Kalibrierungswerten.
 * 
 * @author Tim Dünte
 * 
 */
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

	/**
	 * Erstellt einen neuen Teilnehmer. Dieser ist nach dem erstellen noch nicht
	 * initialisiert, da die Kalibrierungsdaten fehlen.
	 * 
	 * @param id
	 *            ID die der Teilnehmer bekommen soll.
	 */
	public Participant(int id) {
		this.id = id;
		actualLevel = -1;
		initialized = false;
	}

	/**
	 * Gibt zurück, ob der Teilnehmer initialisiert/kalibriert ist.
	 * 
	 * @return true, wenn Kalibrierungsinformationen vorliegen. false, falls
	 *         nicht.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Mit dieser Methode wird der Teilnehmer initialisiert. Es werden die Werte
	 * aus der Kalibrierung übergeben. Die Levelliste wird erstellt.
	 * 
	 * Die Form des Array ist: [Maximalintensität Kanal 0, Intensität Winkel
	 * schwach Kanal 0,Intensität Winkel mittel Kanal 0, Intensität Winkel
	 * starkKanal 0, Maximalintensität Kanal 1, Intensität Winkel schwach Kanal
	 * 1,Intensität Winkel mittel Kanal 1, Intensität Winkel starkKanal 1]
	 * 
	 * @param intensitys
	 *            Array, welches die Kalibrierungsinformationen enthält.
	 */
	public void setCalibratedIntensitys(int[] intensitys) {
		calibratedIntensitys = intensitys;
		// for (int i = 0; i < intensitys.length; i++) {
		// System.out.println("Intensität an : " + i + " = " + intensitys[i]);
		// }
		levels = new ArrayList<Level>();
		initialiazeRandomList();
		actualLevel = 0;
		initialized = true;
	}

	/**
	 * Gibt die Kalibrierungsinformationen zurück.
	 * 
	 * @return Kalibrierungsinformationen des Teilnehmers.
	 */
	public int[] getIntensitys() {
		return calibratedIntensitys;
	}

	/**
	 * Gibt das aktuelle Level zurück.
	 * 
	 * @return aktuelles Level
	 */
	public Level getActualLevel() {
		return levels.get(actualLevel);
	}

	/**
	 * Gibt den Index des aktuellen Levels zurück.
	 * 
	 * @return Index des aktuellen Levels
	 */
	public int getActualLevelIndex() {
		return actualLevel;
	}

	/**
	 * Gibt an, ob der Teilnehmer alle Level durchlaufen hat.
	 * 
	 * @return true, wenn der Teilnehmer alle Level durchlaufen hat. false,
	 *         falls nicht.
	 */
	public boolean isStudyDone() {
		return actualLevel >= levels.size();
	}

	/**Gibt an, ob der Teilnehmer sich im letzten Block befindet.
	 * 
	 * @return true, falls der TN sich im letzten Block befindet. Sonst false.
	 */
	public boolean isLastSet() {
		return (levels.size() - DIFFERENT_LEVEL_COUNT) <= actualLevel;
	}

	/**
	 * Setzt das nächste Level.
	 */
	public void nextLevel() {
		if (actualLevel < levels.size()) {
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
	public String toString() {
		return "Participant_" + id;
	}

}
