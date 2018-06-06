import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
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

public class GUI extends JPanel implements /*KeyListener, */ActionListener, MouseListener, MouseMotionListener { // Remove keylistener if not using keys
	private static final long serialVersionUID = 6040042718732238432L;
	
	static boolean run = false;
	static boolean overlap = false;
	static boolean pause = false;
	static boolean showVector = false;
	static boolean randomColors = false;
	static boolean cursor = false;
	private boolean cursorConfirm = false;
	private byte cursorMass = 1;
	
	static Timer timer;
	static double delay = 8; // 8 is standard speed
	
	static int numParticles = 50; 
	static int particleSize = 11;
	static int cursorSize = 20;
	static double energyLoss = 0.7;
	
	static int borderCoord[] = {0, 0}; // location of top left corner of border. 
	
	static ArrayList<particle> particleList = new ArrayList<particle>();
	
	// Vector arrow variables:
	Path2D.Double path;
	AffineTransform t;
	double angle;
	
	// Variables used for particle-particle collision detection:
	static boolean particleParticleCollisionConfirm = false;
	
	static Color particleColor = Color.black;
	static Color backgroundColor = Color.white;
	static Color vectorColor = Color.black;
	static Color cursorColor = Color.black;
	
	private int borderWidth = 685;
	private int borderHeight = 700;
	private double cursorLocation[] = new double[2];
	private double oldCursorLocation[] = new double[2];
	private double cursorVelocity[] = new double[2];
	private boolean emptyOldCursor = true;
	
	// Variables used for boundary collision detection
	static boolean collisionConfirm = false;
	Rectangle2D boundaries1 = new Rectangle2D.Double(borderCoord[0], borderCoord[1], 3, borderWidth + 15);
	Rectangle2D boundaries2 = new Rectangle2D.Double(borderCoord[0], borderCoord[1], borderWidth, 3);
	Rectangle2D boundaries3 = new Rectangle2D.Double(borderCoord[0] + borderWidth - 5, borderCoord[1], 3, borderHeight + 3);
	Rectangle2D boundaries4 = new Rectangle2D.Double(borderCoord[0], borderCoord[1] + borderHeight, borderWidth, 3);
	Rectangle2D boundaries = boundaries1.createUnion(boundaries2.createUnion(boundaries3.createUnion(boundaries4)));
	
