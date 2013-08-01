package gui;

import javax.swing.JPanel;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

@SuppressWarnings("serial")
public class MainPanel extends JPanel implements Runnable{

	private static int UPDATE_RATE = 60; // number of game updates per second
	private static long UPDATE_PERIOD = 1000000000L / UPDATE_RATE; // nanoseconds
	
	public static final World world = new World(new Vec2(0.0f, -10.0f));

	
	private DoodlePad pad;
	private volatile Thread thread;

	public MainPanel() {
		super();
		world.setAllowSleep(true);
		pad = new DoodlePad(world);
		this.add(pad);
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		
		// How much to advance the simulation each second
		// 60 frames of advancement per second
		float timeStep = 1.0f / 60.f;
		
		// Accuracy of velocity simulations, higher = better
		int velocityIterations = 6;
		
		// Accuracy of position simulations, higher = better
		int positionIterations = 3;

		// For frame timer
		long beginTime, timeTaken, timeLeft;
		beginTime = System.nanoTime();
		
		// Game loop
		while (true) {
			// Update World objects
			world.step(timeStep, velocityIterations, positionIterations);
			
			
			// TODO: transition everything to World.step
			pad.updatePos();
			
			
			// Delay timer to provide the necessary delay to meet the target
			// rate
			timeTaken = System.nanoTime() - beginTime;
			timeLeft = (UPDATE_PERIOD - timeTaken) / 1000000; // in milliseconds
			if (timeLeft < 10)
				timeLeft = 10; // set a minimum
			try {
				// Provides the necessary delay and also yields control so that
				// other thread can do work.
				Thread.sleep(timeLeft);
			} catch (InterruptedException ex) {
			}
			
			beginTime = System.nanoTime();
		}
	}
}
