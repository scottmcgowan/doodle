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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.text.html.ImageView;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import model.CurvedLine;
import model.Player;
import model.StickMan;
import model.Utils;

@SuppressWarnings("serial")
public class DoodlePad extends JPanel {
	
	StickMan man;
	CurvedLine curve;
	Point old;
	
	private Body player;
	
	private static World world;
	
	private static final int width = 500;
	private static final int height = 500;

	
	
	/*
	 * TODO:
	 * JBox2D engine uses meters for all calculations,
	 * but we use pixels for drawing.  All game logic should 
	 * be calculated in meters, and then converted to px for
	 * drawing to the screen.  This will require efficient
	 * decoupling of the model (game logic) and view (gui).
	 * 
	 * This also requires that a scale be developed to convert
	 * px <--> m ... 50px == 1m seems like a safe place to
	 * start, which would make a human character ~89px high.
	 */
	
	/*
	 * TODO:
	 * All kinds of stuff is messed up.  Whoops.
	 */
	
	/*
	 * TODO:
	 * GRAVITY IS BACKWARDS?!
	 */
	
	
	
	public DoodlePad (World world) {
		super();
		this.setPreferredSize(new Dimension(500, 500));
		this.setBackground(Color.WHITE);
		this.addMouseListener(new MListener());
		this.addMouseMotionListener(new MListener());
		DoodlePad.world = world;
		curve = new CurvedLine();
		old = null;
		
		addGround(100, 10);
		addWall(0, 0, 1, 100); //Left wall
        addWall(100, 0, 1, 100); //Right wall
        
       buildPlayer();
        
//		man = new StickMan(100, 30);
	}
	
	
	private BufferedImage playerImg;
	// This method creates the player object
	// TODO: Move this to a Player class?
	public void buildPlayer() {

		//body definition
		BodyDef bd = new BodyDef();
		bd.position.set(1, 30);  // 1m right, 30m up!
		bd.type = BodyType.DYNAMIC;
		 
		//define shape of the body.
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.5f;  
		 
		//define fixture of the body.
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 0.5f;
		fd.friction = 0.3f;        
		fd.restitution = 0.5f;
		 
		//create the body and add fixture to it
		player =  world.createBody(bd);
		player.createFixture(fd);
		
		try {
			playerImg = ImageIO.read(new File("images/heart.png"));
		} catch (IOException e) {
			System.out.println("Player image not found");
		}
	
		
	}
	
	public Vec2 getPlayerPos() {
		return player.getPosition();
	}
	
	public Point getPlayerPosInt() {
		
		Float x = Utils.toPixelPosX(player.getPosition().x);
		Float y = Utils.toPixelPosY(player.getPosition().y);
		
		int x2 = Math.round(x);
		int y2 = Math.round(y);
		Point pos = new Point(x2, y2);
		
		return pos;
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
		
		
		g2d.drawImage(playerImg, getPlayerPosInt().x, getPlayerPosInt().y, 30, 30, this);
		g2d.drawImage(playerImg, 50, 120, 30, 30, this);
		
		
		// WHERRE AM I?!
		// Debug prints for spacing
		g2d.drawString("50px abs",50, 10);
		g2d.drawString("50px", Utils.toPosX(50), 30);
		g2d.drawString("5m", Utils.toPixelPosX(250), 50);
		
//		g2d.setColor(Color.DARK_GRAY);
//		g2d.fill(man);
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
