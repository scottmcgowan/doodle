package gui;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	Vec2 vec = new Vec2(0.0f, -10.0f);
	World world = new World(vec);
	BodyDef ground = new BodyDef();
	
	public MainPanel() {
		super();
		this.setPreferredSize(new Dimension(500, 500));
	}
}
