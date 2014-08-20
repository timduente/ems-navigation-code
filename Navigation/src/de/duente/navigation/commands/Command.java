package de.duente.navigation.commands;

/**
 * Abstrakte Oberklasse für alle Kommandos und Optionen. Beeinhaltet Informationen zum Start und den Befehl.
 * 
 * @author Tim Dünte
 * 
 */

public abstract class Command {
	
	protected long startTimeStamp;

	protected String command;
	
	public Command(long startTimeStamp, String command){
		this.startTimeStamp = startTimeStamp;
		this.command = command;
		
	}
	
	/**
	 * Prüft ob das Kommando gestartet werden muss. Nimmt die Systemzeit
	 * System.currentTimeMillis() als Referenz zum Prüfen.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check() {
		return System.currentTimeMillis() >= startTimeStamp;
	}	

	/**
	 * Gibt das Kommando dieser Aktion zurück.
	 * 
	 * @return Kommando
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Prüft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            Aktuelle Zeit in der Zeiteinheit, die beim Erstellen übergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check(long actualTime) {
		return actualTime >= startTimeStamp;
	}
}
