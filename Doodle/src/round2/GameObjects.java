package round2;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class GameObjects {

	private World world;
	private List<MyBody> bodies;

	public static enum ShapeType {
		BOX, TRIANGLE, CIRCLE, LINE;
	}

	public GameObjects(World world) {
		this.world = world;
		bodies = new ArrayList<MyBody>();
	}

	public void draw() {
		for (MyBody myBody : bodies) {
			Body body = myBody.getBody();
			if (body.getType() == BodyType.DYNAMIC) {
				glPushMatrix();

				Vec2 bodyPosition = body.getPosition().mul(Doodle.METER_SCALE);
				glTranslatef(bodyPosition.x, bodyPosition.y, 0);

				switch (myBody.getType()) {

				case BOX:
					float x = -myBody.getParam1() * Doodle.METER_SCALE;
					float y = -myBody.getParam2() * Doodle.METER_SCALE;
					float x2 = myBody.getParam1() * Doodle.METER_SCALE;
					float y2 = myBody.getParam2() * Doodle.METER_SCALE;

					glColor3f(0.25f, 0.25f, 0.25f);
					glBegin(GL_QUADS);
					glVertex2f(x, y);
					glVertex2f(x2, y);
					glVertex2f(x2, y2);
					glVertex2f(x, y2);
					glEnd();

				case CIRCLE:
					break;

				case LINE:
					break;

				case TRIANGLE:
					break;
				}

				glPopMatrix();
			}
		}
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			String userData, float density) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.type = bodyType;
		Body body = world.createBody(bodyDef);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.friction = 0.01f;

		switch (shapeType) {
		case BOX:
			PolygonShape boxShape = new PolygonShape();
			boxShape.setAsBox(param1, param2);
			fixtureDef.shape = boxShape;
			break;
		case CIRCLE:
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(param1);
			fixtureDef.shape = circleShape;
			break;
		case LINE:
			EdgeShape edgeShape = new EdgeShape();
			edgeShape.set(new Vec2(x, y), new Vec2(param1, param2));
			fixtureDef.shape = edgeShape;
			break;
		case TRIANGLE:
			break;
		}
		Fixture boxFixture = body.createFixture(fixtureDef);
		boxFixture.setUserData(userData);
		bodies.add(new MyBody(body, shapeType, param1, param2));
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			String userData, float density, float friction) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.type = bodyType;
		Body body = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.density = density;
		boxFixtureDef.friction = friction;

		switch (shapeType) {
		case BOX:
			PolygonShape boxShape = new PolygonShape();
			boxShape.setAsBox(param1, param2);
			boxFixtureDef.shape = boxShape;
			break;
		case CIRCLE:
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(param1);
			boxFixtureDef.shape = circleShape;
			break;
		case LINE:
			EdgeShape edgeShape = new EdgeShape();
			edgeShape.set(new Vec2(x, y), new Vec2(param1, param2));
			boxFixtureDef.shape = edgeShape;
			break;
		case TRIANGLE:
			break;
		}
		Fixture boxFixture = body.createFixture(boxFixtureDef);
		boxFixture.setUserData(userData);
		bodies.add(new MyBody(body, shapeType, param1, param2));
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			String userData, float density, float friction, float restitution) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.type = bodyType;
		Body body = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.density = density;
		boxFixtureDef.friction = friction;
		boxFixtureDef.restitution = restitution;

		switch (shapeType) {
		case BOX:
			PolygonShape boxShape = new PolygonShape();
			boxShape.setAsBox(param1, param2);
			boxFixtureDef.shape = boxShape;
			break;
		case CIRCLE:
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(param1);
			boxFixtureDef.shape = circleShape;
			break;
		case LINE:
			EdgeShape edgeShape = new EdgeShape();
			edgeShape.set(new Vec2(x, y), new Vec2(param1, param2));
			boxFixtureDef.shape = edgeShape;
			break;
		case TRIANGLE:
			break;
		}
		Fixture boxFixture = body.createFixture(boxFixtureDef);
		boxFixture.setUserData(userData);
		bodies.add(new MyBody(body, shapeType, param1, param2));
	}
}
