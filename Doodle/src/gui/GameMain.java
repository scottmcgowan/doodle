package gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import test.JBoxHelloWorld;

public class GameMain {

	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame game = new JFrame("Drawing Game!");
				game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				game.setContentPane(new JBoxHelloWorld());
				game.pack();
				game.setResizable(false);
				game.setLocationRelativeTo(null);
				game.setVisible(true);
			}
		});
	}
}
