import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class UserInput extends JPanel implements KeyListener, ActionListener, DocumentListener {
	private static final long serialVersionUID = -3239823441327400365L;
	
	private boolean infoScreenOpen = false; 
	
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
							if(difference > 1000) {
								difference = 1000;
								numParticlesSelected.setText("1000");
							}
							GUI.initialize(GUI.numParticles, difference);
							GUI.numParticles = difference;
						}
						else {
							// Removing particles:
							if(difference < 0) {
								difference = 0;
								numParticlesSelected.setText("0");
								GUI.particleList = new ArrayList<particle>();
							} else {
								difference = GUI.numParticles - difference;
								GUI.numParticles -= difference;
								for(int i = 0; i < difference; i++) {
									GUI.particleList.remove(0);
								}
							}
						}
					}
				} else {
					numParticlesSelected.setText(Integer.toString(GUI.numParticles));
				}
			}
		});
		
		JLabel particleSizeLabel = new JLabel("Size of Particles: ");
		JTextField particleSizeSelected = new JTextField(4);
		particleSizeLabel.setLabelFor(particleSizeSelected);
		particleSizeSelected.setText("11");
		particleSizeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(particleSizeSelected.getText().matches("[0-9]+")) {
					int tempValue = Integer.parseInt(particleSizeSelected.getText());
					if(tempValue < 0) {
						tempValue = 0;
					} else if(tempValue > 500) {
						tempValue = 500;
					}
					GUI.particleSize = tempValue;
				} else {
					particleSizeSelected.setText(Integer.toString(GUI.particleSize));
				}
			}
		});
		
		JLabel energyLabel = new JLabel("Energy Loss (%): ");
		JTextField energySelected = new JTextField(4);
		energyLabel.setLabelFor(energySelected);
		energySelected.setText("30.0");
		energySelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(energySelected.getText().matches("[0-9.-]*")) {
					GUI.energyLoss = 1 - (Double.parseDouble(energySelected.getText())/100);
				} else {
					energySelected.setText(Double.toString(100 - (GUI.energyLoss * 100)));
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
				} else {
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
				if(gravity.getText().matches("[0-9.-]*")) {
					changeValues();
				} else {
					gravity.setText(Double.toString(GUI.gravity[1] * (-1)));
				}
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
				if(wind.getText().matches("[0-9.-]*")) {
					changeValues();
				} else {
					wind.setText(Double.toString(GUI.wind[0]));
				}
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
				} else {
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
				} else {
					GUI.showVector = false;
					vectorColorLabel.setVisible(false);
					vectorColorBox.setVisible(false);
					vectorButton.setText("Enable");
				}
			}
		});
		
		JLabel cursorSizeLabel = new JLabel("Size of Cursor: ");
		cursorSizeLabel.setVisible(false);
		JTextField cursorSizeSelected = new JTextField(4);
		cursorSizeSelected.setVisible(false);
		cursorSizeLabel.setLabelFor(cursorSizeSelected);
		cursorSizeSelected.setText("20");
		cursorSizeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(cursorSizeSelected.getText().matches("[0-9]+")) {
					GUI.cursorSize = Integer.parseInt(cursorSizeSelected.getText());
				} else {
					cursorSizeSelected.setText(Integer.toString(GUI.cursorSize));
				}
			}
		});
		
		JLabel cursorColorLabel = new JLabel("Cursor Color: ");
		cursorColorLabel.setVisible(false);
		JComboBox<String> cursorColorBox = new JComboBox<String>(colorOptions);
		cursorColorBox.setPreferredSize(new Dimension(85, 20));
		cursorColorBox.setSelectedItem("Black");
		cursorColorBox.setEditable(false);
		cursorColorBox.setVisible(false);
		cursorColorBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Field tempField2 = Class.forName("java.awt.Color").getField(((String)cursorColorBox.getSelectedItem()).toLowerCase());
					GUI.cursorColor = (Color)tempField2.get(null);
				} catch (Exception e1) {
					GUI.cursorColor = Color.black;
					cursorColorBox.setSelectedItem("Black");
					e1.printStackTrace();
				}
			}
		});
		
		JLabel cursorLabel = new JLabel("Cursor: ");
		JButton cursorButton = new JButton("Enable");
		cursorButton.setPreferredSize(new Dimension(85, 20));
		cursorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(cursorButton.getText() == "Enable") {
					GUI.cursor = true;
					cursorColorLabel.setVisible(true);
					cursorColorBox.setVisible(true);
					cursorSizeLabel.setVisible(true);
					cursorSizeSelected.setVisible(true);
					cursorButton.setText("Disable");
				} else {
					GUI.cursor = false;
					cursorColorLabel.setVisible(false);
					cursorColorBox.setVisible(false);
					cursorSizeLabel.setVisible(false);
					cursorSizeSelected.setVisible(false);
					cursorButton.setText("Enable");
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
				} else {
					GUI.randomColors = false;
					colorLabel.setVisible(true);
					colorBox.setVisible(true);
					backgroundColorLabel.setVisible(true);
					backgroundColorBox.setVisible(true);
					randomColorButton.setText("Enable");
				}
			}
		});
		
		JButton resetButton = new JButton("Reset");
		resetButton.setPreferredSize(new Dimension(85, 20));
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUI.run = false;
				GUI.overlap = false;
				GUI.pause = false;
				GUI.showVector = false;
				GUI.randomColors = false;
				GUI.delay = 8; // 8 is standard speed
				GUI.timer.setDelay((int) GUI.delay);
				GUI.numParticles = 50;
				GUI.particleSize = 11;
				GUI.particleList = new ArrayList<particle>();
				GUI.cursorParticleList = new ArrayList<cursorParticle>();
				GUI.cursor = false;
				GUI.cursorSize = 20;
				GUI.cursorColor = Color.black;
				GUI.energyLoss = 0.7;
				GUI.collisionConfirm = false;
				GUI.particleParticleCollisionConfirm = false;
				GUI.particleColor = Color.black;
				GUI.backgroundColor = Color.white;
				GUI.vectorColor = Color.black;
				GUI.gravity[0] = 0;
				GUI.gravity[1] = -1;
				GUI.wind[0] = 0;
				GUI.wind[1] = 0;
				
				numParticlesSelected.setText("50");
				energySelected.setText("30");
				speedMultiplier.setText("1.0");
				gravity.setText("1.0");
				wind.setText("0.0");
				colorBox.setSelectedIndex(0);
				backgroundColorBox.setSelectedIndex(9);
				cursorButton.setText("Enable");
				cursorSizeSelected.setText("20");
				cursorColorBox.setSelectedIndex(0);
				vectorColorBox.setSelectedIndex(0);
				pauseButton.setText("Pause");
				vectorButton.setText("Enable");
				vectorColorLabel.setVisible(false);
				vectorColorBox.setVisible(false);
				backgroundColorLabel.setVisible(true);
				backgroundColorBox.setVisible(true);
				cursorColorLabel.setVisible(false);
				cursorColorBox.setVisible(false);
				cursorSizeLabel.setVisible(false);
				cursorSizeSelected.setVisible(false);
				GUI.initialize(0, 50);
			}
		});
		
		
		JButton info = new JButton("CLICK ME");
		info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!infoScreenOpen) {
					infoScreenOpen = true;
					JFrame infoScreen = new JFrame();
					infoScreen.setTitle("README - Particle Physics Simulator");
					infoScreen.setResizable(true);
					infoScreen.setVisible(true);
					infoScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					infoScreen.setLocationRelativeTo(null);
					infoScreen.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent windowEvent) {
							infoScreenOpen = false;
						}
					});
					
					infoScreen.setSize(new Dimension(850, 500));
					JTextArea information = new JTextArea();
					information.setEditable(false);
					infoScreen.setLocation(300, 100);
					information.setLineWrap(true);
					information.setWrapStyleWord(true);
					information.setText("This Particle Physics Simulator is created by Jay Jaewon Yoo using Java."
					+ "\n\nThis program simulates movement and collision of particles. Use the text fields and buttons to the left of the particle area to manipulate the simulation."
					+ "\n\nTo change a value in a text area, replace the text with the desired value and press the ENTER key on your keyboard."
					+ "\nChange the value in the text field beside \"Number of Particles\" to change the number of particles present."
					+ "\nMinimum value: 0\nMaximum Value: 1000\nDecimal Values: Not accepted."
					+ "\n\nChange the value in the text field beside \"Size of Particles\" to change the size of the particles present."
					+ "\nMinimum value: 0\nMaximum Value: 500\nDecimal Values: Not accepted."
					+ "\n\nChange the value in the text field beside \"Energy Loss (%)\" to change the percentage of energy particles lose upon collision."
					+ "\nMinimum value: None\nMaximum Value: None\nDecimal Values: Accepted."
					+ "\n\nChange the value in the text field beside \"Speed Multiplier\" to change the speed at which the simulation runs."
					+ "\nMinimum value: 0\nMaximum Value: 8\nDecimal Values: Accepted."
					+ "\n* Note that values greater than 100 will increase the speed and values less than 0 will increase the speed and flip the direction."
					+ "\n\nChange the value in the text field beside \"Vertical Gravity\" to change the force of gravity acting downwards on the particles."
					+ "\nMinimum value: None\nMaximum Value: None\nDecimal Values: Accepted."
					+ "\n\nChange the value in the text field beside \"Horizontal Wind\" to change the force of horizontal wind acting rightward on the particles."
					+ "\nMinimum value: None\nMaximum Value: None\nDecimal Values: Accepted."
					+ "\n\nClick the \"Particle Color\" drop-down menu to change the color of the particles."
					+ "\n\nClick the \"Background Color\" drop-down menu to change the color of the background."
					+ "\n\nClick the \"Cursor\" button to manipulate the particles using the cursor."
					+ "\n* While \"Cursor\" is enabled, the size of the cursor can be manipulated by changing the corresponding text field."
					+ "\n* While \"Cursor\" is enabled, the cursor color can be changed by clicking the corresponding drop-down menu."
					+ "\n* While \"Cursor\" is enabled, left-clicking on the screen will place a copy of the cursor that acts as a particle."
					+ "\n* While \"Cursor\" is enabled, right-clicking on a placed cursor copy will remove it."
					+ "\n\nClick the \"Show Vectors\" button to show the vectors of the particles, represented by arrows."
					+ "\n* While \"Show Vectors\" is enabled, the vector arrow color can be changed by clicking the corresponding drop-down menu."
					+ "\n\nClick the \"Random Colors\" button to constantly change the particle colors to a random color."
					+ "\n\nClick the \"Pause\" button to pause the simulation."
					+ "\n\nClick the \"Reset\" button to reset the simulation. Note that the initial locations of the particles will be randomized.");
					information.setCaretPosition(0);
					JScrollPane scroll = new JScrollPane(information, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					infoScreen.getContentPane().add(scroll, BorderLayout.CENTER);
				}
			}
		});
		
		// Grid Bag Layout used for simplicity. 
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(numParticlesLabel, constraints);
		constraints.gridx = 1;
		add(numParticlesSelected, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		add(particleSizeLabel, constraints);
		constraints.gridx = 1;
		add(particleSizeSelected, constraints);
		constraints.gridy++;
		add(energySelected, constraints);
		constraints.gridx = 0;
		add(energyLabel, constraints);
		constraints.gridy++;
		add(speedLabel, constraints);
		constraints.gridx = 1;
		add(speedMultiplier, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		add(gravityLabel, constraints);
		constraints.gridx = 1;
		add(gravity, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		add(windLabel, constraints);
		constraints.gridx = 1;
		add(wind, constraints);
		constraints.gridy++;
		add(blank, constraints);
		constraints.gridy++;
		add(colorBox, constraints);
		constraints.gridx = 0;
		add(colorLabel, constraints);
		constraints.gridy++;
		add(backgroundColorLabel, constraints);
		constraints.gridx = 1;
		add(backgroundColorBox, constraints);
		constraints.gridy++;
		add(cursorButton, constraints);
		constraints.gridx = 0;
		add(cursorLabel, constraints);
		constraints.gridy++;
		add(cursorSizeLabel, constraints);
		constraints.gridx = 1;
		add(cursorSizeSelected, constraints);
		constraints.gridy++;
		add(cursorColorBox, constraints);
		constraints.gridx = 0;
		add(cursorColorLabel, constraints);
		constraints.gridx = 1;
		constraints.gridy++;
		add(vectorButton, constraints);
		constraints.gridx = 0;
		add(vectorLabel, constraints);
		constraints.gridy++;
		add(vectorColorLabel, constraints);
		constraints.gridx = 1;
		add(vectorColorBox, constraints);
		constraints.gridy++;
		add(randomColorButton, constraints);
		constraints.gridx = 0;
		add(randomColorLabel, constraints);
		constraints.gridy++;
		constraints.gridx = 1;
		add(pauseButton, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		add(blank, constraints);
		constraints.gridx = 1;
		add(resetButton, constraints);
		constraints.gridy++;
		add(blank, constraints);
		constraints.gridy++;
		add(info, constraints);
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
