package de.duente.navigation.actions;

/**
 * Interface für Aktionen
 * 
 * @author Tim Dünte
 * 
 */

public interface IAction {

	/**
	 * Prüft ob das Kommando gestartet werden muss.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check();

	/**
	 * Gibt das Kommando dieser Aktion zurück.
	 * 
	 * @return Kommando
	 */
	public String getCommand();

	/**
	 * Prüft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            Aktuelle Zeit in der Zeiteinheit, die beim Erstellen übergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	boolean check(long actualTime);
}
