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
		this.setPreferredSize(new Dimension(500, 500));
		
		// Create world
		vec = new Vec2(0.0f, -10.0f);
		world = new World(vec);
		
		// Create ground
		groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0.0f, -10.0f);
		groundBody = world.createBody(groundBodyDef);
		groundBox = new PolygonShape();
		groundBox.setAsBox(50.0f, 10.0f);
		groundBody.createFixture(groundBox, 0.0f);
		
		// Create dynamic body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(0.0f, 4.0f);
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
		
		// set time step
		float timeStep = 1.0f / 60.0f;
		
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
