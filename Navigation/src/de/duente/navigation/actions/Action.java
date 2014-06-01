package de.duente.navigation.actions;

/**
 * Diese Klasse ist eine Aktion und enth�lt Informationen zu wann diese
 * ausgef�hrt werden muss.
 * 
 * @author Tim D�nte
 * 
 */
public class Action implements IAction{
	long startTimeStamp;

	String command;
	

	/**
	 * Erstellt ein Aktionsobjekt. Dieses enth�lt die StartZeit und das
	 * Kommando.
	 * 
	 * @param startTimeStamp
	 *            Zeit zu der das Kommando ausgef�hrt werden soll.
	 * @param command
	 *            Kommando, das ausgef�hrt werden soll.
	 */
	public Action(long startTimeStamp, String command) {
		this.command = command;
		this.startTimeStamp = startTimeStamp;
	}
	

	/**
	 * Pr�ft ob das Kommando gestartet werden muss. Nimmt die Systemzeit
	 * System.currentTimeMillis() als Referenz zum Pr�fen.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	@Override
	public boolean check() {
		return System.currentTimeMillis() >= startTimeStamp;
	}

	/**
	 * Pr�ft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            aktuelle Zeit in der Zeiteinheit, die beim Erstellen �bergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	@Override
	public boolean check(long actualTime) {
		return actualTime >= startTimeStamp;
	}

	/**
	 * Gibt das Kommando dieser Aktion zur�ck.
	 * 
	 * @return Kommando
	 */
	@Override
	public String getCommand() {
		return command;
	}

}
