package de.duente.navigation.commands;

/**
 * Diese Klasse ist eine Aktion und enth�lt Informationen zu wann diese
 * ausgef�hrt werden muss.
 * 
 * @author Tim D�nte
 * 
 */
public class Action extends Command {

	/**
	 * 
	 * @param startTimeStamp
	 *            Zeit zu der das Kommando ausgef�hrt werden soll.
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensit�t des Signals
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
