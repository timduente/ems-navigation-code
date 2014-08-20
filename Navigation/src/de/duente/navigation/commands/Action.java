package de.duente.navigation.commands;

/**
 * Diese Klasse ist eine Aktion und enthält Informationen zu wann diese
 * ausgeführt werden muss.
 * 
 * @author Tim Dünte
 * 
 */
public class Action extends Command {

	/**
	 * 
	 * @param startTimeStamp
	 *            Zeit zu der das Kommando ausgeführt werden soll.
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensität des Signals
	 * @param onTime
	 *            Zeit, die das Signal anstehen soll
	 */
	public Action(long startTimeStamp, int channel, int intensity, int onTime) {
		super(startTimeStamp, "I" + intensity + "C" + channel + "T" + onTime);
	}
	
	public Action(){
		super(System.currentTimeMillis(), ";");
	}
}
