package model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class Player {
	
	private BufferedImage playerImg;
	
	private BodyType playerType = BodyType.DYNAMIC;
	private BodyDef playerBodyDef;
	private Vec2 pos;
	
	public Player(BodyDef bod, World world, Vec2 initPos) {
		pos = initPos;
		
		// Body definition
		playerBodyDef = new BodyDef();
		playerBodyDef.position.set(pos.x, pos.y);
		playerBodyDef.type = BodyType.DYNAMIC;

		// Define shape of the body.
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.5f;

		// Define fixture of the body.
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 0.5f;
		fd.friction = 0.3f;
		fd.restitution = 0.5f;

		world.createBody(playerBodyDef);
//		createFixture(fd);
		
		
		try {
			playerImg = ImageIO.read(new File("images/heart.png"));
		} catch (IOException e) {
			System.out.println("Player image not found");
		}
		
	}
	
	public BufferedImage getPlayerImage() {
		return playerImg;
	}
	
	public Vec2 getPos() {
		return pos;
	}
	
	
}
