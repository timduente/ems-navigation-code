package de.duente.navigation.actions;

/**
 * Diese Klasse ist eine Aktion und enthält Informationen zu wann diese
 * ausgeführt werden muss.
 * 
 * @author Tim Dünte
 * 
 */
public class Action implements IAction{
	long startTimeStamp;

	String command;
	

	/**
	 * Erstellt ein Aktionsobjekt. Dieses enthält die StartZeit und das
	 * Kommando.
	 * 
	 * @param startTimeStamp
	 *            Zeit zu der das Kommando ausgeführt werden soll.
	 * @param command
	 *            Kommando, das ausgeführt werden soll.
	 */
	public Action(long startTimeStamp, String command) {
		this.command = command;
		this.startTimeStamp = startTimeStamp;
	}
	

	/**
	 * Prüft ob das Kommando gestartet werden muss. Nimmt die Systemzeit
	 * System.currentTimeMillis() als Referenz zum Prüfen.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	@Override
	public boolean check() {
		return System.currentTimeMillis() >= startTimeStamp;
	}

	/**
	 * Prüft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            aktuelle Zeit in der Zeiteinheit, die beim Erstellen übergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	@Override
	public boolean check(long actualTime) {
		return actualTime >= startTimeStamp;
	}

	/**
	 * Gibt das Kommando dieser Aktion zurück.
	 * 
	 * @return Kommando
	 */
	@Override
	public String getCommand() {
		return command;
	}

}
