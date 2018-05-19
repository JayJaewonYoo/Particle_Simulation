import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.lang.reflect.Field;

// To Do:
	// Figure out where particles are colliding relative to one another
	// Allow user to manipulate number of particles
	// Allow user to change natural forces
	// Make the width/height of borders into variables that can be manipulated
	// Add option to change the speed of the simulation by increasing/decreasing delay
	// Make collisions based on a function (only for particle-particle)

public class GUI extends JPanel implements KeyListener, ActionListener{ // Remove keylistener if not using keys
	private static final long serialVersionUID = 6040042718732238432L;
	
	private boolean run = false;
	static boolean overlap = false;
	static boolean pause = false;
	static boolean showVector = false;
	static boolean randomColors = false;
	
	static Timer timer;
	static double delay = 8; // 8 is standard speed
	
	static int numParticles = 50; 
	static final byte particleSize = 11;
	
	static int borderCoord[] = {0, 0}; // lLcation of top left corner of border. 
	
	static ArrayList<particle> particleList = new ArrayList<particle>();
	
	private boolean collisionConfirm = false;
	
	// Variables used for particle-particle collision detection:
	private boolean particleParticleCollisionConfirm = false;
	
	static Color particleColor = Color.black;
	static Color backgroundColor = Color.white;
	static Color vectorColor = Color.black;
	
	private int borderWidth = 685;
	private int borderHeight = 700;

	// Initializing particles:
	static void initialize(int start, int num) {
		/*borders = borders.createUnion(new Rectangle(0, 0, 700, 3));
		borders = borders.createUnion(new Rectangle(700, 0, 3, 703));
		borders = borders.createUnion(new Rectangle(0, 700, 700, 3)); */
		
		/*if(numParticles > 2000) {
			numParticles = 2000;
		}*/
		for(int i = start; i < num; i++) {
			particleList.add(new particle());
			particleList.get(i).time = 0;
			particleList.get(i).velocity[0] = 0;
			particleList.get(i).velocity[1] = 0;
			particleList.get(i).particleParticleCollision = false;
			particleList.get(i).particleParticleCollision2 = false;

			// Generate random location for entity to be placed:
			Random temp = new Random();
			int tempLocationArray[] = new int[2];
			tempLocationArray[0] = temp.nextInt(borderCoord[0] + 640) + borderCoord[0];
			temp = new Random();
			tempLocationArray[1] = temp.nextInt(borderCoord[1] + 640) + borderCoord[1];
			for(int j = 0; j < i; j++) {
				if(new Ellipse2D.Double(tempLocationArray[0] - (particleSize/2), tempLocationArray[1] - (particleSize/2), particleSize, particleSize).intersects
						(particleList.get(j).location[0] - (particleSize/2), particleList.get(j).location[1] - (particleSize/2), particleSize, particleSize)) {
				
					overlap = true;
					break;
				}
			}
			
			double tempVelocityArray[] = new double[2];
			temp = new Random();
			tempVelocityArray[0] = temp.nextInt(5) + 5;
			tempVelocityArray[1] = temp.nextInt(5) + 5;
			particleList.get(i).velocity = tempVelocityArray;
			
			if(overlap) {
				overlap = false;
				i--;
			}
			else {
				particleList.get(i).location = tempLocationArray;
			}
			particleList.get(i).appliedForce = naturalForceCalc();
		}
	}
	
	private void drawArrow(double location[], double velocity[]) {
		// https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
		AffineTransform transform = new AffineTransform();
		transform.setToIdentity();
		Line2D.Double tail = new Line2D.Double(location[0] + velocity[0], location[1] + velocity[1], location[0], location[1]);
			// arguments: (x1, y1, x2, y2)
	}
	
	// for these forces if mass is being used, set the following values as accelerations.
	public static double gravity[] = {0, -1}; // index 0 is horizontal, index 1 is vertical.
	public static double wind[] = {0, 0}; // positive indicates right, negative indicates left.
	
	public static double[] naturalForceCalc() {
		// calculates natural forces (ie: forces that are not from other entities) to affect solids
		// index 0 is horizontal, index 1 is vertical
		double[] res = new double[2];
		res[0] += gravity[0];
		res[0] += wind[0];
		
		res[1] -= gravity[1];
		res[1] -= wind[1];
		
		return res;
	}
	
	public GUI() {
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		timer = new Timer((int) delay, this);
		timer.start();
	}
	
