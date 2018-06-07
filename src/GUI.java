import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import java.lang.Math;
import java.lang.reflect.Field;

public class GUI extends JPanel implements ActionListener, MouseListener, MouseMotionListener { // Remove keylistener if not using keys
	private static final long serialVersionUID = 6040042718732238432L;
	
	// Standard boolean variables:
	static boolean run = false;
	static boolean overlap = false;
	static boolean pause = false;
	static boolean showVector = false;
	static boolean randomColors = false;
	
	// Timer variables:
	static Timer timer;
	static double delay = 8; // 8 is standard speed
	
	// Particle variables:
	static int numParticles = 50; 
	static int particleSize = 11;
	static ArrayList<particle> particleList = new ArrayList<particle>();
	
	// Energy loss variables:
	static double energyLoss = 0.7;
	
	// Border variables:
	static int borderCoord[] = {0, 0}; // location of top left corner of border. 
	private int borderWidth = 678;
	private int borderHeight = 700;
	
	// Vector arrow variables:
	Path2D.Double path;
	AffineTransform t;
	double angle;
	
	// Variables used for particle-particle collision detection:
	static boolean particleParticleCollisionConfirm = false;
	
	// Colors variables:
	static Color particleColor = Color.black;
	static Color backgroundColor = Color.white;
	static Color vectorColor = Color.black;
	static Color cursorColor = Color.black;
	
	// Cursor variables:
	static boolean cursor = false;
	static int cursorSize = 20;
	private boolean cursorConfirm = false;
	private byte cursorMass = 1;
	private double cursorLocation[] = new double[2];
	private double oldCursorLocation[] = new double[2];
	private double cursorVelocity[] = new double[2];
	private boolean emptyOldCursor = true;
	static ArrayList<cursorParticle> cursorParticleList = new ArrayList<cursorParticle>();
	
	// Variables used for boundary collision detection
	static boolean collisionConfirm = false;
	
	// Force variables:
	public static double gravity[] = {0, -1}; // index 0 is horizontal, index 1 is vertical.
	public static double wind[] = {0, 0}; // positive indicates right, negative indicates left.

	// Initializing particles:
	static void initialize(int start, int num) {
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
				if(new Ellipse2D.Double(tempLocationArray[0] - (particleSize/2), tempLocationArray[1] - (particleSize/2), particleSize + 1, particleSize + 1)
				.intersects(particleList.get(j).location[0] - (particleSize/2), particleList.get(j).location[1] - (particleSize/2), particleSize + 1, particleSize + 1)) {
					overlap = true;
					break;
				}
			}
			
			double tempVelocityArray[] = new double[2];
			temp = new Random();
			tempVelocityArray[0] = temp.nextInt(5) + 6;
			tempVelocityArray[1] = temp.nextInt(5) + 6;
			particleList.get(i).velocity = tempVelocityArray;
			
