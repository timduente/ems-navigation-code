package de.duente.navigation.actions;

/**
 * Interface f�r Aktionen
 * 
 * @author Tim D�nte
 * 
 */

public interface IAction {

	/**
	 * Pr�ft ob das Kommando gestartet werden muss.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check();

	/**
	 * Gibt das Kommando dieser Aktion zur�ck.
	 * 
	 * @return Kommando
	 */
	public String getCommand();

	/**
	 * Pr�ft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            Aktuelle Zeit in der Zeiteinheit, die beim Erstellen �bergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	boolean check(long actualTime);
}
