package de.duente.navigation;

import java.util.ArrayList;

import android.location.Location;

public class Route {
	private ArrayList<Step> stepsToDo;
	private int actWayPoint;
	
	public static double MIN_DISTANCE_TO_WAYPOINT = 20.0;

	public Route() {
		stepsToDo = new ArrayList<Step>();
		actWayPoint = -1;
	}

	/**
	 * @return the actWayPoint
	 */
	public int getActWayPoint() {
		return actWayPoint;
	}

	/**
	 * @param actWayPoint
	 *            the actWayPoint to set
	 */
	public void setActWayPoint(int actWayPoint) {
		this.actWayPoint = actWayPoint;
	}

	public void addStep(Step step) {
		stepsToDo.add(step);
		if (stepsToDo.size() == 1) {
			actWayPoint = 0;
		}
	}

	public int getSize() {
		return stepsToDo.size();
	}

	public Step getStep(int index) {
		if (isIndexInRange(index)) {
			return stepsToDo.get(index);
		} else {
			return null;
		}
	}

	public Step getActStep() {
		if (isIndexInRange(actWayPoint)) {
			return stepsToDo.get(actWayPoint);
		} else {
			return null;
		}
	}

	public void printMe() {
		for (int i = 0; i < stepsToDo.size(); i++) {
			System.out.println(i + ". " + stepsToDo.get(i).toString());
		}
	}

	/**
	 * Updated die Route. Welcher Schritt als nächstes gemacht werden muss.
	 * 
	 * @param updateLocation
	 *            neue Location an hand derer die Position auf der Route
	 *            bestimmt wird.
	 */
	public void updateNextWayPoint(Location updateLocation) {
		double minDistance = -1.0;
		int step = 0;
		for (int i = actWayPoint; i < stepsToDo.size(); i++) {
			Location stepStartLocation = new Location(updateLocation);
			double iDistance = 0.0;

			stepStartLocation.setLatitude(stepsToDo.get(i).getStart().latitude);
			stepStartLocation
					.setLongitude(stepsToDo.get(i).getStart().longitude);
			iDistance = stepStartLocation.distanceTo(updateLocation);

			if (minDistance <= 0 || minDistance >= iDistance) {
				minDistance = iDistance;
				step = i;
			}
		}
		if (minDistance < MIN_DISTANCE_TO_WAYPOINT) {
			actWayPoint = step;
		}
	}

	/**Gibt die Entfernung zum Endpunkt des Steps zurück
	 * 
	 * @param step Index des Steps
	 * @param location Ort zu dem die Distanz berechnet werden soll
	 * @return Entfernung in Metern.
	 */
	public double getDistanceToStep(int step, Location location) {		
		double distance = -1.0;
		if (isIndexInRange(step)) {
			Location stepLocation = new Location(location);
			stepLocation.setLatitude(stepsToDo.get(step).getEnd().latitude);
			stepLocation.setLongitude(stepsToDo.get(step).getEnd().longitude);
			distance = stepLocation.distanceTo(location);
		}
		return distance;
	}

	private boolean isIndexInRange(int index) {
		return (index >= 0 && index < stepsToDo.size());
	}

}