	// for these forces if mass is being used, set the following values as accelerations.
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
				if(new Ellipse2D.Double(tempLocationArray[0] - (particleSize/2), tempLocationArray[1] - (particleSize/2), particleSize + 1, particleSize + 1).intersects
						(particleList.get(j).location[0] - (particleSize/2), particleList.get(j).location[1] - (particleSize/2), particleSize + 1, particleSize + 1)) {
				
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
			}
			else {
				particleList.get(i).location = tempLocationArray;
			}
			particleList.get(i).appliedForce = naturalForceCalc();
		}
	}
	
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
		//addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		timer = new Timer((int) delay, this);
		timer.start();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		
		Random randomInt = new Random();
		
		// Background:
		g2d.setColor(UIManager.getColor("Panel.background"));;
		g2d.fillRect(0, 0, 730, borderHeight + 3);

		// Background within Borders:
		/*if(randomColors) {
			Field tempField;
			try {
				tempField = Class.forName("java.awt.Color").getField((UserInput.colorOptions[randomInt.nextInt(9)].toLowerCase()));
				//g2d.setColor((Color)tempField.get(null));
				randomInt = new Random();
			} catch (Exception e1) {
				g2d.setColor(Color.black);
				e1.printStackTrace();
			}
		}
		else {*/
			g2d.setColor(backgroundColor);
		//}
		g2d.fillRect(borderCoord[0] + 1, borderCoord[1] + 1, borderWidth, borderHeight);
		
		// Borders:
		g2d.setColor(Color.black);
		g2d.fillRect(borderCoord[0], borderCoord[1], 3, borderWidth + 15);
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
		
		// Particle:
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
		
		g2d.dispose();
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
					}
					// Move particles here:
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
						// Checking for collision with existing cursor
						if(cursor && (new Ellipse2D.Double(
								particleList.get(i).location[0] - (particleSize / 2),
								particleList.get(i).location[1] - (particleSize / 2),
								particleSize, particleSize)
								.intersects(cursorLocation[0] - (cursorSize / 2), cursorLocation[1]
								- (cursorSize / 2), cursorSize, cursorSize))) {

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
							
							// Velocity calculation begins here:
							double tempArrayi[] = new double[2];
							for(int k = 0; k < 2; k++) {
								tempArrayi[k] = cursorMass * cursorVelocity[k];
								tempArrayi[k] += particleList.get(i).mass * particleList.get(i).velocity[k];
								tempArrayi[k] += cursorMass * energyLoss * (cursorVelocity[k] - particleList.get(i).velocity[k]);
								tempArrayi[k] /= cursorMass + particleList.get(i).mass;
							}
							
							double midpoint[] = new double[2];
							midpoint[0] = (particleList.get(i).location[0] + cursorLocation[0]) / 2;
							midpoint[1] = (particleList.get(i).location[1] + cursorLocation[1]) / 2;
							
							// CONTINUE HERE, REMOVE  + VELOCITIES
							
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
						
						
						for (int j = 0; j < numParticles; j++) {
							if ((i != j) && (new Ellipse2D.Double(
									particleList.get(i).location[0] + particleList.get(i).velocity[0] - (particleSize / 2),
									particleList.get(i).location[1] + particleList.get(i).velocity[1] - (particleSize / 2),
									particleSize, particleSize)
									.intersects(particleList.get(j).location[0] + particleList.get(j).velocity[0]
									- (particleSize / 2), particleList.get(j).location[1] + particleList.get(j).velocity[1]
									- (particleSize / 2), particleSize, particleSize))) {
								particleList.get(i).particleParticleCollision = true;
								particleParticleCollisionConfirm = true;
								
								// Velocity calculation begins here:
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
									particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
								} else if (particleList.get(i).velocity[0] == 0) {
									particleList.get(i).particleParticleVelocity[0] = tempArrayi[0];
								}
								if (((particleList.get(i).velocity[1] > 0)
										&& midpoint[1] > particleList.get(i).location[1] + particleList.get(i).velocity[1])
										|| ((particleList.get(i).velocity[1] < 0)
												&& (midpoint[1] < particleList.get(i).location[1]
														+ particleList.get(i).velocity[1]))) {
									particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
								} else if (particleList.get(i).velocity[1] == 0) {
									particleList.get(i).particleParticleVelocity[1] = tempArrayi[1];
								}
								if (i > j) {
									particleList.get(j).particleParticleCollision = true;
									if (((particleList.get(j).velocity[0] > 0)
											&& midpoint[0] > particleList.get(j).location[0]
													+ particleList.get(j).velocity[0])
											|| ((particleList.get(j).velocity[0] < 0)
													&& (midpoint[0] < particleList.get(j).location[0]
															+ particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
											&& midpoint[1] > particleList.get(j).location[1]
													+ particleList.get(j).velocity[1])
											|| ((particleList.get(j).velocity[1] < 0)
													&& (midpoint[1] < particleList.get(j).location[1]
															+ particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
									}
								} else {
									particleList.get(j).particleParticleCollision2 = true;
									if (((particleList.get(j).velocity[0] > 0)
											&& midpoint[0] > particleList.get(j).location[0]
													+ particleList.get(j).velocity[0])
											|| ((particleList.get(j).velocity[0] < 0)
													&& (midpoint[0] < particleList.get(j).location[0]
															+ particleList.get(j).velocity[0]))) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									} else if (particleList.get(j).velocity[0] == 0) {
										particleList.get(j).particleParticleVelocity[0] = tempArrayj[0];
									}
									if (((particleList.get(j).velocity[1] > 0)
											&& midpoint[1] > particleList.get(j).location[1]
													+ particleList.get(j).velocity[1])
											|| ((particleList.get(j).velocity[1] < 0)
													&& (midpoint[1] < particleList.get(j).location[1]
															+ particleList.get(j).velocity[1]))) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
									} else if (particleList.get(j).velocity[1] == 0) {
										particleList.get(j).particleParticleVelocity[1] = tempArrayj[1];
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
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).location[0] += particleList.get(i).velocity[0];
								collisionConfirm = true;
							} else if ((particleList.get(i).location[0]
									+ particleList.get(i).velocity[0] > (borderCoord[0] + borderWidth) - particleSize)
									&& (particleList.get(i).velocity[0] > 0)) {
								particleList.get(i).velocity[0] *= -1;
								particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[0] *= energyLoss;
								particleList.get(i).location[0] += particleList.get(i).velocity[0];
								collisionConfirm = true;
							}
							if ((particleList.get(i).location[1] + particleList.get(i).velocity[1] < borderCoord[1])
									&& (particleList.get(i).velocity[1] < 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
										/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[1] *= energyLoss;
								particleList.get(i).location[1] += particleList.get(i).velocity[1];
								collisionConfirm = true;
							} else if ((particleList.get(i).location[1]
									+ particleList.get(i).velocity[1] > (borderCoord[1] + borderHeight) - particleSize)
									&& (particleList.get(i).velocity[1] > 0)) {
								particleList.get(i).velocity[1] *= -1;
								particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]	/ particleList.get(i).mass) * particleList.get(i).time;
								particleList.get(i).velocity[1] *= energyLoss;
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
	
							particleList.get(i).velocity[0] += (particleList.get(i).appliedForce[0]
									/ particleList.get(i).mass) * particleList.get(i).time;
							particleList.get(i).velocity[1] += (particleList.get(i).appliedForce[1]
									/ particleList.get(i).mass) * particleList.get(i).time;
	
							particleList.get(i).time += 0.001;
						}
						if (particleParticleCollisionConfirm) {
							// Location calculation after particle-particle collision:
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
	
	/*@Override
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
		
	}*/

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		cursorLocation[0] = e.getX();
		cursorLocation[1] = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		cursorConfirm = true;
		emptyOldCursor = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		cursorConfirm = false;
	}
}
