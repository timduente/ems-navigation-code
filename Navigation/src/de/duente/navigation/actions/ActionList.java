package de.duente.navigation.actions;

import java.util.ArrayList;

/**
 * Diese Klasse enth�lt eine einzige Liste in der alle Kommandos zwischen
 * gespeichert werden, die zum Arduino geschickt werden.
 * 
 * @author Tim D�nte
 * 
 */

public class ActionList {
	private static ArrayList<IAction> actionList = new ArrayList<IAction>();

	/**
	 * L�scht alle Elemente in der ActionList.
	 * 
	 */
	public static void clearActionList() {
		actionList = new ArrayList<IAction>();
	}

	/**
	 * F�gt ein IActionobjekt zur Liste hinzu.
	 * 
	 * @param action
	 *            IAction, die hinzugef�gt werden soll.
	 */
	public static void addAction(IAction action) {
		System.out.println("added");
		actionList.add(action);
	}

	/**
	 * Entfernt ein IAction Objekt aus der Liste.
	 * 
	 * @param action
	 *            IAction, die entfernt werden soll.
	 */
	public static void removeAction(IAction action) {
		actionList.remove(action);
	}

	/**
	 * Gibt die Anzahl der Elemente in der ActionList zur�ck.
	 * 
	 * @return Anzahl der Elemente in der Liste.
	 */
	public static int getSize() {
		return actionList.size();
	}

	/**
	 * Ermoeglicht einen Zugriff �ber einen Index.
	 * 
	 * @param index
	 *            Position des Elements in der Liste
	 * @return IAction oder null.
	 */
	public static IAction getAction(int index) {
		return actionList.get(index);
	}

	/**
	 * Gibt einen String zur�ck, der alle Kommandos enth�lt, die gesendet werden
	 * sollen. Entfernt diese Elemente aus der Liste.
	 * 
	 * @param actualTime
	 *            Referenzzeit, wenn nicht System.currentMillis() die
	 *            Referenzzeit ist.
	 * @return String im Format Action1;Action2;Action3;
	 */
	public static String getCommandsToDo(long actualTime) {
		StringBuilder commands = new StringBuilder();
		for (IAction action : actionList) {
			if (action.check(actualTime)) {
				commands.append(action.getCommand());
				commands.append(';');
			}
		}
		return commands.toString();

	}

	/**
	 * Gibt einen String zur�ck, der alle Kommandos enth�lt, die gesendet werden
	 * sollen. Entfernt diese Elemente aus der Liste. Referenzzeit ist
	 * System.getCurrentMillis().
	 * 
	 * @return String im Format Action1;Action2;Action3;
	 */
	public static String getCommandsToDo() {
		StringBuilder commands = new StringBuilder();
		for (int i = actionList.size() - 1; i >= 0; i--) {
			IAction action = actionList.get(i);
			if (action != null && action.check()) {
				commands.append(action.getCommand());
				commands.append(';');
				actionList.remove(i);
			}
		}
		return commands.toString();
	}
}
