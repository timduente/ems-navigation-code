package de.duente.navigation.actions;

import java.util.ArrayList;

/**
 * Diese Klasse enthält eine einzige Liste in der alle Kommandos zwischen
 * gespeichert werden, die zum Arduino geschickt werden.
 * 
 * @author Tim Dünte
 * 
 */

public class ActionList {
	private static ArrayList<Command> actionList = new ArrayList<Command>();

	/**
	 * Löscht alle Elemente in der ActionList.
	 * 
	 */
	public synchronized static void clearActionList() {
		actionList = new ArrayList<Command>();
	}

	/**
	 * Fügt ein IActionobjekt zur Liste hinzu.
	 * 
	 * @param action
	 *            IAction, die hinzugefügt werden soll.
	 */
	public synchronized static void addAction(Command action) {
		for(int i = 0; i< actionList.size();i++){
			if(actionList.get(i).startTimeStamp <= action.startTimeStamp){
				actionList.add(i, action);
				return;
			}
		}
		actionList.add(action);
	}

	/**
	 * Entfernt ein IAction Objekt aus der Liste.
	 * 
	 * @param action
	 *            IAction, die entfernt werden soll.
	 */
	public synchronized static void removeAction(Command action) {
		actionList.remove(action);
	}

	/**
	 * Gibt die Anzahl der Elemente in der ActionList zurück.
	 * 
	 * @return Anzahl der Elemente in der Liste.
	 */
	public static int getSize() {
		return actionList.size();
	}

	/**
	 * Ermoeglicht einen Zugriff über einen Index.
	 * 
	 * @param index
	 *            Position des Elements in der Liste
	 * @return IAction oder null.
	 */
	public synchronized static Command getAction(int index) {
		return actionList.get(index);
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
		for (int i = actionList.size() - 1; i >= 0; i--) {
			Command action = actionList.get(i);
			if (action != null && action.check(actualTime)) {
				commands.append(action.getCommand());
				commands.append(';');
				actionList.remove(i);
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
		for (int i = actionList.size() - 1; i >= 0; i--) {
			Command action = actionList.get(i);
			if (action != null && action.check()) {
				commands.append(action.getCommand());
				commands.append(';');
				actionList.remove(i);
			}
		}
		return commands.toString();
	}
}
