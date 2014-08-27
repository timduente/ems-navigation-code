package de.duente.study;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Das Objekt repräsentiert einen einzelnen aufgenommenen Datensatz.
 * 
 * @author Tim Dünte
 * 
 */
public class TrackingDataObject implements Comparable<TrackingDataObject> {
	float x, y, z;
	private long frameNumber;
	boolean signalOn;
	float yaw;
	private String dateSend;
	private String dateReceive;

	private int mergeCount = 1;

	public TrackingDataObject(float x, float z, float yaw) {
		this.x = x;
		this.y = 0.0f;
		this.z = z;
		this.yaw = yaw;
		this.frameNumber = 0;
		this.signalOn = true;
		this.dateSend = "";
		this.dateReceive = "";
	}

	public TrackingDataObject(float x, float y, float z, long frameNumber,
			float yaw, String dateSend, String dateReceive, boolean signalOn) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.frameNumber = frameNumber;
		this.signalOn = signalOn;
		this.dateSend = dateSend;
		this.dateReceive = dateReceive;
	}

	public TrackingDataObject(float x, float y, float z, long frameNumber,
			float yaw, String dateSend, String dateReceive, boolean signalOn,
			int mergeCount) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.frameNumber = frameNumber;
		this.signalOn = signalOn;
		this.dateSend = dateSend;
		this.dateReceive = dateReceive;
		this.mergeCount = mergeCount;
	}

	public TrackingDataObject(TrackingDataObject t) {
		this.x = t.x;
		this.y = t.y;
		this.z = t.z;
		this.frameNumber = t.frameNumber;
		this.signalOn = t.signalOn;
		this.dateSend = t.dateSend;
		this.dateReceive = t.dateReceive;
		this.mergeCount = t.mergeCount;
	}

	@Override
	public int compareTo(TrackingDataObject other) {
		if (frameNumber < other.frameNumber) {
			return -1;
		} else if (frameNumber > other.frameNumber) {
			return 1;
		} else {
			return 0;
		}
	}

	static ArrayList<TrackingDataObjectList> parseFileIntoSortedTrackingDataObjectList(
			File file) {

		String lastDateSignalOn = "01-01-1970 01:00:00:000";
		String temporaryDateSignalOn = "";

		ArrayList<TrackingDataObjectList> trackingData = new ArrayList<TrackingDataObjectList>();

		TrackingDataObjectList tDOList = null;

		try {

			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			bufferedReader.readLine(); // Read first empty line

			String line;
			String[] data;
			TrackingDataObject dataObject = null;
			while (bufferedReader.ready()) {
				line = bufferedReader.readLine();
				data = line.split(";");

				if (data.length >= 12) {
					if (tDOList == null) {
						tDOList = new TrackingDataObjectList(
								Integer.parseInt(data[2]),
								Integer.parseInt(data[1]),
								Integer.parseInt(data[0]));
					}
					dataObject = new TrackingDataObject(
							Float.parseFloat(data[3]),
							Float.parseFloat(data[4]),
							Float.parseFloat(data[5]), Long.parseLong(data[6]),
							Float.parseFloat(data[7]), data[8], data[9],
							(Integer.parseInt(data[10]) == 1));

					temporaryDateSignalOn = data[11];

					if (!data[11].equals(lastDateSignalOn)
							&& data[11].compareTo(data[8]) < 0) {
						// if(!dataObject.signalOn){
						// System.err.println(dataObject);
						// }
						dataObject.signalOn = true;
						// System.out.println(data[11] + "Verglichen mit: " +
						// data[8]);
					} else {
						// if(dataObject.signalOn){
						// System.err.println(dataObject);
						// }
						dataObject.signalOn = false;
					}

					tDOList.addTDOtoList(dataObject);
				} else {
					if (tDOList != null) {
						lastDateSignalOn = temporaryDateSignalOn;
						tDOList.sort();
						trackingData.add(tDOList);
						tDOList = null;
					}
				}

			}
			bufferedReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Collections.sort(trackingData);
		return trackingData;
	}

	public boolean isPositionValid() {
		return !(this.x == 0.0f && this.y == 0.0f && this.z == 0.0f);
	}

	public TrackingDataObject meanMergeWithOtherTDO(TrackingDataObject other) {
		if (other == null
				|| (!other.isPositionValid() && this.isPositionValid())) {
			return new TrackingDataObject(this);
		} else if (other.isPositionValid() && this.isPositionValid()) {
			return new TrackingDataObject(this.x + other.x, this.y + other.y,
					this.z + other.z, -1, this.yaw + other.yaw, null, null,
					true, this.mergeCount + 1);
		} else {
			return new TrackingDataObject(other);
		}
	}

	public void mean() {
		z = z / mergeCount;
		x = x / mergeCount;
		y = y / mergeCount;
		yaw = yaw / mergeCount;
	}

	public String toString() {
		return "x: " + x + "; z: " + z + "; Signal ON: " + signalOn;
	}

}
