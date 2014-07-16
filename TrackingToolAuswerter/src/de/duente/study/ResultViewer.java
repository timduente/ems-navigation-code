package de.duente.study;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ResultViewer extends JFrame {
	private static final long serialVersionUID = -6272869584209311245L;
	private JButton loadFile = new JButton("Datei öffnen");
	private JCheckBox showSingles = new JCheckBox("Singles anzeigen");
	private JCheckBox showMean = new JCheckBox("Mittelwert anzeigen");
	private JComboBox comboBox = new JComboBox();
	Container cp;
	ArrayList<TrackingDataObject> list2;
	ArrayList<TrackingDataObject> listMean;

	public ResultViewer() {
		super("Result Viewer");
		this.setSize(1024, 768);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		cp = getContentPane();
		cp.setLayout(null);

		loadFile.setBounds(0, 0, 140, 20);
		loadFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"TXT & CSV Files", "txt", "csv");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog((Component) actionEvent
						.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You chose to open this file: "
							+ chooser.getSelectedFile().getName());
					File file = chooser.getSelectedFile();

					processFile(file);
				}

			}

		});
		cp.add(loadFile);
		showSingles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}

		});

		showSingles.setBounds(160, 0, 140, 20);
		cp.add(showSingles);

		showMean.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}

		});

		showMean.setBounds(320, 0, 140, 20);
		cp.add(showMean);

	}

	public static void main(String... args) {
		ResultViewer resultViewer = new ResultViewer();
	}

	public void processFile(File file) {
		ArrayList<TrackingDataObject> list = TrackingDataObject
				.parseFileIntoSortedTrackingDataObjectList(file);
		list2 = list;
		// for (int i = 0; i < list.size(); i++) {
		// System.out.println("Intensität: "
		// + list.get(i).intensity + "; FrameNumber: "
		// + list.get(i).frameNumber);
		// }
		TrackingDataObject t;
		boolean b = true;
		for (int i = 0; i < list.size(); i++) {
			t = list.get(i);

			if (b && t.signalOn) {
				float xOffset = t.x;
				b = false;
				TrackingDataObject other;
				for (int j = 0; j < list.size(); j++) {
					other = list.get(j);
					if (t.count == other.count) {
						other.x = other.x - xOffset;
					}
				}
			}
			if (!t.signalOn) {
				b = true;
			}
		}

		listMean = new ArrayList<TrackingDataObject>();

		for (int j = -3; j < 4; j++) {
			ArrayList<TrackingDataObject> sum = new ArrayList<TrackingDataObject>();
			int lastCount = -4;
			

			for (int i = 0, sumIndex = 0; i < list.size(); i++) {
				t = list.get(i);

				if (t.intensity == j && t.signalOn) {
					lastCount =-2;
				}

					if (lastCount == -2) {
						while (sumIndex <150) {
							TrackingDataObject u = new TrackingDataObject(t);
							u.count = 1;
							sum.add(u);
							i++;
							t = list.get(i);
							sumIndex++;
						}

						System.out.println("Size of SumList: " + sum.size()
								+ " i: " + i);
						lastCount = -3;
					} else if (lastCount == -1) {

						if (sumIndex >= sum.size()) {
							TrackingDataObject u = new TrackingDataObject(t);
							sum.add(u);

							u.count = 1;
						} else {
							TrackingDataObject u = sum.get(sumIndex);
							u.x = u.x + t.x;
							u.z = u.z + t.z;
							u.count++;

						}
						System.out.println("2. Size of SumList: " + sum.size()
								+ " sumIndex: " + sumIndex);
						sumIndex++;
						
						if(sumIndex == 150){
							lastCount = -1;
							sumIndex = 0;
						}
					
//				} else if (t.intensity == j && !t.signalOn
//						&& (lastCount == -3 || lastCount == -1)) {
//					lastCount = -1;
//					sumIndex = 0;
				}
			}

			for (int i = 0; i < sum.size(); i++) {
				sum.get(i).x = sum.get(i).x / (float) sum.get(i).count;
				sum.get(i).z = sum.get(i).z / (float) sum.get(i).count;
				listMean.add(sum.get(i));
			}
			System.out.println("Size of SumList: " + sum.size());

		}

		repaint();
		System.out.println(list.size());

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.white);
		g.fillRect(0, 100, getWidth(), getHeight());

		g.setColor(Color.black);

		g.drawLine(200, 100, 200, getHeight());

		if (list2 != null && showSingles.isSelected()) {
			TrackingDataObject t;
			for (int i = 0; i < list2.size(); i++) {
				t = list2.get(i);
				switch (t.intensity) {
				case (-3):
					g.setColor(Color.BLUE);
					break;
				case (-2):
					g.setColor(Color.CYAN);
					break;
				case (-1):
					g.setColor(Color.GREEN);
					break;
				case (0):
					g.setColor(Color.BLACK);
					break;
				case (1):
					g.setColor(Color.ORANGE);
					break;
				case (2):
					g.setColor(Color.RED);
					break;
				case (3):
					g.setColor(Color.MAGENTA);
					break;

				}

				g.drawLine((int) (list2.get(i).x * 100.0f + 200),
						(int) (list2.get(i).z * 100.0f) + 100,
						(int) (list2.get(i).x * 100.0f) + 200,
						(int) (list2.get(i).z * 100.0f) + 100);
			}
		}

		if (listMean != null && showMean.isSelected()) {
			TrackingDataObject t;
			boolean signalEnd = false;
			for (int i = 0; i < listMean.size(); i++) {
				t = listMean.get(i);
				switch (t.intensity) {
				case (-3):
					g.setColor(Color.BLUE);
					break;
				case (-2):
					g.setColor(Color.CYAN);
					break;
				case (-1):
					g.setColor(Color.GREEN);
					break;
				case (0):
					g.setColor(Color.BLACK);
					break;
				case (1):
					g.setColor(Color.ORANGE);
					break;
				case (2):
					g.setColor(Color.RED);
					break;
				case (3):
					g.setColor(Color.MAGENTA);
					break;

				}
				if(signalEnd && listMean.get(i).signalOn){
					signalEnd = false;
				}
				
				if(!listMean.get(i).signalOn && !signalEnd){
					signalEnd = true;
					g.setColor(Color.DARK_GRAY);
					
					g.fillRect((int) (listMean.get(i).x * 100.0f) + 198,
							(int) (listMean.get(i).z * 100.0f) + 98, 5, 5);
				}
				

				
				g.fillRect((int) (listMean.get(i).x * 100.0f) + 199,
						(int) (listMean.get(i).z * 100.0f) + 99, 3, 3);
			}
		}

	}

}