			if(overlap) {
				overlap = false;
				i--;
			} else {
				particleList.get(i).location = tempLocationArray;
			}
			particleList.get(i).appliedForce = naturalForceCalc();
		}
	}
	
	public static double[] naturalForceCalc() {
		// Calculates natural forces (ie: forces that are not from other entities) to affect particles.
		// Index 0 is horizontal, index 1 is vertical.
		double[] res = new double[2];
		res[0] += gravity[0];
		res[0] += wind[0];
		
		// Subtraction used because gravity affects particles downward.
		res[1] -= gravity[1];
		res[1] -= wind[1];
		
		return res;
	}
	
	// GUI implementation:
	public GUI() {
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		timer = new Timer((int) delay, this);
		timer.start();
	}
	
	// Producing painted GUI:
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		
		Random randomInt = new Random();
		
		// Background:
		g2d.setColor(UIManager.getColor("Panel.background"));;
		g2d.fillRect(0, 0, 730, borderHeight + 3);

		// Background within Borders:
		g2d.setColor(backgroundColor);
		g2d.fillRect(borderCoord[0] + 1, borderCoord[1] + 1, borderWidth, borderHeight);
		
		// Borders:
		g2d.setColor(Color.black);
		g2d.fillRect(borderCoord[0], borderCoord[1], 3, borderHeight);
		g2d.fillRect(borderCoord[0], borderCoord[1], borderWidth, 3);
		g2d.fillRect(borderCoord[0] + borderWidth - 5, borderCoord[1], 3, borderHeight + 3);
		g2d.fillRect(borderCoord[0], borderCoord[1] + borderHeight, borderWidth, 3);
		
		// Cursor:
		if(cursor) {
			if((cursorLocation[0] < borderCoord[0] + borderWidth - 5) && (cursorLocation[0] > borderCoord[0] + 3) 
					&& (cursorLocation[1] > 3) && (cursorLocation[1] < borderCoord[1] + borderHeight) && cursorConfirm) {
				g2d.setColor(cursorColor);
				g2d.fillOval((int) (cursorLocation[0] - (cursorSize/2)), (int) (cursorLocation[1] - (cursorSize/2)), cursorSize, cursorSize);
			}
		}
		
		// Particles:
		for(int i = 0; i < numParticles; i++) {
			if(randomColors) {
				Field tempField;
				try {
					tempField = Class.forName("java.awt.Color").getField((UserInput.colorOptions[randomInt.nextInt(9)].toLowerCase()));
					g2d.setColor((Color)tempField.get(null));
				} catch (Exception e1) {
					g2d.setColor(Color.black);
					e1.printStackTrace();
				}
			}
			else {
				g2d.setColor(vectorColor);
			}
			
			if(showVector) {
				// Draw vector arrows here:
				path = new Path2D.Double();
				t = new AffineTransform();
				
				double xPoints[] = {4, 4, 1, 6, 11, 8, 8, 5};
				double yPoints[] = {5, 20, 20, 25, 20, 20, 5, 5};
				
				path.moveTo(0, 0);
				for(int j = 0; j < 8; j++) {
					path.lineTo(xPoints[j] * (particleSize/11), yPoints[j] * (particleSize/11));
				}
				angle = Math.atan(((-1) * particleList.get(i).velocity[0])/particleList.get(i).velocity[1]);
				if(particleList.get(i).velocity[1] < 0) {
					angle += Math.PI;
				}
				t.rotate(angle, particleList.get(i).location[0] + particleSize/2 + (11/particleSize), particleList.get(i).location[1] + particleSize/2 + (11/particleSize));
				t.translate(particleList.get(i).location[0], particleList.get(i).location[1]);
				
				path.transform(t);
				g2d.fill(path);
			}
			if(!randomColors) {
				g2d.setColor(particleColor);
			}
			g2d.fillOval(particleList.get(i).location[0], particleList.get(i).location[1], particleSize, particleSize);
		}
		
		// Areas set by cursor:
		for(int i = 0; i < cursorParticleList.size(); i++) {
			g2d.setColor(cursorParticleList.get(i).color);
			g2d.fillOval(cursorParticleList.get(i).location[0], cursorParticleList.get(i).location[1], cursorParticleList.get(i).size, cursorParticleList.get(i).size);
		}
		
		g2d.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		timer.start();
		repaint();
		if(!run) {
			run = true;
			initialize(0, 50);
		} else {
			if(!pause) {
				for (int i = 0; i < numParticles; i++) {
					// Checks if an entity is nearby:
					if (overlap) {
						overlap = false;
					}
					
					// Particle movement calculations begin here:
					particleList.get(i).appliedForce = naturalForceCalc();
					
					// Condition that there was a particle-particle collision:
					if (particleList.get(i).particleParticleCollision) {
						particleList.get(i).particleParticleCollision = false;
						particleList.get(i).velocity = particleList.get(i).particleParticleVelocity;
					}
					if (particleList.get(i).particleParticleCollision2) {
						particleList.get(i).particleParticleCollision2 = false;
						particleList.get(i).particleParticleCollision = true;
					}
					
					if (!particleList.get(i).particleParticleCollision) {
						// Checking for collision with cursor:
						if(cursor && (new Ellipse2D.Double(
						particleList.get(i).location[0] - (particleSize / 2),
						particleList.get(i).location[1] - (particleSize / 2),
						particleSize, particleSize)
						.intersects(cursorLocation[0] - (cursorSize / 2), cursorLocation[1] - (cursorSize / 2),
						cursorSize, cursorSize))) {
							particleList.get(i).particleParticleCollision = true;
							particleParticleCollisionConfirm = true;
							
							if(emptyOldCursor) {
								emptyOldCursor = false;
							} else {
								cursorVelocity[0] = (cursorLocation[0] - oldCursorLocation[0]) / 10;
								cursorVelocity[1] = (cursorLocation[1] - oldCursorLocation[1]) / 10;
							}
							oldCursorLocation[0] = cursorLocation[0];
							oldCursorLocation[1] = cursorLocation[1];
							
							// Velocity calculations:
							double tempArrayi[] = new double[2];
							for(int k = 0; k < 2; k++) {
								tempArrayi[k] = cursorMass * cursorVelocity[k];
								tempArrayi[k] += particleList.get(i).mass * particleList.get(i).velocity[k];
								tempArrayi[k] += cursorMass * energyLoss * (cursorVelocity[k] - particleList.get(i).velocity[k]);
								tempArrayi[k] /= cursorMass + particleList.get(i).mass;
							}
							
							// Calculations to ensure velocity manipulations are performed correctly:
							double midpoint[] = new double[2];
							midpoint[0] = (particleList.get(i).location[0] + cursorLocation[0]) / 2;
							midpoint[1] = (particleList.get(i).location[1] + cursorLocation[1]) / 2;
							
							if (((particleList.get(i).velocity[0] > 0) && midpoint[0] > particleList.get(i).location[0])
							|| ((particleList.get(i).velocity[0] < 0) && (midpoint[0] < particleList.get(i).location[0]))) {
								particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
							} else if (particleList.get(i).velocity[0] == 0) {
								particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
							}
							if (((particleList.get(i).velocity[1] > 0) && midpoint[1] > particleList.get(i).location[1])
							|| ((particleList.get(i).velocity[1] < 0) && (midpoint[1] < particleList.get(i).location[1]))) {
								particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
							} else if (particleList.get(i).velocity[1] == 0) {
								particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
							}
						}
						
						// Checking for collision with areas set by cursor:
						for(int j = 0; j < cursorParticleList.size(); j++) {
							if ((i != j) && (new Ellipse2D.Double(
							particleList.get(i).location[0] + particleList.get(i).velocity[0] - (particleSize / 2),
							particleList.get(i).location[1] + particleList.get(i).velocity[1] - (particleSize / 2),
							particleSize, particleSize)
							.intersects(cursorParticleList.get(j).location[0] - (cursorParticleList.get(j).size / 2), 
							cursorParticleList.get(j).location[1] - (cursorParticleList.get(j).size / 2), 
							cursorParticleList.get(j).size, cursorParticleList.get(j).size))) {
								particleList.get(i).particleParticleCollision = true;
								particleParticleCollisionConfirm = true;
								
								// Calculations to ensure velocity manipulations are performed correctly:
								double midpoint[] = new double[2];
								midpoint[0] = (particleList.get(i).location[0] + particleList.get(i).velocity[0] + cursorParticleList.get(j).location[0]) / 2;
								midpoint[1] = (particleList.get(i).location[1] + particleList.get(i).velocity[1] + cursorParticleList.get(j).location[1]) / 2;
								
								
								//CONTINUE HERE:
								if (((particleList.get(i).velocity[0] > 0)
								&& midpoint[0] > particleList.get(i).location[0] + particleList.get(i).velocity[0])
								|| ((particleList.get(i).velocity[0] < 0)
								&& (midpoint[0] < particleList.get(i).location[0] + particleList.get(i).velocity[0]))) {
									particleList.get(i).particleParticleVelocity[0] *= -1;
									particleList.get(i).particleParticleVelocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
									particleList.get(i).particleParticleVelocity[0] *= energyLoss;
									particleList.get(i).particleParticleVelocity[1] *= energyLoss;
									particleList.get(i).location[0] += particleList.get(i).velocity[0];
								} else if (particleList.get(i).velocity[0] == 0) {
									particleList.get(i).particleParticleVelocity[0] *= -1;
									particleList.get(i).particleParticleVelocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
									particleList.get(i).particleParticleVelocity[0] *= energyLoss;
									particleList.get(i).particleParticleVelocity[1] *= energyLoss;
									particleList.get(i).location[0] += particleList.get(i).velocity[0];
								}
								if (((particleList.get(i).velocity[1] > 0)
								&& midpoint[1] > particleList.get(i).location[1] + particleList.get(i).velocity[1])
								|| ((particleList.get(i).velocity[1] < 0)
								&& (midpoint[1] < particleList.get(i).location[1] + particleList.get(i).velocity[1]))) {
									particleList.get(i).particleParticleVelocity[1] *= -1;
									particleList.get(i).particleParticleVelocity[1] += (particleList.get(i).appliedForce[1] / particleList.get(i).mass) * particleList.get(i).time;
									particleList.get(i).particleParticleVelocity[1] *= energyLoss;
									particleList.get(i).particleParticleVelocity[0] *= energyLoss;
									particleList.get(i).location[0] += particleList.get(i).velocity[0];
								} else if (particleList.get(i).velocity[1] == 0) {
									particleList.get(i).particleParticleVelocity[1] *= -1;
									particleList.get(i).particleParticleVelocity[1] += (particleList.get(i).appliedForce[1] / particleList.get(i).mass) * particleList.get(i).time;
									particleList.get(i).particleParticleVelocity[1] *= energyLoss;
									particleList.get(i).particleParticleVelocity[0] *= energyLoss;
									particleList.get(i).location[0] += particleList.get(i).velocity[0];
								}
							}
						}
						
						// Particle-particle collisions: 
						for (int j = 0; j < numParticles; j++) {
							if ((i != j) && (new Ellipse2D.Double(
						particleList.get(i).location[0] + particleList.get(i).velocity[0] - (particleSize / 2),
						particleList.get(i).location[1] + particleList.get(i).velocity[1] - (particleSize / 2), particleSize, particleSize)
						.intersects(particleList.get(j).location[0] + particleList.get(j).velocity[0]
						- (particleSize / 2), particleList.get(j).location[1] + particleList.get(j).velocity[1]
						- (particleSize / 2), particleSize, particleSize))) {
								particleList.get(i).particleParticleCollision = true;
								particleParticleCollisionConfirm = true;
								
								// Velocity calculation:
								double tempArrayi[] = new double[2];
								double tempArrayj[] = new double[2];
								for(int k = 0; k < 2; k++) {
									tempArrayj[k] = particleList.get(i).mass * particleList.get(i).velocity[k];
									tempArrayj[k] += particleList.get(j).mass * particleList.get(j).velocity[k];
									tempArrayj[k] += particleList.get(i).mass * energyLoss * (particleList.get(i).velocity[k] - particleList.get(j).velocity[k]);
									tempArrayj[k] /= particleList.get(i).mass + particleList.get(j).mass;
									
									tempArrayi[k] = particleList.get(j).particleParticleVelocity[k];
									tempArrayi[k] += energyLoss * (particleList.get(j).velocity[k] - particleList.get(i).velocity[k]);
								}
								
								// Calculations to ensure velocity manipulations are performed correctly:
								double midpoint[] = new double[2];
								midpoint[0] = (particleList.get(i).location[0] + particleList.get(i).velocity[0] + particleList.get(j).location[0] + particleList.get(j).velocity[0]) / 2;
								midpoint[1] = (particleList.get(i).location[1] + particleList.get(i).velocity[1] + particleList.get(j).location[1] + particleList.get(j).velocity[1]) / 2;
								
								if (((particleList.get(i).velocity[0] > 0)
								&& midpoint[0] > particleList.get(i).location[0] + particleList.get(i).velocity[0])
								|| ((particleList.get(i).velocity[0] < 0)
								&& (midpoint[0] < particleList.get(i).location[0] + particleList.get(i).velocity[0]))) {
									particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
								} else if (particleList.get(i).velocity[0] == 0) {
									particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
								}
								if (((particleList.get(i).velocity[1] > 0)
								&& midpoint[1] > particleList.get(i).location[1] + particleList.get(i).velocity[1])
								|| ((particleList.get(i).velocity[1] < 0)
								&& (midpoint[1] < particleList.get(i).location[1] + particleList.get(i).velocity[1]))) {
									particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
								} else if (particleList.get(i).velocity[1] == 0) {
									particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
								}
								if (i > j) {
									particleList.get(j).particleParticleCollision = true;
									if (((particleList.get(j).velocity[0] > 0)
									&& midpoint[0] > particleList.get(j).location[0] + particleList.get(j).velocity[0])
									|| ((particleList.get(j).velocity[0] < 0)
									&& (midpoint[0] < particleList.get(j).location[0] + particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
									&& midpoint[1] > particleList.get(j).location[1] + particleList.get(j).velocity[1])
									|| ((particleList.get(j).velocity[1] < 0)
									&& (midpoint[1] < particleList.get(j).location[1] + particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
									}
								} else {
									particleList.get(j).particleParticleCollision2 = true;
									if (((particleList.get(j).velocity[0] > 0)
									&& midpoint[0] > particleList.get(j).location[0] + particleList.get(j).velocity[0])
									|| ((particleList.get(j).velocity[0] < 0)
									&& (midpoint[0] < particleList.get(j).location[0] + particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									} else if (particleList.get(j).velocity[0] == 0) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
									&& midpoint[1] > particleList.get(j).location[1] + particleList.get(j).velocity[1])
									|| ((particleList.get(j).velocity[1] < 0)
									&& (midpoint[1] < particleList.get(j).location[1] + particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
									} else if (particleList.get(j).velocity[1] == 0) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
									}
								}
							}
						}
						
						// Condition that there is a boundary collision:
						if (!collisionConfirm) {
							if ((particleList.get(i).location[0] + particleList.get(i).velocity[0] < borderCoord[0])
							&& (particleList.get(i).velocity[0] < 0)) {
								particleList.get(i).velocity[0] *= -1;
								particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).velocity[1] *= energyLoss;
								particleList.get(i).location[0] += particleList.get(i).velocity[0];
								collisionConfirm = true;
							} else if ((particleList.get(i).location[0] + particleList.get(i).velocity[0] > (borderCoord[0] + borderWidth) - particleSize)
							&& (particleList.get(i).velocity[0] > 0)) {
								particleList.get(i).velocity[0] *= -1;
								particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).velocity[1] *= energyLoss;
								particleList.get(i).location[0] += particleList.get(i).velocity[0];
								collisionConfirm = true;
							}
							if ((particleList.get(i).location[1] + particleList.get(i).velocity[1] < borderCoord[1])
							&& (particleList.get(i).velocity[1] < 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[1] *= energyLoss;
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).location[1] += particleList.get(i).velocity[1];
								collisionConfirm = true;
							} else if ((particleList.get(i).location[1] + particleList.get(i).velocity[1] > (borderCoord[1] + borderHeight) - particleSize)
							&& (particleList.get(i).velocity[1] > 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]	/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[1] *= energyLoss;
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).location[1] += particleList.get(i).velocity[1];
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
	
							particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
							particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1] / particleList.get(i).mass) * particleList.get(i).time;
	
							particleList.get(i).time += 0.001;
						}
						if (particleParticleCollisionConfirm) {
							// Location calculation after particle-particle collision:
							particleParticleCollisionConfirm = false;
							
							particleList.get(i).location[0] += particleList.get(i).velocity[0];
							particleList.get(i).location[1] += particleList.get(i).velocity[1];
	
							particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0] / particleList.get(i).mass) * particleList.get(i).time;
							particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1] / particleList.get(i).mass) * particleList.get(i).time;
	
							particleList.get(i).time += 0.001;
						}
					}
				}
				repaint();
			}
		}
	}

	// Relevant auto-generated methods:
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		cursorLocation[0] = e.getX();
		cursorLocation[1] = e.getY();
	}
	
	public void mouseClicked(MouseEvent e) {
		if(cursor) { 
			// Left Click:
			if(e.getButton() == MouseEvent.BUTTON1) {
				cursorParticle tempCursorParticle = new cursorParticle();
				tempCursorParticle.location[0] = e.getX() - (cursorSize / 2);
				tempCursorParticle.location[1] = e.getY() - (cursorSize / 2);
				tempCursorParticle.size = cursorSize;
				tempCursorParticle.color = cursorColor;
				cursorParticleList.add(tempCursorParticle);
			// Right Click:
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				for(int i = 0; i <cursorParticleList.size(); i++) {
					if(((Math.pow((e.getX() - cursorParticleList.get(i).location[0]), 2)) + (Math.pow((e.getY() - cursorParticleList.get(i).location[1]), 2))) < Math.pow((cursorParticleList.get(i).size), 2)) {
						cursorParticleList.remove(i);
					}
				}
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		cursorConfirm = true;
		emptyOldCursor = true;
	}

	public void mouseExited(MouseEvent e) {
		cursorConfirm = false;
	}
	
	// Trivial auto-generated methods:
	public void mouseDragged(MouseEvent e) {
	}



	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
