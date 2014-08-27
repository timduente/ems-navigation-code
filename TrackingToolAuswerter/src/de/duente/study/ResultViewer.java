package de.duente.study;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ResultViewer extends JFrame {
	private static final long serialVersionUID = -6272869584209311245L;
	private JButton loadFile = new JButton("Datei öffnen");
	private JCheckBox showSingles = new JCheckBox("Singles anzeigen");
	private JCheckBox showMean = new JCheckBox("Mittelwert anzeigen");
	private JCheckBox showFilteredSingle = new JCheckBox("Gefilterte Singles");
	private JCheckBox showFilteredMean = new JCheckBox("Gefilterte Mittel");
	private JSlider slideAngles = new JSlider(0, 500, 0);
	private JLabel slideLabel = new JLabel("Winkel ziehen");

	private JLabel leftText = new JLabel("Links");
	private JLabel rightText = new JLabel("Rechts");

	private String[] text = { "null", "schwach", "mittel", "stark" };
	private JCheckBox[] rightBoxes = new JCheckBox[text.length];
	private JCheckBox[] leftBoxes = new JCheckBox[text.length];

	public final static float SCALE_FACTOR = 150.0f;
	public final static int X_OFFSET = 250;
	public final static int Z_OFFSET = 150;

	private FileWriter singleAngleWriter;

	private Container cp;
	private ArrayList<TrackingDataObjectList> listSingles;
	private ArrayList<TrackingDataObjectList> listMean;
	private ArrayList<TrackingDataObjectList> listFilteredSingles;
	private ArrayList<TrackingDataObjectList> listFilteredMean;

	private static final String SINGLE_ANGLE_HEAD = "ID;Count;Angle per Meter;\n";
	private static final String SEPERATOR = ";";
	private static final String TITEL = "Result Viewer";

	public ResultViewer() {
		super(TITEL);
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

		showFilteredSingle.addActionListener(repaintActionListener);

		showFilteredSingle.setBounds(160, 40, 140, 20);
		cp.add(showFilteredSingle);

		showFilteredMean.addActionListener(repaintActionListener);

		showFilteredMean.setBounds(160, 60, 140, 20);
		cp.add(showFilteredMean);

		slideAngles.setBounds(100, 80, slideAngles.getMaximum(), 20);

		slideAngles.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				repaint();
			}

		});
		cp.add(slideAngles);

		slideLabel.setBounds(20, 80, 80, 20);
		cp.add(slideLabel);

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

	private void writeSingleAngleDataToFile(float anglePerMeter,
			TrackingDataObjectList tDOList) {
		// System.out.println("Id: " + id + " Angle: " + anglePerMeter);
		try {
			String toWrite = tDOList.getID() + SEPERATOR
					+ tDOList.getCount() + SEPERATOR + anglePerMeter
					+ SEPERATOR + "\n";
			singleAngleWriter.append(toWrite.replace('.', ','));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processFile(File file) {
		this.setTitle(TITEL + " - " + file.getAbsolutePath());
		File file_single_angle = new File(file.getParent(), "Winkel_einzel.csv");

		try {
			singleAngleWriter = new FileWriter(file_single_angle);
			singleAngleWriter.write(SINGLE_ANGLE_HEAD);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<TrackingDataObjectList> list = TrackingDataObject
				.parseFileIntoSortedTrackingDataObjectList(file);
		listSingles = list;

		for (int i = 0; i < list.size(); i++) {
			listSingles.get(i).transformCoords();
			ArrayList<Vector> vectors1 = list.get(i).getVectors();
			printAngleForVectorList(vectors1, list.get(i));
		}

		listMean = new ArrayList<TrackingDataObjectList>();
		listFilteredMean = new ArrayList<TrackingDataObjectList>();
		for (int j = -4; j < 4; j++) {

			ArrayList<TrackingDataObjectList> sameID = new ArrayList<TrackingDataObjectList>();
			for (int i = 0; i < listSingles.size(); i++) {
				if (listSingles.get(i).getID() == j) {
					sameID.add(listSingles.get(i));
				}
			}
			System.out
					.println("id: " + j + "Size von SameID: " + sameID.size());
			listMean.add(TrackingDataObjectList.createMeanList(sameID));
			listFilteredMean.add(TrackingDataObjectList
					.generateNMeanFilteredList(
							listMean.get(listMean.size() - 1), 41));
		}
		listFilteredSingles = new ArrayList<TrackingDataObjectList>();
		for (int i = 0; i < listSingles.size(); i++) {
			listFilteredSingles.add(TrackingDataObjectList
					.generateNMeanFilteredList(listSingles.get(i), 41));
		}

		try {
			singleAngleWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repaint();
	}

	private void printAngleForVectorList(ArrayList<Vector> vectors,
			TrackingDataObjectList tDOList) {
		// System.out.println("Laenge der Vektorliste: " + vectors.size());

		float anglePerMeter = 0.0f;
		for (int j = 0; j < vectors.size() - 1; j++) {
			float angle = Vector.getAngleBetweenVecs(vectors.get(j),
					vectors.get(j + 1));

			// if(list.get(i).id == -4)
			// System.out.println("Winkel: " + angle + vectors.get(j).toString()
			// + vectors.get(j+1).toString() + ";" +
			// filteredList.get(i).dataList.get(0).toString() + ";" +
			// filteredList.get(i).dataList.get(1)+";" +
			// filteredList.get(i).dataList.get(2));
			//

			if (Float.isNaN(angle) || Float.isNaN(vectors.get(j).getLength())
					|| Float.isNaN(vectors.get(j + 1).getLength())) {
				System.err.println("Winkel ist NAN. \n"
						+ vectors.get(j).toString() + "\n"
						+ vectors.get(j + 1).toString() + "\n");
			} else {
				anglePerMeter = anglePerMeter + angle;
			}
		}

		float length = 0.0f;
		for (int j = 0; j < vectors.size() - 1; j++) {
			if (Float.isNaN(length)) {
				System.out
						.println("Laenge is NAN" + j + " ; " + vectors.get(j));
			}
			length = length + vectors.get(j).getLength();
		}
//		System.out.println("ID: " + tDOList.getID() + "; Count: " +tDOList.getCount() + " TDOListengröße " + tDOList.getSize());
//		System.out.println("winkel pro meter vorher: "+anglePerMeter +
//		 ", Laenge: "+ length);
		anglePerMeter = anglePerMeter / length;
//		System.out.println("winkel pro meter nachher: "+anglePerMeter);
		writeSingleAngleDataToFile(anglePerMeter, tDOList);

	}

	private Color chooseColor(TrackingDataObjectList tDOList) {
		Color color = null;
		switch (tDOList.getID()) {
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

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// Bildbereich leeren
		g.setColor(Color.white);
		g.fillRect(0, 150, getWidth(), getHeight());

		// Waagerechte obere schwarze Linie
		g.setColor(Color.black);
		g.drawLine(0, Z_OFFSET, getWidth(), Z_OFFSET);

		// Skala für x-Achse.
		for (int i = -1; i < 10; i++) {
			g.drawLine(X_OFFSET + (int) (i * SCALE_FACTOR), Z_OFFSET, X_OFFSET
					+ (int) (i * SCALE_FACTOR), Z_OFFSET + 20);
		}

		// Z-Achse
		g.drawLine(X_OFFSET, Z_OFFSET, X_OFFSET, getHeight());

		for (int i = -1; i < 10; i++) {
			g.drawLine(X_OFFSET, Z_OFFSET + (int) (i * SCALE_FACTOR),
					X_OFFSET + 20, Z_OFFSET + (int) (i * SCALE_FACTOR));
		}

		if (listSingles != null && showSingles.isSelected()) {
			for (int i = 0; i < listSingles.size(); i++) {
				listSingles.get(i).paint(g, chooseColor(listSingles.get(i)));
			}

		}

		if (listMean != null && showMean.isSelected()) {
			System.out.println("Size: " + listMean.size());
			for (int i = 0; i < listMean.size(); i++) {
				// System.out.println("Size of List with id: "
				// + listMean.get(i).id + "Size: "
				// + listMean.get(i).dataList.size());
				listMean.get(i).paint(g, chooseColor(listMean.get(i)));
				if (chooseColor(listMean.get(i)) != null) {
					g.drawString(
							"Winkel für: "
									+ listMean.get(i).getID()
									+ " = "
									+ listMean.get(i).getAngleOnIndex(
											slideAngles.getValue()),
							getWidth() - 200, 500 + 30 * listMean.get(i)
									.getID());
				}
			}

		}
		if (listFilteredSingles != null && showFilteredSingle.isSelected()) {
			for (int i = 0; i < listFilteredSingles.size(); i++) {
				listFilteredSingles.get(i).paint(g,
						chooseColor(listFilteredSingles.get(i)));
			}
		}

		if (listFilteredMean != null && showFilteredMean.isSelected()) {
			for (int i = 0; i < listFilteredMean.size(); i++) {
				listFilteredMean.get(i).paint(g,
						chooseColor(listFilteredMean.get(i)));
			}
		}
	}
}
