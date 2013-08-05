package round2;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

public class GameObjects {

	private World world;
	private List<MyBody> bodies;

	public static enum ShapeType {
		BOX, TRIANGLE, CIRCLE, LINE;

		// public ShapeType valueOf(String string) {
		// switch (string) {
		// case "BOX":
		// return BOX;
		// case "TRIANGLE":
		// return TRIANGLE;
		// case "CIRCLE":
		// return CIRCLE;
		// case "LINE":
		// return LINE;
		// default:
		// return null;
		// }
		// }
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
				glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);

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

	public void destroyBodies() {
		for (MyBody myBody : bodies) {
			Body body = myBody.getBody();
			world.destroyBody(body);
		}
		bodies.clear();
	}

	public List<MyBody> getList() {
		return bodies;
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, 0.01f, 0.0f));
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density, float friction) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, friction, 0.0f));
	}

	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density, float friction, float restitution) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, friction,
				restitution));
	}
}
