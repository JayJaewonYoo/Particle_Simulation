import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

class particle {
	double appliedForce[];
		// Negative indicates move left, positive indicates move right.
		// Negative indicates move down, positive indicates move up.
		// The image will update to the location horizontal to the right and vertical up from its current location.
	double velocity[] = new double[2];
	double time;
	int location[] = new int[2]; 
		// Location has [x, y] location of center of circle.
	byte mass = 1;
	// Following are required variables that determine whether collisions have occurred. 
	boolean particleParticleCollision;
	boolean particleParticleCollision2;
	double particleParticleVelocity[] = new double[2];
}

class cursorParticle {
	int location[] = new int[2];
	int size;
	Color color;
}

public class PhysicsSimulationProject {
	public static void main(String[] args) {
		GUI simulation = new GUI();
		UserInput userInput = new UserInput();

		JFrame screen = new JFrame();
		screen.setBounds(0, 0, 900, 738);
		screen.setTitle("Particle Physics Simulator");
		screen.setResizable(false);
		screen.setVisible(true);
		screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screen.add(simulation, BorderLayout.CENTER);
		screen.add(userInput, BorderLayout.EAST);
	}
}