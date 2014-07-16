package de.duente.study;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TrackingDataObject implements Comparable<TrackingDataObject>{
	float x, y, z;
	long frameNumber;
	int intensity, count;
	boolean signalOn;
	String dateSend;
	String dateReceive;
	
	public TrackingDataObject(int intensity, int count, float x, float y, float z, long frameNumber,String dateSend, String dateReceive, boolean signalOn ){
		this.x = x;
		this.y = y;
		this.z = z;
		this.count = count;
		this.frameNumber = frameNumber;
		this.intensity = intensity;
		this.signalOn = signalOn;
		this.dateSend = dateSend;
		this.dateReceive = dateReceive;
	}
	
	public TrackingDataObject(TrackingDataObject t){
		this.x = t.x;
		this.y = t.y;
		this.z = t.z;
		this.count = t.count;
		this.frameNumber = t.frameNumber;
		this.intensity = t.intensity;
		this.signalOn = t.signalOn;
		this.dateSend = t.dateSend;
		this.dateReceive = t.dateReceive;
	}
	
	@Override
	public int compareTo(TrackingDataObject other) {
		if(intensity < other.intensity){
			return -1;
		}else if(intensity > other.intensity){
			return +1;
		}
		else{
			if(frameNumber < other.frameNumber){
				return -1;
			}else if(frameNumber > other.frameNumber){
				return 1;
			}else{
				return 0;
			}
		}
	}
	
	static ArrayList<TrackingDataObject> parseFileIntoSortedTrackingDataObjectList(File file){
		ArrayList<TrackingDataObject> trackingData = new ArrayList<TrackingDataObject>();
		
		try {
			
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			bufferedReader.readLine(); //Read first empty line
			
			String line;
			String[] data;
			while(bufferedReader.ready()){
				line = bufferedReader.readLine();
				data = line.split(";");
				if(data.length >= 11){
					TrackingDataObject dataObject = new TrackingDataObject(Integer.parseInt(data[2]), Integer.parseInt(data[1]), Float.parseFloat(data[3]) , Float.parseFloat(data[4]) , Float.parseFloat(data[5]), Long.parseLong(data[6]), data[8], data[9], (Integer.parseInt(data[10]) == 1));
					trackingData.add(dataObject);
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

	
}
