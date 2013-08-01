package gui;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	public MainPanel() {
		super();
		this.add(new DoodlePad());
	}
}
