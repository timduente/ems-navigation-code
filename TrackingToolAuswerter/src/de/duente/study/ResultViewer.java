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

	private final static float SCALE_FACTOR = 150.0f;
	private final static int X_OFFSET = 250;
	private final static int Z_OFFSET = 150;
	
	private final static int POINT_COUNT = 150;

	private Container cp;
	private ArrayList<TrackingDataObject> listSingles;
	private ArrayList<TrackingDataObject> listMean;

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
		ArrayList<TrackingDataObject> list = TrackingDataObject
				.parseFileIntoSortedTrackingDataObjectList(file);
		listSingles = list;
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

		for (int j = -4; j < 4; j++) {
			ArrayList<TrackingDataObject> sum = new ArrayList<TrackingDataObject>();
			int lastCount = -4;

			for (int i = 0; i < list.size(); i++) {
				t = list.get(i);
				int sumIndex = 0;

				if (t.intensity == j && t.signalOn && lastCount == -4) {
					lastCount = -2;
				}
				if (t.intensity == j && !t.signalOn && lastCount == -3) {
					lastCount = -5;
				}
				if (t.intensity == j && t.signalOn && lastCount == -5) {
					lastCount = -1;
				}

				if (lastCount == -2) {
					while (sumIndex < POINT_COUNT) {
						TrackingDataObject u = new TrackingDataObject(t);
						u.count = 1;
						sum.add(u);
						i++;
						t = list.get(i);
						sumIndex++;
					}
					lastCount = -3;
				} else if (lastCount == -1) {
					while (sumIndex < POINT_COUNT && i < list.size()) {
						TrackingDataObject u = sum.get(sumIndex);
						// if(sumIndex == 0){
						if (u.x == 0.0f && u.z == 0.0f && u.y <= 1.0f) {
							u.x = t.x;
							u.z = t.z;
							u.count = 1;
						} else if (t.x != 0.0f || t.y != 0.0f || t.z != 0.0f) {
							u.x = u.x + t.x;
							u.z = u.z + t.z;
							u.count++;
						}
						// }
						// else{
						// u.x = u.x + t.x;
						// u.z = u.z + t.z;
						// u.count++;
						// }
						System.out.println("U: " + u.x + " SUM: "
								+ sum.get(sumIndex).x + "Count: "
								+ sum.get(sumIndex).count);
						sumIndex++;
						i++;
						if (i < list.size()) {
							t = list.get(i);
						}
					}
					lastCount = -3;
				}
			}

			for (int i = 0; i < sum.size(); i++) {
				sum.get(i).x = sum.get(i).x / (float) sum.get(i).count;
				sum.get(i).z = sum.get(i).z / (float) sum.get(i).count;
				System.out.println(" SUM: " + sum.get(i).x);
				listMean.add(sum.get(i));
			}
			System.out.println("Size of SumList: " + sum.size());

		}

		repaint();
		System.out.println(list.size());

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
			TrackingDataObject t;
			boolean signalEnd = false;
			for (int i = 0; i < listSingles.size(); i++) {
				t = listSingles.get(i);
				Color color = chooseColor(t.intensity);
				if (color != null) {
					g.setColor(color);

					g.drawLine(
							(int) (listSingles.get(i).x * SCALE_FACTOR + X_OFFSET),
							(int) (listSingles.get(i).z * SCALE_FACTOR)
									+ Z_OFFSET,
							(int) (listSingles.get(i).x * SCALE_FACTOR)
									+ X_OFFSET,
							(int) (listSingles.get(i).z * SCALE_FACTOR)
									+ Z_OFFSET);

					if (signalEnd && listSingles.get(i).signalOn) {
						signalEnd = false;
					}

					if (!listSingles.get(i).signalOn && !signalEnd) {
						signalEnd = true;
						g.setColor(Color.DARK_GRAY);

						g.fillRect((int) (listSingles.get(i).x * SCALE_FACTOR)
								+ X_OFFSET - 2,
								(int) (listSingles.get(i).z * SCALE_FACTOR)
										+ Z_OFFSET - 2, 5, 5);
					}
				}
			}
		}

		if (listMean != null && showMean.isSelected()) {
			TrackingDataObject t;
			boolean signalEnd = false;
			for (int i = 0; i < listMean.size(); i++) {
				t = listMean.get(i);
				Color color = chooseColor(t.intensity);
				if (color != null) {
					g.setColor(color);

					if (signalEnd && listMean.get(i).signalOn) {
						signalEnd = false;
					}

					g.fillRect((int) (listMean.get(i).x * SCALE_FACTOR)
							+ X_OFFSET - 1,
							(int) (listMean.get(i).z * SCALE_FACTOR) + Z_OFFSET
									- 1, 3, 3);
					if (!listMean.get(i).signalOn && !signalEnd) {
						signalEnd = true;
						g.setColor(Color.DARK_GRAY);

						double angle = getAngle(listMean.get(0),
								listMean.get(i - 1));

						g.drawString("Winkel für: " + t.intensity + " = "
								+ angle, 1000, 500 + 30 * t.intensity);

						g.fillRect((int) (listMean.get(i).x * SCALE_FACTOR)
								+ X_OFFSET - 2,
								(int) (listMean.get(i).z * SCALE_FACTOR)
										+ Z_OFFSET - 2, 5, 5);
					}

				}
			}
		}

	}
}