	public void paintComponent(Graphics g) {
		Random randomInt = new Random();
		
		// Background:
		g.setColor(UIManager.getColor("Panel.background"));;
		g.fillRect(0, 0, 730, borderHeight + 3);

		// Background within Borders:
		if(randomColors) {
			Field tempField;
			try {
				tempField = Class.forName("java.awt.Color").getField((UserInput.colorOptions[randomInt.nextInt(9)].toLowerCase()));
				g.setColor((Color)tempField.get(null));
				randomInt = new Random();
			} catch (Exception e1) {
				g.setColor(Color.black);
				e1.printStackTrace();
			}
		}
		else {
			g.setColor(backgroundColor);
		}
		g.fillRect(borderCoord[0] + 1, borderCoord[1] + 1, borderWidth, borderHeight);
		
		// Borders:
		g.setColor(Color.black);
		g.fillRect(borderCoord[0], borderCoord[1], 3, borderWidth);
		g.fillRect(borderCoord[0], borderCoord[1], borderWidth, 3);
		g.fillRect(borderCoord[0] + borderWidth, borderCoord[1], 3, borderHeight + 3);
		g.fillRect(borderCoord[0], borderCoord[1] + borderHeight, borderWidth, 3);
		
		// Particle:
		for(int i = 0; i < numParticles; i++) {
			if(randomColors) {
				Field tempField;
				try {
					tempField = Class.forName("java.awt.Color").getField((UserInput.colorOptions[randomInt.nextInt(9)].toLowerCase()));
					g.setColor((Color)tempField.get(null));
				} catch (Exception e1) {
					g.setColor(Color.black);
					e1.printStackTrace();
				}
			}
			else {
				g.setColor(particleColor);
			}
			g.fillOval(particleList.get(i).location[0], particleList.get(i).location[1], particleSize, particleSize);
			
			if(showVector) {
				// Draw vector arrows here:
			}
		}
		g.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		timer.start();
		repaint();
		if(!run) {
			run = true;
			initialize(0, 50);
		}
		else {
			if(!pause) {
				for (int i = 0; i < numParticles; i++) {
					// check if there is an entity nearby
					if (overlap) {
						overlap = false;
						// update force based on location of entity and the existing natural forces
						// inside
					}
				}
				// Move particles here:
				for (int i = 0; i < numParticles; i++) {
					particleList.get(i).appliedForce = naturalForceCalc();
					// Condition that there is a particle-particle collision:
					if (particleList.get(i).particleParticleCollision) {
						particleList.get(i).particleParticleCollision = false;
						particleList.get(i).velocity = particleList.get(i).particleParticleVelocity;
					}
					if (particleList.get(i).particleParticleCollision2) {
						particleList.get(i).particleParticleCollision2 = false;
						particleList.get(i).particleParticleCollision = true;
					}
					if (!particleList.get(i).particleParticleCollision) {
						for (int j = 0; j < numParticles; j++) {
							if ((i != j) && (new Ellipse2D.Double(
									particleList.get(i).location[0] + particleList.get(i).velocity[0] - (particleSize / 2),
									particleList.get(i).location[1] + particleList.get(i).velocity[1] - (particleSize / 2),
									particleSize, particleSize)
									.intersects(
											particleList.get(j).location[0] + particleList.get(j).velocity[0]
													- (particleSize / 2),
													particleList.get(j).location[1] + particleList.get(j).velocity[1]
															- (particleSize / 2),
															particleSize, particleSize))) {
								particleList.get(i).particleParticleCollision = true;
								particleParticleCollisionConfirm = true;
								double midpoint[] = new double[2];
								midpoint[0] = (particleList.get(i).location[0] + particleList.get(i).velocity[0]
										+ particleList.get(j).location[0] + particleList.get(j).velocity[0]) / 2;
								midpoint[1] = (particleList.get(i).location[1] + particleList.get(i).velocity[1]
										+ particleList.get(j).location[1] + particleList.get(j).velocity[1]) / 2;
								if (((particleList.get(i).velocity[0] > 0)
										&& midpoint[0] > particleList.get(i).location[0] + particleList.get(i).velocity[0])
										|| ((particleList.get(i).velocity[0] < 0)
												&& (midpoint[0] < particleList.get(i).location[0]
														+ particleList.get(i).velocity[0]))) {
									particleList.get(i).particleParticleVelocity[0] = (-1)
											* particleList.get(i).velocity[0];
								} else if (particleList.get(i).velocity[0] == 0) {
									particleList.get(i).particleParticleVelocity[1] = particleList.get(j).velocity[0];
								}
								if (((particleList.get(i).velocity[1] > 0)
										&& midpoint[1] > particleList.get(i).location[1] + particleList.get(i).velocity[1])
										|| ((particleList.get(i).velocity[1] < 0)
												&& (midpoint[1] < particleList.get(i).location[1]
														+ particleList.get(i).velocity[1]))) {
									particleList.get(i).particleParticleVelocity[1] = (-1)
											* particleList.get(i).velocity[1];
								} else if (particleList.get(i).velocity[1] == 0) {
									particleList.get(i).particleParticleVelocity[1] = particleList.get(j).velocity[1];
								}
								if (i > j) {
									particleList.get(j).particleParticleCollision = true;
									if (((particleList.get(j).velocity[0] > 0)
											&& midpoint[0] > particleList.get(j).location[0]
													+ particleList.get(j).velocity[0])
											|| ((particleList.get(j).velocity[0] < 0)
													&& (midpoint[0] < particleList.get(j).location[0]
															+ particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = (-1)
												* particleList.get(j).velocity[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
											&& midpoint[1] > particleList.get(j).location[1]
													+ particleList.get(j).velocity[1])
											|| ((particleList.get(j).velocity[1] < 0)
													&& (midpoint[1] < particleList.get(j).location[1]
															+ particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = (-1)
												* particleList.get(j).velocity[1];
									}
								} else {
									particleList.get(j).particleParticleCollision2 = true;
									if (((particleList.get(j).velocity[0] > 0)
											&& midpoint[0] > particleList.get(j).location[0]
													+ particleList.get(j).velocity[0])
											|| ((particleList.get(j).velocity[0] < 0)
													&& (midpoint[0] < particleList.get(j).location[0]
															+ particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = (-1)
												* particleList.get(j).velocity[0];
									} else if (particleList.get(j).velocity[0] == 0) {
										particleList.get(j).particleParticleVelocity[0] = particleList.get(i).velocity[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
											&& midpoint[1] > particleList.get(j).location[1]
													+ particleList.get(j).velocity[1])
											|| ((particleList.get(j).velocity[1] < 0)
													&& (midpoint[1] < particleList.get(j).location[1]
															+ particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = (-1)
												* particleList.get(j).velocity[1];
									} else if (particleList.get(j).velocity[1] == 0) {
										particleList.get(j).particleParticleVelocity[1] = particleList.get(i).velocity[1];
									}
								}
							}
						}
	
						// Condition that there is a boundary collision:
						// Here, 3 and 700 are the min and max boundaries respectively.
						if (!collisionConfirm) {
							if ((particleList.get(i).location[0] + particleList.get(i).velocity[0] < borderCoord[0])
									&& (particleList.get(i).velocity[0] < 0)) {
								particleList.get(i).velocity[0] *= -1;
								particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).location[0] += particleList.get(i).velocity[0] * (9 / 10);
								collisionConfirm = true;
							} else if ((particleList.get(i).location[0]
									+ particleList.get(i).velocity[0] > (borderCoord[0] + borderWidth) - particleSize)
									&& (particleList.get(i).velocity[0] > 0)) {
								particleList.get(i).velocity[0] *= -1;
								particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).location[0] += particleList.get(i).velocity[0] * (9 / 10);
								collisionConfirm = true;
							}
							if ((particleList.get(i).location[1] + particleList.get(i).velocity[1] < borderCoord[1])
									&& (particleList.get(i).velocity[1] < 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).location[1] += particleList.get(i).velocity[1] * (9 / 10);
								collisionConfirm = true;
							} else if ((particleList.get(i).location[1]
									+ particleList.get(i).velocity[1] > (borderCoord[1] + borderHeight) - particleSize)
									&& (particleList.get(i).velocity[1] > 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).location[1] += particleList.get(i).velocity[1] * (9 / 10);
								collisionConfirm = true;
							}
						}
	
						// Condition that there are no collisions:
						if (collisionConfirm) {
							collisionConfirm = false;
							particleList.get(i).time = 0;
						} else {
							particleList.get(i).location[0] += particleList.get(i).velocity[0];
							particleList.get(i).location[1] += particleList.get(i).velocity[1];
	
							particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
									/ particleList.get(i).mass) * particleList.get(i).time;
							particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
									/ particleList.get(i).mass) * particleList.get(i).time;
	
							particleList.get(i).time += 0.001;
						}
						if (particleParticleCollisionConfirm) {
							particleParticleCollisionConfirm = false;
	
							particleList.get(i).location[0] += particleList.get(i).velocity[0];
							particleList.get(i).location[1] += particleList.get(i).velocity[1];
	
							particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
									/ particleList.get(i).mass) * particleList.get(i).time;
							particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
									/ particleList.get(i).mass) * particleList.get(i).time;
	
							particleList.get(i).time += 0.001;
						}
					}
				}
				repaint();
			}
		}
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
	
}
