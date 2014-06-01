package de.duente.navigation.actions;

import java.io.IOException;

import de.duente.navigation.BluetoothConnector;

/**
 * Die CommandManager Klasse steht verwaltet die Actions in der ActionList.
 * Ueber statische Methoden können Aktionen hinzugefügt werden, die dann über
 * die Instanz des Kommandmangers zum richtigen Zeitpunkt an den
 * BluetoothConnector zum Senden an das Arduino weitergegeben werden.
 * 
 * @author Tim Dünte
 * 
 */

public class CommandManager implements Runnable {

	/**
	 * Erstellt einen Impuls auf einem bestimmten Kanal mit einer Intensitaet
	 * und Dauer.
	 * 
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensitaet im Moment 0-127
	 * @param time
	 *            Dauer des Impulses in Millisekunden
	 * @param stepTime
	 *            Zeit die gewartet werden soll zwischen zwei Steps beim
	 *            schrittweisen Incrementieren oder Decrementieren des dig.
	 *            Potentiometers.
	 */
	public static void setIntensityForTime(int channel, int intensity,
			int time, int stepTime) {
		ActionList.addAction(new Action(System.currentTimeMillis(), "I"
				+ intensity + "C" + channel + "S" + stepTime + "T" + time));
	}

	/**
	 * Erstellt einen Impuls auf einem bestimmten Kanal mit einer Intensitaet
	 * und Dauer.
	 * 
	 * @param channel
	 *            Kanal
	 * @param intensity
	 *            Intensitaet im Moment 0-127
	 * @param time
	 *            Dauer des Impulses in Millisekunden
	 * @param startTime
	 *            Startzeit des Impulses
	 * @param stepTime
	 *            Zeit die gewartet werden soll zwischen zwei Steps beim
	 *            schrittweisen Incrementieren oder Decrementieren des dig.
	 *            Potentiometers.
	 */
	public static void setIntensityForTime(int channel, int intensity,
			long startTime, int time, int stepTime) {
		ActionList.addAction(new Action(startTime, "I" + intensity + "C"
				+ channel + "S" + stepTime + "T" + time));
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
	 *            Länge des Highsignals
	 * @param lowTime
	 *            Länge des Lowsignals
	 * @param stepTime
	 *            Zeit die gewartet werden soll zwischen zwei Steps beim
	 *            schrittweisen Incrementieren oder Decrementieren des dig.
	 *            Potentiometers.
	 */

	public static void setPulseForTime(int channel, int intensity,
			long startTime, int count, long highTime, long lowTime, int stepTime) {
		for (int i = 0; i < count; i++) {
			ActionList.addAction(new Action(startTime + i
					* (highTime + lowTime), "I" + intensity + "C" + channel
					+ "S" + stepTime + "T" + highTime));
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
	 * Hier werden alle Actions überprüft, ob sie ausgeführt werden und
	 * gegebenfalls an den BluetoothConnector weitergegeben.
	 * 
	 */

	@Override
	public void run() {
		run = true;
		while (run) {
			try {
				// Hole Kommandos, die gesendet werden müssen
				String commands = ActionList.getCommandsToDo();
				System.out.println(commands);
				if (commands.length() > 0) {
					bluetoothConnector.sendText(commands);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Führe diese aus.

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stopt den Thread.
	 */
	public void stop() {
		run = false;
	}

}
