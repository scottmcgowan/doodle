package gui;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MainPanel extends JPanel implements Runnable{

	private static int UPDATE_RATE = 60; // number of game updates per second
	private static long UPDATE_PERIOD = 1000000000L / UPDATE_RATE; // nanoseconds

	
	private DoodlePad pad;
	private volatile Thread thread;

	public MainPanel() {
		super();
		System.out.println("debug");
		pad = new DoodlePad();
		this.add(pad);
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		// Regenerate the game objects for a new game

		// Game loop
		long beginTime, timeTaken, timeLeft;
		
		beginTime = System.nanoTime();
		while (true) {
			// Refresh the display
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
