package de.duente.navigation.commands;

/**
 * Abstrakte Oberklasse f�r alle Kommandos und Optionen. Beeinhaltet Informationen zum Start und den Befehl.
 * 
 * @author Tim D�nte
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
	 * Pr�ft ob das Kommando gestartet werden muss. Nimmt die Systemzeit
	 * System.currentTimeMillis() als Referenz zum Pr�fen.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check() {
		return System.currentTimeMillis() >= startTimeStamp;
	}	

	/**
	 * Gibt das Kommando dieser Aktion zur�ck.
	 * 
	 * @return Kommando
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Pr�ft ob das Kommando gestartet werden muss.
	 * 
	 * @param actualTime
	 *            Aktuelle Zeit in der Zeiteinheit, die beim Erstellen �bergeben
	 *            wurde.
	 * 
	 * @return true, wenn das Kommando gestartet werden muss.
	 */
	public boolean check(long actualTime) {
		return actualTime >= startTimeStamp;
	}
}
