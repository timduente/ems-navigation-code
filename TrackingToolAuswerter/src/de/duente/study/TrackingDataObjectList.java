package de.duente.study;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

public class TrackingDataObjectList implements
		Comparable<TrackingDataObjectList> {
	int id;
	int count;
	int participantId;

	ArrayList<TrackingDataObject> dataList;

	public TrackingDataObjectList(int id, int count, int participantId) {
		this.id = id;
		this.count = count;
		this.participantId = participantId;
		dataList = new ArrayList<TrackingDataObject>();
	}

	public void addTDOtoList(TrackingDataObject tDO) {
		dataList.add(tDO);
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
	}

	public static TrackingDataObjectList createMeanList(
			ArrayList<TrackingDataObjectList> otherTDOLists) {
		if (otherTDOLists.size() > 0 && otherTDOLists.get(0) != null) {
			TrackingDataObjectList meanList = new TrackingDataObjectList(otherTDOLists.get(0).id,
					-1, otherTDOLists.get(0).participantId);
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
		
//		System.out.println("Indexe: "+indexSignalOn1 + "; " +indexSignalOn2);

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
//				System.out.println("Hallo");
				meanList.dataList.add(otherTDOList.dataList.get(indexSignalOn2)
						.meanMergeWithOtherTDO(null));
//				System.out.println(meanList.dataList.size());
			}
		}
		
		return meanList;
	}
	
	private void meanTDOValues(){
		for(int i = 0; i< dataList.size(); i++){
			dataList.get(i).mean();
		}
	}

	public void paint(Graphics g, Color color) {
		if (color != null) {
			for (int i = 0; i < dataList.size(); i++) {
				TrackingDataObject tdo = dataList.get(i);
				g.setColor(color);
				g.fillRect((int) (tdo.x * ResultViewer.SCALE_FACTOR)
						+ ResultViewer.X_OFFSET - 1,
						(int) (tdo.z * ResultViewer.SCALE_FACTOR)
								+ ResultViewer.Z_OFFSET - 1, 3, 3);
			}
		}
	}
}
