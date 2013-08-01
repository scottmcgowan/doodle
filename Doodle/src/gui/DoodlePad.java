package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JPanel;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import model.CurvedLine;
import model.StickMan;

@SuppressWarnings("serial")
public class DoodlePad extends JPanel {
	
	StickMan man;
	CurvedLine curve;
	Point old;
	
	public static final World world = new World(new Vec2(0.0f, -10.0f));
	private static final int width = 500;
	private static final int height = 500;
	
	public DoodlePad() {
		super();
		this.setPreferredSize(new Dimension(width, height));
		this.setBackground(Color.BLUE);
		this.addMouseListener(new MListener());
		this.addMouseMotionListener(new MListener());
		curve = new CurvedLine();
		old = null;
		
		addGround(width, 10);
		addWall(0, 0, 1, 500); //Left wall
        addWall(499, 0, 1, 500); //Right wall
        
		man = new StickMan(100, 30);
	}
	
	//This method adds ground. 
    public static void addGround(float width, float height){
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(width,height);
        
        FixtureDef fd = new FixtureDef();
        fd.shape = ps;

        BodyDef bd = new BodyDef();
        bd.position= new Vec2(0.0f,-10f);

        world.createBody(bd).createFixture(fd);
    }
    
    //This method creates a walls. 
    public static void addWall(float posX, float posY, float width, float height){
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(width,height);
        
        FixtureDef fd = new FixtureDef();
        fd.shape = ps;
        fd.density = 1.0f;
        fd.friction = 0.3f;    

        BodyDef bd = new BodyDef();
        bd.position.set(posX, posY);
        
        world.createBody(bd).createFixture(fd);
    }

    // Updates object positions
    public void updatePos() {
    	boolean toFall = true;
    	List<Line2D.Double> lines = curve.getLine();
    	for (Line2D.Double l: lines) {
    		if (l.intersects(man)) {
    			toFall = false;
    		}
    	} if (toFall) {
    		man.fall();
    	}
    	repaint();
    }
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		List<Line2D.Double> lines = curve.getLine();
		for (Line2D.Double l: lines) {
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2d.draw(l);
		}
		g2d.setColor(Color.DARK_GRAY);
		g2d.fill(man);
	}
	
	public class MListener extends MouseAdapter {
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (old == null) {
				old = e.getPoint();
			}
			curve.addLine(old, e.getPoint());
			old = e.getPoint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			old = null;
		}
	}
}