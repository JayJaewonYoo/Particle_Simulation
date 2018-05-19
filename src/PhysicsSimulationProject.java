import java.awt.BorderLayout;

import javax.swing.JFrame;

class particle {
	// solid will push liquids out of the way but will not penetrate immovables (immovables are basically walls)
		// possibly have liquids as just smaller sized solids and treat solids as particles
	double appliedForce[];
		// negative indicates move left, positive indicates move right
		// negative indicates move down, positive indicates move up
		// the image will update to the location horizontal to the right and vertical up from its current location
	double velocity[] = new double[2];
	double time;
	int location[] = new int[2]; 
		// location has [x, y] location of center of circle.
	byte mass = 1; // possibly remove
	boolean particleParticleCollision;
	boolean particleParticleCollision2;
	double particleParticleVelocity[] = new double[2];
}

public class PhysicsSimulationProject {
	public static void main(String[] args) {
		
		JFrame screen = new JFrame();
		
		GUI simulation = new GUI();
		UserInput userInput = new UserInput();
		screen.setBounds(0, 0, 900, 738);
		screen.setTitle("Physics Simulator.");
		screen.setResizable(false);
		screen.setVisible(true);
		screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		screen.add(simulation, BorderLayout.CENTER);
		screen.add(userInput, BorderLayout.EAST);
	}
}