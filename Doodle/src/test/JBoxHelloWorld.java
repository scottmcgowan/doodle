package test;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;


@SuppressWarnings("serial")
public class JBoxHelloWorld extends JPanel {
	
	Vec2 vec;
	World world;
	BodyDef groundBodyDef;
	Body groundBody;
	PolygonShape groundBox;
	BodyDef bodyDef;
	Body body;
	PolygonShape dynamicBox;
	FixtureDef fixtureDef;
	
	public JBoxHelloWorld() {
		super();
		setPreferredSize(new Dimension(500, 500));
		// Create world
		vec = new Vec2(0.0f, -10.0f);
		world = new World(vec);
		world.setAllowSleep(true);
		createGround();
		createBody(0.0f, 4.0f, BodyType.DYNAMIC);
		setTimeStep(1.0f / 60.0f);
	}
	
	private void createGround() {
		// Create ground
		groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0.0f, -10.0f);
		groundBody = world.createBody(groundBodyDef);
		groundBox = new PolygonShape();
		groundBox.setAsBox(50.0f, 10.0f);
		groundBody.createFixture(groundBox, 0.0f);
	}
	
	private void createBody(float x, float y, BodyType type) {
		// Create dynamic body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = type;
		bodyDef.position.set(x, y);
		body = world.createBody(bodyDef);
		
		// attach shape to body
		dynamicBox = new PolygonShape();
		dynamicBox.setAsBox(1.0f, 1.0f);
		
		// create fixture definition
		fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicBox;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.3f;
		body.createFixture(fixtureDef);
	}
	
	private void setTimeStep(float timeStep) {
		// set time step
		int velocityIterations = 6;
		int positionIterations = 2;
		
		for (int i = 0; i < 60; ++i) {
			world.step(timeStep, velocityIterations, positionIterations);
			Vec2 position = body.getPosition();
			float angle = body.getAngle();
			System.out.println(position.x + " " + position.y + " " + angle);
		}
	}
}
