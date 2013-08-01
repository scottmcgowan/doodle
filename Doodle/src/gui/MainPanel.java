package gui;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;



@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	
	public MainPanel() {
		super();
		this.setPreferredSize(new Dimension(500, 500));
		
		Vec2 gravity = new Vec2(0.0f, -10.0f);
		boolean doSleep = true;
		World world = new World(gravity);
	}
}
