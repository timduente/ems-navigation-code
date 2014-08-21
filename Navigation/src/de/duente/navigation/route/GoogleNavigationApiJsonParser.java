package de.duente.navigation.route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.duente.navigation.ShowPosition;

/**
 * Diese Klasse parst den JSON String aus der Google Api wandelt den JSON-String
 * in ein JSON-Objekt um und kann aus diesem eine Route erstellen. Eventuell
 * kann zu Erweiterungszwecken noch auf das JSON Objekt zugegriffen werden.
 * 
 * @author Tim Dünte
 * 
 */

public class GoogleNavigationApiJsonParser {
	private final static String JSON_ENTRY_STATUS = "status";
	private final static String JSON_ENTRY_ROUTES = "routes";
	private final static String JSON_ENTRY_COPYRIGHTS = "copyrights";
	private final static String JSON_ENTRY_LEGS = "legs";
	private final static String JSON_ENTRY_STARTADDRESS = "start_address";
	private final static String JSON_ENTRY_ENDADDRESS = "end_address";
	private final static String JSON_ENTRY_STARTLOCATION = "start_location";
	private final static String JSON_ENTRY_ENDLOCATION = "end_location";
	private final static String JSON_ENTRY_STEPS = "steps";
	private final static String JSON_ENTRY_MANEUVER = "maneuver";
	private final static String JSON_ENTRY_LATITUDE = "lat";
	private final static String JSON_ENTRY_LONGITUDE = "lng";
	private final static String JSON_ENTRY_HTMLINSTRUCTIONS = "html_instructions";

	private static String regex = "(.+),(.+),(.+),(.+),(.+)";
	private static Pattern pattern = Pattern.compile(regex);

	private static String regex2 = "(.+),(.+),(.+),(.+)";
	private static Pattern pattern2 = Pattern.compile(regex2);

	private JSONObject jsonObj;

	/**
	 * Erstellt ein neues Parser Objekt.
	 * 
	 * @param jsonString
	 *            JSON Objekt als String
	 * @throws JSONException
	 *             falls der String ungültig ist.
	 */
	public GoogleNavigationApiJsonParser(String jsonString)
			throws JSONException {
		jsonObj = new JSONObject(jsonString);
	}

	/**
	 * Holt alle Informationen aus dem JSON Objekt und erstellt ein Routeobjekt
	 * daraus.
	 * 
	 * @return Route, die der Anfrage entspricht.
	 * @throws JSONException
	 *             falls das Parsen fehlschlägt.
	 */
	public Route parseJsonStringInRoute() throws JSONException {
		Route route = new Route();

//		System.out.println("Status: " + jsonObj.getString(JSON_ENTRY_STATUS));
		// Status kann auf NOT_FOUND oder OK überprüft werden.

		JSONArray routes = jsonObj.getJSONArray(JSON_ENTRY_ROUTES);
		JSONObject googleRoute = (JSONObject) routes.get(0);
		// Länge von JSON Array kann abgefragt werden.

//		System.out.println("CopyRights: "
//				+ googleRoute.get(JSON_ENTRY_COPYRIGHTS));

		JSONArray legs = googleRoute.getJSONArray(JSON_ENTRY_LEGS);
		// Da wir nur eine Route wollen nehmen wir leg 0
		JSONObject leg = legs.getJSONObject(0);

		route.setStartLocation(removePlaceInformationFromAddress(leg
				.getString(JSON_ENTRY_STARTADDRESS)));
		route.setEndLocation(removePlaceInformationFromAddress(leg
				.getString(JSON_ENTRY_ENDADDRESS)));

		JSONObject startLocation = leg.getJSONObject(JSON_ENTRY_STARTLOCATION);
		JSONObject endLocation = leg.getJSONObject(JSON_ENTRY_ENDLOCATION);
//		System.out.println("Start Location: " + startLocation.toString()
//				+ "\nEndLocation: " + endLocation.toString());
		JSONArray steps = leg.getJSONArray(JSON_ENTRY_STEPS);

		// text = "\nStart: " + startLocation.toString() + "\n";
		for (int i = 0; i < steps.length(); i++) {
			JSONObject step = steps.getJSONObject(i);
			/*
			 * text = text + i + ". START " +
			 * step.getJSONObject("start_location").toString() + "ENDE: " +
			 * step.getJSONObject("end_location").toString() + "\n";
			 */
			// step.get
			JSONObject strloc = step.getJSONObject(JSON_ENTRY_STARTLOCATION);
			JSONObject endloc = step.getJSONObject(JSON_ENTRY_ENDLOCATION);
			String maneuver = new String();

			// Wenn es kein maneuver gibt, muss man weiter der route
			// folgen.
			if (step.has(JSON_ENTRY_MANEUVER)) {
				maneuver = step.getString(JSON_ENTRY_MANEUVER);
			} else {
				maneuver = ShowPosition.MANEUVER_FOLLOW;
			}
			Step stepi = new Step(new GeoPoint(
					strloc.getDouble(JSON_ENTRY_LATITUDE),
					strloc.getDouble(JSON_ENTRY_LONGITUDE)), new GeoPoint(
					endloc.getDouble(JSON_ENTRY_LATITUDE),
					endloc.getDouble(JSON_ENTRY_LONGITUDE)), maneuver);
			stepi.setHtmlInstruction(step
					.getString(JSON_ENTRY_HTMLINSTRUCTIONS));
			route.addStep(stepi);
			
			// text = text + "\n Tot. Ende: " + endLocation.toString();
		}
		GeoPoint finish = route.getStep(route.getSize() - 1).getEnd();
		route.addStep(new Step(finish, finish, ShowPosition.MANEUVER_FINISH));
		return route;
	}

	private String removePlaceInformationFromAddress(String address) {

		Matcher matcher = pattern.matcher(address);
		Matcher matcher2 = pattern2.matcher(address);
		 if (matcher.matches()) {
			//System.out.println(address + " MATCHES");

			return matcher.group(1) + "," + matcher.group(4) + ","
					+ matcher.group(5);

		} else if (matcher2.matches()) {
			//System.out.println(address + " MATCHES2");
			return matcher2.group(1) + "," + matcher2.group(3) + ","
					+ matcher2.group(4);
		} else {
			return address;
		}
	}

}
