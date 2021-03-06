package de.duente.navigation.commands;

import java.io.IOException;

import de.duente.navigation.bluetooth.BluetoothConnector;

/**
 * Die CommandManager Klasse steht verwaltet die Commands in der ActionList.
 * Ueber statische Methoden k�nnen Aktionen hinzugef�gt werden, die dann �ber
 * die Instanz des Kommandmangers zum richtigen Zeitpunkt an den
 * BluetoothConnector zum Senden an das Arduino weitergegeben werden.
 * 
 * @author Tim D�nte
 * 
 */

public class CommandManager implements Runnable {
	/**
	 * Setzt die Steptime fuer einen Kanal.
	 * 
	 * @param channel
	 *            Kanal
	 * 
	 * @param stepTime
	 *            Zeit die gewartet werden soll zwischen zwei Steps beim
	 *            schrittweisen Incrementieren oder Decrementieren des dig.
	 *            Potentiometers.
	 */
	public static void setStepTimeForChannel(int channel, int stepTime) {
		CommandList.addCommand(new Option(System.currentTimeMillis(),
				Option.OPTION.SET_CHANGE_TIME, channel, stepTime));
	}

	/**
	 * Setzt die maximale Intensitaet fuer einen Kanal
	 * 
	 * @param channel
	 *            Kanal
	 * @param max
	 *            maximale Intensitaet in Prozent 0-100
	 */
	public static void setMaxIntensityForChannel(int channel, int max) {
		CommandList.addCommand(new Option(System.currentTimeMillis(),
				Option.OPTION.SET_CALIBRATION_MAXIMUM, channel, max));
	}

	/**
	 * Setzt die minimale Intensitaet fuer einen Kanal
	 * 
	 * @param channel
	 *            Kanal
	 * @param min
	 *            minimale Intensitaet in Prozent 0-100
	 */
	public static void setMinIntensityForChannel(int channel, int min) {
		CommandList.addCommand(new Option(System.currentTimeMillis(),
				Option.OPTION.SET_CALIBRATION_MINIMUM, channel, min));
	}

	/**
	 * Erstellt einen Impuls auf einem bestimmten Kanal mit einer Intensitaet
	 * und Dauer.
	 * 
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensitaet im Moment 0-127
	 * @param onTime
	 *            Dauer des Impulses in Millisekunden
	 */
	public static void setIntensityForTime(int channel, int intensity,
			int onTime) {
		CommandList.addCommand(new Action(System.currentTimeMillis(), channel,
				intensity, onTime));
	}

	/**
	 * L�sst das Signal auf dem Kanal sofort ausgehen.
	 * 
	 * @param channel
	 *            Kanal
	 */
	public static void stopSignal(int channel) {
		CommandList.addCommand(new Action(System.currentTimeMillis(), channel, 0,
				0));
	}

	/**
	 * Erstellt einen Impuls auf einem bestimmten Kanal mit einer Intensitaet
	 * und Dauer.
	 * 
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensitaet im Moment 0-127
	 * @param onTime
	 *            Dauer des Impulses in Millisekunden
	 * @param startTime
	 *            Startzeit des Impulses
	 */
	@Deprecated
	public static void setIntensityForTime(int channel, int intensity,
			long startTime, int onTime) {
		CommandList.addCommand(new Action(startTime, channel, intensity, onTime));
	}

	/**
	 * Konstruiert einen Signal, das aus mehreren gleich langen Impulsen
	 * besteht.
	 * 
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensitaet im Moment 0-127
	 * @param startTime
	 *            Startzeit in bezug zur Systemzeit
	 * @param count
	 *            Anzahl der Wellen
	 * @param highTime
	 *            L�nge des Highsignals
	 * @param lowTime
	 *            L�nge des Lowsignals
	 */
	public static void setPulseForTime(int channel, int intensity,
			long startTime, int count, int highTime, int lowTime) {
		for (int i = 0; i < count; i++) {
			CommandList.addCommand(new Action(startTime + i
					* (highTime + lowTime), channel, intensity, highTime));
		}
	}

	private boolean run;
	private BluetoothConnector bluetoothConnector;

	/**
	 * Erstellt einen CommandManager, der die Kommandos an einen
	 * BluetoothConnector sendet.
	 * 
	 * @param bluetoothConnector
	 *            BluetoothConnector, an den die Kommandos gesendet werden.
	 */
	public CommandManager(BluetoothConnector bluetoothConnector) {
		this.bluetoothConnector = bluetoothConnector;
	}

	/**
	 * Hier werden alle Actions �berpr�ft, ob sie ausgef�hrt werden und
	 * gegebenfalls an den BluetoothConnector weitergegeben.
	 * 
	 */

	@Override
	public void run() {
		run = true;
		while (run) {
			try {
				// Hole Kommandos, die gesendet werden m�ssen
				String commands = CommandList.getCommandsToDo();
//				System.out.println(commands);
				if (commands.length() > 0) {
					bluetoothConnector.sendText(commands);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// F�hre diese aus.

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stoppt den Thread.
	 */
	public void stop() {
		run = false;
	}

}
