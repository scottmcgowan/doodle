package gui;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class GameApp extends JApplet {

	@Override
	public void init() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setContentPane(new DoodlePad());
				setSize(501, 501);
			}
		});
	}
}
