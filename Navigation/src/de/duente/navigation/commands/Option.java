package de.duente.navigation.commands;

/**
 * Diese Klasse repraesentiert eine Option. Optionen haben laenger Kommandos als
 * Aktionen
 * 
 * @see Action
 * 
 * @author Tim Dünte
 * 
 */
public class Option extends Command {

	/**
	 * Erstellt eine Option mit einer Startzeit, einer Option, zu einem Kanal
	 * und einen Wert
	 * 
	 * @param startTimeStamp
	 *            Zeitpunkt zu dem die Option gesendet werden soll
	 * @param option
	 *            Option {@link OPTION}
	 * @param channel
	 *            Kanal
	 * @param value
	 *            Wert für die Option
	 */
	public Option(long startTimeStamp, OPTION option, int channel, int value) {
		super(startTimeStamp, "_" + option.option() + "[" + channel + ","
				+ value + "]");
	}
	
	/**
	 * In dieser Enumeration werden alle Optionen deklariert.
	 * 
	 * @author Tim Dünte
	 * 
	 */
	public enum OPTION {
		SET_DEVICE("De"), SET_CHANGE_TIME("CT"), SET_CALIBRATION_MINIMUM("MI"), SET_CALIBRATION_MAXIMUM(
				"MA");

		private final String option;

		private OPTION(String option) {
			this.option = option;
		}

		public String option() {
			return option;
		}
	}
}
