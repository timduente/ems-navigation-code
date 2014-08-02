package de.duente.navigation.study;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

/**Diese Klasse stellt statische Methoden bereit um die letzte Kalibrierung zu speichern und zu laden.
 * 
 * @author Tim Dünte
 *
 */
public class CalibrationSaver {
	public static final String NAME_LAST_CALIBRATION_FILE = "LastCalibration.txt";
	public static final String PATH_TO_LAST_CALIBRATION_FILE = "logs";

	public static void writeCalibrationValues(int[] calibrationValues){
		File path = Environment.getExternalStorageDirectory();
		File pathToLogs = new File(path, PATH_TO_LAST_CALIBRATION_FILE);		
		File calibFile =  new File(pathToLogs, NAME_LAST_CALIBRATION_FILE);
		try {
			FileWriter fileWriter = new FileWriter(calibFile);
			for(int i = 0; i< calibrationValues.length; i++){
				fileWriter.write(calibrationValues[i] + "\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int[] readCalibrationValues(int size){
		int[] calibrationValues = new int[size];
		File path = Environment.getExternalStorageDirectory();
		File pathToLogs = new File(path, PATH_TO_LAST_CALIBRATION_FILE);
		
		File calibFile =  new File(pathToLogs, NAME_LAST_CALIBRATION_FILE);
		try {
			FileReader fileReader = new FileReader(calibFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			for(int i = 0; i< calibrationValues.length; i++){
				
				try {
					calibrationValues[i] = Integer.parseInt(bufferedReader.readLine());
				} catch (NumberFormatException e) {
					calibrationValues[i] = 0;
				} catch (IOException e) {
					calibrationValues[i] = 0;
				}
		
			}
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			//Letzte Kalibrierung konnte nicht geladen werden.
		} 
		return calibrationValues;
	}	
}
