package model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

@SuppressWarnings("serial")
public class StickMan extends Rectangle2D.Double {

	private Dimension size = new Dimension(30, 70);
	private World world;
	private BodyDef bodyDef;
	private Body body;
	private PolygonShape rect;
	private int yStart;

	public StickMan(int x, int y, World world) {
		super(x, y, 30, 70);
		this.world = world;
		yStart = y * 2;

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x, y);
		body = this.world.createBody(bodyDef);

		Vec2[] vertices = new Vec2[4];
		vertices[0] = new Vec2(x, y);
		vertices[1] = new Vec2(x + size.width, y);
		vertices[2] = new Vec2(x + size.width, y + size.height);
		vertices[3] = new Vec2(x, y + size.height);
		int count = 4;

		rect = new PolygonShape();
		rect.set(vertices, count);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = rect;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.3f;
		body.createFixture(fixtureDef);
	}

	public void setPosition(Vec2 pos) {
		bodyDef.position.set(pos);
	}

	public float getAngle() {
		return body.getAngle();
	}
	
	public Vec2 getPosition() {
		return body.getPosition();
	}

	public void setLocation(int x, int y) {}

	public void updateRect() {
		Vec2 vec = body.getPosition();
		System.out.print(yStart - vec.y);
		super.setFrame(vec.x, yStart - vec.y, size.width, size.height);
		System.out.println("  Rect Y pos: " + this.getY());
	}
}
