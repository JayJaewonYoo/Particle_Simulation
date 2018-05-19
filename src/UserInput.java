import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class UserInput extends JPanel implements KeyListener, ActionListener, DocumentListener{
	private static final long serialVersionUID = -3239823441327400365L;
	
	public static double speed = 1;
	public int userNumParticles = 50;
	static String[] colorOptions = {"Black", "Blue", "Cyan", "Green", "Yellow", "Magenta", "Orange", "Pink", "Red", "White"};
	public UserInput() {
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		JLabel numParticlesLabel = new JLabel("Number of Particles: ");
		JTextField numParticlesSelected = new JTextField(4);
		numParticlesLabel.setLabelFor(numParticlesSelected);
		numParticlesSelected.setText("50");
		numParticlesSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeValues();
			}

			public void changeValues() {
				if(numParticlesSelected.getText().matches("[0-9]+")) {
					int difference = Integer.parseInt(numParticlesSelected.getText());
					if(difference != GUI.numParticles) {
						if(difference > GUI.numParticles) {
							// Adding particles:
							if(difference > 2000) {
								difference = 2000;
								numParticlesSelected.setText("2000");
							}
							GUI.initialize(GUI.numParticles, difference);
							GUI.numParticles = difference;
						}
						else {
							// Removing particles:
							if(difference < 0) {
								difference = 0;
								numParticlesSelected.setText("0");
							}
							difference = GUI.numParticles - difference;
							GUI.numParticles -= difference;
							for(int i = 0; i < difference; i++) {
								GUI.particleList.remove(0);
							}
						}
					}
				}
				else {
					numParticlesSelected.setText(Integer.toString(GUI.numParticles));
				}
			}
		});
		
		JLabel speedLabel = new JLabel("Speed Multiplier: ");
		JTextField speedMultiplier = new JTextField(4);
		speedLabel.setLabelFor(speedMultiplier);
		speedMultiplier.setText("1.0");
		speedMultiplier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeValues();
			}

			public void changeValues() {
				if(speedMultiplier.getText().matches("[0-9.]*") && Double.parseDouble(speedMultiplier.getText()) > 0) {
					speed = Double.parseDouble(speedMultiplier.getText());
					GUI.delay = 8/speed;
					if(speed > 8) {
						GUI.delay = 1;
						speed = 8;
						speedMultiplier.setText("8.0");
					}
					GUI.timer.setDelay((int) GUI.delay);
				}
				else {
					speedMultiplier.setText(Double.toString(speed));
				}
			}
		});
		
		JLabel gravityLabel = new JLabel("Vertical Gravity: ");
		JTextField gravity = new JTextField(4);
		gravityLabel.setLabelFor(gravity);
		gravity.setText("1.0");
		gravity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeValues();
			}

			public void changeValues() {
				GUI.gravity[1] = (-1) * Double.parseDouble(gravity.getText());
			}
		});

		JLabel windLabel = new JLabel("Horizontal Wind: ");
		JTextField wind = new JTextField(4);
		windLabel.setLabelFor(wind);
		wind.setText("0.0");
		wind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeValues();
			}
			public void changeValues() {
				GUI.wind[0] = Double.parseDouble(wind.getText());
			}
		});
		
		JLabel blank = new JLabel(" ");
		
		JLabel colorLabel = new JLabel("Particle Color: ");
		JComboBox<String> colorBox = new JComboBox<String>(colorOptions);
		colorBox.setPreferredSize(new Dimension(85, 20));
		colorBox.setEditable(false);
		colorBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Field tempField = Class.forName("java.awt.Color").getField(((String)colorBox.getSelectedItem()).toLowerCase());
					GUI.particleColor = (Color)tempField.get(null);
				} catch (Exception e1) {
					GUI.particleColor = Color.black;
					colorBox.setSelectedItem("Black");
					e1.printStackTrace();
				}
			}
		});
		
		JLabel backgroundColorLabel = new JLabel("Background Color: ");
		JComboBox<String> backgroundColorBox = new JComboBox<String>(colorOptions);
		backgroundColorBox.setPreferredSize(new Dimension(85, 20));
		backgroundColorBox.setSelectedItem("White");
		backgroundColorBox.setEditable(false);
		backgroundColorBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Field tempField2 = Class.forName("java.awt.Color").getField(((String)backgroundColorBox.getSelectedItem()).toLowerCase());
					GUI.backgroundColor = (Color)tempField2.get(null);
				} catch (Exception e1) {
					GUI.backgroundColor = Color.white;
					backgroundColorBox.setSelectedItem("White");
					e1.printStackTrace();
				}
			}
		});
		
		JButton pauseButton = new JButton("Pause");
		pauseButton.setPreferredSize(new Dimension(85, 20));
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pauseButton.getText() == "Pause") {
					GUI.pause = true;
					pauseButton.setText("Unpause");
				}
				else {
					GUI.pause = false;
					pauseButton.setText("Pause");
				}
			}
		});
		
		JLabel vectorColorLabel = new JLabel("Vector Color: ");
		vectorColorLabel.setVisible(false);
		JComboBox<String> vectorColorBox = new JComboBox<String>(colorOptions);
		vectorColorBox.setPreferredSize(new Dimension(85, 20));
		vectorColorBox.setSelectedItem("Black");
		vectorColorBox.setEditable(false);
		vectorColorBox.setVisible(false);
		vectorColorBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Field tempField2 = Class.forName("java.awt.Color").getField(((String)vectorColorBox.getSelectedItem()).toLowerCase());
					GUI.vectorColor = (Color)tempField2.get(null);
				} catch (Exception e1) {
					GUI.vectorColor = Color.black;
					backgroundColorBox.setSelectedItem("Black");
					e1.printStackTrace();
				}
			}
		});
		
		JLabel vectorLabel = new JLabel("Show Vectors: ");
		JButton vectorButton = new JButton("Enable");
		vectorButton.setPreferredSize(new Dimension(85, 20));
		vectorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(vectorButton.getText() == "Enable") {
					GUI.showVector = true;
					vectorColorLabel.setVisible(true);
					vectorColorBox.setVisible(true);
					vectorButton.setText("Disable");
				}
				else {
					GUI.showVector = false;
					vectorColorLabel.setVisible(false);
					vectorColorBox.setVisible(false);
					vectorButton.setText("Enable");
				}
			}
		});
		
		JLabel randomColorLabel = new JLabel("Random Colors: ");
		JButton randomColorButton = new JButton("Enable");
		randomColorButton.setPreferredSize(new Dimension(85, 20));
		randomColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(randomColorButton.getText() == "Enable") {
					GUI.randomColors = true;
					colorLabel.setVisible(false);
					colorBox.setVisible(false);
					backgroundColorLabel.setVisible(false);
					backgroundColorBox.setVisible(false);
					randomColorButton.setText("Disable");
				}
				else {
					GUI.randomColors = false;
					colorLabel.setVisible(true);
					colorBox.setVisible(true);
					backgroundColorLabel.setVisible(true);
					backgroundColorBox.setVisible(true);
					randomColorButton.setText("Enable");
				}
			}
		});
		
		
		/*JButton accept = new JButton("Accept");
		accept.setMnemonic(KeyEvent.VK_D);
		accept.setActionCommand("accept");
		accept.addActionListener(this);
		accept.setToolTipText("Click here to implement changes.");*/
		
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(numParticlesLabel, constraints);
		constraints.gridx = 1;
		add(numParticlesSelected, constraints);
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(speedLabel, constraints);
		constraints.gridx = 1;
		add(speedMultiplier, constraints);
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(gravityLabel, constraints);
		constraints.gridx = 1;
		add(gravity, constraints);
		constraints.gridx = 0;
		constraints.gridy = 4;
		add(windLabel, constraints);
		constraints.gridx = 1;
		add(wind, constraints);
		constraints.gridy = 5;
		add(blank, constraints);
		constraints.gridy = 6;
		add(colorBox, constraints);
		constraints.gridx = 0;
		add(colorLabel, constraints);
		constraints.gridy = 7;
		add(backgroundColorLabel, constraints);
		constraints.gridx = 1;
		add(backgroundColorBox, constraints);
		constraints.gridy = 8;
		add(pauseButton, constraints);
		constraints.gridy = 9;
		add(vectorButton, constraints);
		constraints.gridx = 0;
		add(vectorLabel, constraints);
		constraints.gridy = 10;
		add(vectorColorLabel, constraints);
		constraints.gridx = 1;
		add(vectorColorBox, constraints);
		constraints.gridy = 11;
		add(randomColorButton, constraints);
		constraints.gridx = 0;
		add(randomColorLabel, constraints);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		
	}
	
}
