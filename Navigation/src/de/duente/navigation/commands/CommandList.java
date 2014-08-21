package de.duente.navigation.commands;

import java.util.ArrayList;

/**
 * Diese Klasse enthält eine einzige Liste in der alle Kommandos zwischen
 * gespeichert werden, die zum Arduino geschickt werden.
 * 
 * @author Tim Dünte
 * 
 */

public class CommandList {
	private static ArrayList<Command> commandList = new ArrayList<Command>();

	/**
	 * Löscht alle Elemente in der CommandList.
	 * 
	 */
	public synchronized static void clearCommandList() {
		commandList = new ArrayList<Command>();
	}

	/**
	 * Fügt ein Command OPbjekt zur Liste hinzu.
	 * 
	 * @param command
	 *            Command, die hinzugefügt werden soll.
	 */
	public synchronized static void addCommand(Command command) {
		for(int i = 0; i< commandList.size();i++){
			if(commandList.get(i).startTimeStamp <= command.startTimeStamp){
				commandList.add(i, command);
				return;
			}
		}
		commandList.add(command);
	}

	/**
	 * Entfernt ein Command Objekt aus der Liste.
	 * 
	 * @param command
	 *            Command, die entfernt werden soll.
	 */
	public synchronized static void removeCommand(Command command) {
		commandList.remove(command);
	}

	/**
	 * Gibt die Anzahl der Elemente in der CommandList zurück.
	 * 
	 * @return Anzahl der Elemente in der Liste.
	 */
	public static int getSize() {
		return commandList.size();
	}

	/**
	 * Ermoeglicht einen Zugriff über einen Index.
	 * 
	 * @param index
	 *            Position des Elements in der Liste
	 * @return Command oder null.
	 */
	public synchronized static Command getCommand(int index) {
		return commandList.get(index);
	}

	/**
	 * Gibt einen String zurück, der alle Kommandos enthält, die gesendet werden
	 * sollen. Entfernt diese Elemente aus der Liste.
	 * 
	 * @param actualTime
	 *            Referenzzeit, wenn nicht System.currentMillis() die
	 *            Referenzzeit ist.
	 * @return String im Format Action1;Action2;Action3;
	 */
	public synchronized static String getCommandsToDo(long actualTime) {
		StringBuilder commands = new StringBuilder();
		for (int i = commandList.size() - 1; i >= 0; i--) {
			Command action = commandList.get(i);
			if (action != null && action.check(actualTime)) {
				commands.append(action.getCommand());
				commands.append(';');
				commandList.remove(i);
			}
		}
		return commands.toString();

	}

	/**
	 * Gibt einen String zurück, der alle Kommandos enthält, die gesendet werden
	 * sollen. Entfernt diese Elemente aus der Liste. Referenzzeit ist
	 * System.getCurrentMillis().
	 * 
	 * @return String im Format Action1;Action2;Action3;
	 */
	public synchronized static String getCommandsToDo() {
		StringBuilder commands = new StringBuilder();
		for (int i = commandList.size() - 1; i >= 0; i--) {
			Command action = commandList.get(i);
			if (action != null && action.check()) {
				commands.append(action.getCommand());
				commands.append(';');
				commandList.remove(i);
			}
		}
		return commands.toString();
	}
}
