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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ResultViewer extends JFrame {
	private static final long serialVersionUID = -6272869584209311245L;
	private JButton loadFile = new JButton("Datei öffnen");
	private JCheckBox showSingles = new JCheckBox("Singles anzeigen");
	private JCheckBox showMean = new JCheckBox("Mittelwert anzeigen");

	private JLabel leftText = new JLabel("Links");
	private JLabel rightText = new JLabel("Rechts");

	private String[] text = { "null", "schwach", "mittel", "stark" };
	private JCheckBox[] rightBoxes = new JCheckBox[text.length];
	private JCheckBox[] leftBoxes = new JCheckBox[text.length];

	public final static float SCALE_FACTOR = 150.0f;
	public final static int X_OFFSET = 250;
	public final static int Z_OFFSET = 150;

	private final static int POINT_COUNT = 210;

	private Container cp;
	private ArrayList<TrackingDataObjectList> listSingles;
	private ArrayList<TrackingDataObjectList> listMean;
	private ArrayList<TrackingDataObjectList> filteredList;

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

		ActionListener repaintActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		};

		cp.add(loadFile);
		showSingles.addActionListener(repaintActionListener);

		showSingles.setBounds(160, 0, 140, 20);
		cp.add(showSingles);

		showMean.addActionListener(repaintActionListener);

		showMean.setBounds(160, 20, 140, 20);
		cp.add(showMean);

		leftText.setBounds(320, 0, 100, 20);
		cp.add(leftText);
		rightText.setBounds(420, 0, 100, 20);
		cp.add(rightText);

		for (int i = 0; i < text.length; i++) {
			leftBoxes[i] = new JCheckBox(text[i]);
			leftBoxes[i].setBounds(320, 20 + i * 15, 100, 15);
			leftBoxes[i].setSelected(true);
			leftBoxes[i].addActionListener(repaintActionListener);
			cp.add(leftBoxes[i]);

			rightBoxes[i] = new JCheckBox(text[i]);
			rightBoxes[i].setBounds(420, 20 + i * 15, 100, 15);
			rightBoxes[i].setSelected(true);
			rightBoxes[i].addActionListener(repaintActionListener);
			cp.add(rightBoxes[i]);
		}
	}

	public static void main(String... args) {
		new ResultViewer();
	}

	public void processFile(File file) {
		ArrayList<TrackingDataObjectList> list = TrackingDataObject
				.parseFileIntoSortedTrackingDataObjectList(file);
		listSingles = list;
		for (int i = 0; i < list.size(); i++) {
			list.get(i).transformCoords();
		}

		listMean = new ArrayList<TrackingDataObjectList>();

		for (int j = -4; j < 4; j++) {

			ArrayList<TrackingDataObjectList> sameID = new ArrayList<TrackingDataObjectList>();
			for (int i = 0; i < listSingles.size(); i++) {
				if (listSingles.get(i).id == j) {
					sameID.add(listSingles.get(i));
				}
			}
			System.out.println("id: " + j + "Size von SameID: "+  sameID.size());
			listMean.add(TrackingDataObjectList.createMeanList(sameID));
		}
		repaint();
	}

	private Color chooseColor(int intensity) {
		Color color = null;
		switch (intensity) {
		case (-4):
			if (leftBoxes[3].isSelected()) {
				color = Color.BLUE;
			}
			break;
		case (-3):
			if (leftBoxes[2].isSelected()) {
				color = Color.CYAN;
			}
			break;
		case (-2):
			if (leftBoxes[1].isSelected()) {
				color = Color.GREEN;
			}
			break;
		case (-1):
			if (leftBoxes[0].isSelected()) {
				color = Color.PINK;
			}
			break;
		case (0):
			if (rightBoxes[0].isSelected()) {
				color = Color.BLACK;
			}
			break;
		case (1):
			if (rightBoxes[1].isSelected()) {
				color = Color.ORANGE;
			}
			break;
		case (2):
			if (rightBoxes[2].isSelected()) {
				color = Color.RED;
			}
			break;
		case (3):
			if (rightBoxes[3].isSelected()) {
				color = Color.MAGENTA;
			}
			break;
		}
		return color;
	}

	private double getAngle(TrackingDataObject startData,
			TrackingDataObject endData) {
		return Math.atan2(endData.z - startData.z, endData.x - startData.x)
				* 180.0 / Math.PI;
	}

	private void generateFilteredList() {

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.white);
		g.fillRect(0, 150, getWidth(), getHeight());

		g.setColor(Color.black);
		g.drawLine(0, 160, getWidth(), 160);

		for (int i = -1; i < 10; i++) {
			g.drawLine(X_OFFSET + (int) (i * SCALE_FACTOR), 150, X_OFFSET
					+ (int) (i * SCALE_FACTOR), 170);
		}

		g.drawLine(X_OFFSET, 150, X_OFFSET, getHeight());

		for (int i = 0; i < 3; i++) {
			g.drawLine(X_OFFSET - 10, getHeight() / 2 + 150
					+ (int) (SCALE_FACTOR * i), X_OFFSET + 10, getHeight() / 2
					+ 150 + (int) (SCALE_FACTOR * i));
			g.drawLine(X_OFFSET - 10, getHeight() / 2 + 150
					- (int) (SCALE_FACTOR * i), X_OFFSET + 10, getHeight() / 2
					+ 150 - (int) (SCALE_FACTOR * i));
		}

		if (listSingles != null && showSingles.isSelected()) {
			for (int i = 0; i < listSingles.size(); i++) {
				listSingles.get(i).paint(g, chooseColor(listSingles.get(i).id));
			}
			
		}
		//
		if (listMean != null && showMean.isSelected()) {
			System.out.println("Size: " + listMean.size());
			for (int i = 0; i < listMean.size(); i++) {
//				System.out.println("Size of List with id: "
//						+ listMean.get(i).id + "Size: "
//						+ listMean.get(i).dataList.size());
				listMean.get(i).paint(g, chooseColor(listMean.get(i).id));
			}
		}
	}
}
