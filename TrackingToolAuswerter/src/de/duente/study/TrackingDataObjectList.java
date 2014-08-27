package de.duente.study;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Dieses Objekt repräsentiert eine Liste von geparsten Objekten. Alle Objekte
 * einer Liste haben die gleiche Id und den gleichen count Wert. Jede Liste
 * gehört zu einem Gehversuch.
 * 
 * @author Tim Dünte
 * 
 */
public class TrackingDataObjectList implements
		Comparable<TrackingDataObjectList> {
	private int id;
	private int count;
	private int participantId;
	private int specialPaintIndex = 0;

	private ArrayList<TrackingDataObject> dataList;
	
	public int getID(){
		return id;
	}
	
	public int getCount(){
		return count;
	}

	public TrackingDataObjectList(int id, int count, int participantId) {
		this.id = id;
		this.count = count;
		this.participantId = participantId;
		dataList = new ArrayList<TrackingDataObject>();
	}

	public void addTDOtoList(TrackingDataObject tDO) {
		dataList.add(tDO);
	}

	public ArrayList<Vector> getVectors() {
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		boolean filterForBaseLines = false;
		for (int i = 0; i < dataList.size() - 1;) {
			if ((dataList.get(i).signalOn == false && !filterForBaseLines)
					|| !dataList.get(i).isPositionValid()) {
				i++;
				continue;
			} else {
				if (id == 0 || id == -1) {
					filterForBaseLines = true;
				}
				int j = i + 1;
				// Nächsten gültigen Punkt finden
				for (; j < dataList.size()
						&& !dataList.get(j).isPositionValid(); j++) {

				}
				// Damit dann der Differenzvector gebildet werden kann.
				if (j < dataList.size()) {
					vectors.add(new Vector(dataList.get(i).x,
							dataList.get(i).z, dataList.get(j).x, dataList
									.get(j).z));
				}
				// Damit mit dem nächsten Winkel weiter gemacht werden kann.
				// System.out.println("j: " + j + " Size: "+ dataList.size());
				i = j;
			}
		}
		return vectors;
	}

	@Override
	public int compareTo(TrackingDataObjectList other) {
		if (id < other.id) {
			return -1;
		} else if (id > other.id) {
			return +1;
		} else {
			return 0;
		}
	}

	public void sort() {
		Collections.sort(dataList);
	}

	public void transformCoords() {
		TrackingDataObject t;
		boolean b = true;
		for (int i = 0; i < dataList.size(); i++) {
			t = dataList.get(i);

			if (b && t.signalOn) {
				float xOffset = t.x;
				b = false;
				TrackingDataObject other;
				for (int j = 0; j < dataList.size(); j++) {
					other = dataList.get(j);
					if (other.isPositionValid()) {
						other.x = other.x - xOffset;
					}
				}
				break;
			}
		}

		// Abschneiden aller Werte, bei denen noch kein Signal anlag:
		for (int i = dataList.size() - 1; i >= 0; i--) {
			if (dataList.get(i).x < 0.0f) {
				dataList.remove(i);
			}
		}

		// Abschneiden von defekten Werten, bei denen die Positionsdaten nicht
		// getrackt wurden und das Signal aus war. Also Startwerte.
		for (int i = dataList.size() - 1; i >= 0; i--) {
			if (!dataList.get(i).isPositionValid() && !dataList.get(i).signalOn) {
				dataList.remove(i);
			}
		}

		for (int i = 0; i < dataList.size(); i++) {
			TrackingDataObject tdo = dataList.get(i);
			if (tdo.isPositionValid()
					&& (tdo.x > 3.0f || tdo.x < 0.0f || tdo.z < 1.0f || tdo.z > 4.5f)) {
				// System.err.println("whats up");
				dataList.subList(i, dataList.size()).clear();
				break;
			}
		}

	}

	public static TrackingDataObjectList generateNMeanFilteredList(
			TrackingDataObjectList dataList, int n) {
		TrackingDataObjectList threeMean = new TrackingDataObjectList(
				dataList.id, dataList.count, dataList.participantId);
		TrackingDataObject tDO;
		for (int i = n / 2; i < dataList.dataList.size() - n / 2; i++) {
			float sumX = 0.0f;
			float sumZ = 0.0f;
			int fail = 0;

			for (int j = -n / 2; j <= n / 2; j++) {
				if (dataList.dataList.get(i + j).isPositionValid()) {
					sumX = sumX + dataList.dataList.get(i + j).x;
					sumZ = sumZ + dataList.dataList.get(i + j).z;
				} else {

					fail++;
					// System.out.println("Fail:" + fail +
					// "Info: "+dataList.dataList.get(i+j).signalOn);
				}
			}
			tDO = new TrackingDataObject(sumX / (n - fail), sumZ / (n - fail),
					0.0f);
			threeMean.addTDOtoList(tDO);
		}

		return threeMean;
	}

	public static TrackingDataObjectList generateSavitzkyGolayFilteredList() {
		return null;
	}

	public static TrackingDataObjectList createMeanList(
			ArrayList<TrackingDataObjectList> otherTDOLists) {
		if (otherTDOLists.size() > 0 && otherTDOLists.get(0) != null) {
			TrackingDataObjectList meanList = new TrackingDataObjectList(
					otherTDOLists.get(0).id, -1,
					otherTDOLists.get(0).participantId);
			for (int i = 0; i < otherTDOLists.size(); i++) {
				meanList = meanList.meanMerge(otherTDOLists.get(i));
			}
			meanList.meanTDOValues();
			return meanList;
		}

		return null;
	}

	private TrackingDataObjectList meanMerge(TrackingDataObjectList otherTDOList) {
		TrackingDataObjectList meanList = new TrackingDataObjectList(id, -1,
				participantId);
		int indexSignalOn1 = dataList.size(), indexSignalOn2 = otherTDOList.dataList
				.size();
		// finde Nullwert in dieser Liste.
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).signalOn) {
				indexSignalOn1 = i;
				break;
			}
		}
		// finde Nullwert in anderer Liste.
		for (int i = 0; i < otherTDOList.dataList.size(); i++) {
			if (otherTDOList.dataList.get(i).signalOn) {
				indexSignalOn2 = i;
				break;
			}
		}

		// System.out.println("Indexe: "+indexSignalOn1 + "; " +indexSignalOn2);

		for (; indexSignalOn1 < dataList.size()
				|| indexSignalOn2 < otherTDOList.dataList.size(); indexSignalOn1++, indexSignalOn2++) {
			if (indexSignalOn1 < dataList.size()
					&& indexSignalOn2 < otherTDOList.dataList.size()) {
				meanList.dataList.add(dataList.get(indexSignalOn1)
						.meanMergeWithOtherTDO(
								otherTDOList.dataList.get(indexSignalOn2)));
			} else if (indexSignalOn1 < dataList.size()
					&& indexSignalOn2 >= otherTDOList.dataList.size()) {
				meanList.dataList.add(dataList.get(indexSignalOn1)
						.meanMergeWithOtherTDO(null));
			} else if (indexSignalOn1 >= dataList.size()
					&& indexSignalOn2 < otherTDOList.dataList.size()) {
				// System.out.println("Hallo");
				meanList.dataList.add(otherTDOList.dataList.get(indexSignalOn2)
						.meanMergeWithOtherTDO(null));
				// System.out.println(meanList.dataList.size());
			}
		}

		return meanList;
	}

	private void meanTDOValues() {
		for (int i = dataList.size() - 1; i >= 0; i--) {
			dataList.get(i).mean();
			if (!dataList.get(i).isPositionValid()) {
				dataList.remove(i);
			}
		}
	}

	public int getSize() {
		return dataList.size();
	}

	public float getAngleOnIndex(int index) {
		if (index >= dataList.size()) {
			specialPaintIndex = dataList.size() - 1;
			return dataList.get(dataList.size() - 1).yaw;
		}
		specialPaintIndex = index;
		return dataList.get(index).yaw;
	}

	public void paint(Graphics g, Color color) {
		int size = 3;
		if (color != null) {
			for (int i = 0; i < dataList.size(); i++) {
				TrackingDataObject tdo = dataList.get(i);

				if (i == specialPaintIndex) {
					g.setColor(Color.GRAY);
					size = 5;
				} else {
					size = 3;
					g.setColor(color);
				}

				g.fillRect((int) (tdo.x * ResultViewer.SCALE_FACTOR)
						+ ResultViewer.X_OFFSET - (int) size / 2,
						(int) (tdo.z * ResultViewer.SCALE_FACTOR)
								+ ResultViewer.Z_OFFSET - (int) size / 2, size,
						size);
				// }
			}
		}
	}
}
