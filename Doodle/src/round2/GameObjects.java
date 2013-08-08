package round2;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
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

/**
 * A class containing all inanimate game objects.
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class GameObjects {

	private World world;
	private List<MyBody> bodies;

	/**
	 * An enum for the different shape types a game object can have.
	 */
	public static enum ShapeType {
		BOX, TRIANGLE, CIRCLE, LINE;
	}

	/**
	 * 
	 * 
	 * @param world
	 */
	public GameObjects(World world) {
		this.world = world;
		bodies = new ArrayList<MyBody>();
	}

	/**
	 * Renders all inanimate game objects.
	 */
	public void draw() {
		// Iterate through the game objects
		for (MyBody myBody : bodies) {
			Body body = myBody.getBody();

			// Render stationary objects
			if (body.getType() == BodyType.STATIC) {
				glColor3f(0.4f, 0.4f, 0.4f);
			}

			// Render moveable objects not affected by physics.
			if (body.getType() == BodyType.KINEMATIC) {}

			// Render moveable objects.
			if (body.getType() == BodyType.DYNAMIC) {
				glColor3f(0.25f, 0.25f, 0.25f);
			}

			// Save the current matrix data before translation/rotation.
			glPushMatrix();

			// Get the object's center and move the rendering matrix.
			Vec2 bodyPosition = body.getPosition().mul(Doodle.METER_SCALE);
			glTranslatef(bodyPosition.x - Doodle.TRANSLATE.x, bodyPosition.y - Doodle.TRANSLATE.y, 0);
			glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);

			// Draw each shape accordingly.
			switch (myBody.getType()) {

			case BOX:
				// Get the coordinates for each edge of the box.
				float x = -myBody.getParam1() * Doodle.METER_SCALE;
				float y = -myBody.getParam2() * Doodle.METER_SCALE;
				float x2 = myBody.getParam1() * Doodle.METER_SCALE;
				float y2 = myBody.getParam2() * Doodle.METER_SCALE;

				// Set the color then draw the box by vertex.
				glBegin(GL_QUADS);
				glVertex2f(x, y);
				glVertex2f(x2, y);
				glVertex2f(x2, y2);
				glVertex2f(x, y2);
				glEnd();
				break;

			case CIRCLE:
				// Gets the circle's center position and radius.
				float cx = 0;
				float cy = 0;
				float r = body.getFixtureList().getShape().getRadius() * Doodle.METER_SCALE;
				// System.out.println(r);
				int segments = (int) r * 2; // Sides of the polygon
											// representing the circle.

				// Calculate trig constants.
				float theta = (float) (2.0 * Math.PI / segments);
				float c = (float) Math.cos(theta);
				float s = (float) Math.sin(theta);
				float t;

				// Start at angle zero or coordinate (r, 0).
				x = r;
				y = 0;

				// Set color and render polygon.
				glColor3f(0.8f, 0.8f, 0.8f);
				glBegin(GL_LINE_LOOP);

				// Rotate through the vertices
				for (int i = 0; i < segments; i++) {
					glVertex2f(x + cx, y + cy); // Output vertex.

					// Apply a rotation matrix.
					t = x;
					x = c * x - s * y;
					y = s * t + c * y;
				}
				glEnd();
				break;

			case LINE:
				// TODO: Render lines.
				break;

			case TRIANGLE:
				// TODO: Render triangles (if needed for game).
				break;
			}

			// Restore matrix to its previous state.
			glPopMatrix();

		}
	}

	/**
	 * Destroys all game objects in preparation to load new ones.
	 */
	public void destroyBodies() {
		for (MyBody myBody : bodies) {
			Body body = myBody.getBody();
			world.destroyBody(body);
		}
		bodies.clear();
	}

	/**
	 * @return the list of game objects
	 */
	public List<MyBody> getList() {
		return bodies;
	}

	/**
	 * Creates a new MyBody element and adds it to the game world.
	 * 
	 * @param shapeType
	 *            - the shape of the object
	 * @param bodyType
	 *            - the body type: static, kinematic, dynamic
	 * @param x
	 *            - the x coordinate position
	 * @param y
	 *            = the y coordinate position
	 * @param param1
	 *            - the width or radius of the body
	 * @param param2
	 *            - the height of the body (if necessary)
	 * @param angle
	 *            - the angle in radians of the body
	 * @param userData
	 *            - a string to identify the object
	 * @param density
	 *            - the density of the object, usually in kg/m^2
	 */
	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, 0.01f, 0.0f));
	}

	/**
	 * Creates a new MyBody element and adds it to the game world.
	 * 
	 * @param shapeType
	 *            - the shape of the object
	 * @param bodyType
	 *            - the body type: static, kinematic, dynamic
	 * @param x
	 *            - the x coordinate position
	 * @param y
	 *            - the y coordinate position
	 * @param param1
	 *            - the width or radius of the body
	 * @param param2
	 *            - the height of the body (if necessary)
	 * @param angle
	 *            - the angle in radians of the body
	 * @param userData
	 *            - a string to identify the object
	 * @param density
	 *            - the density of the object, usually in kg/m^2
	 * @param friction
	 *            - the friction coefficient, usually in the range [0, 1]
	 */
	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density, float friction) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, friction,
				0.0f));
	}

	/**
	 * Creates a new MyBody element and adds it to the game world.
	 * 
	 * @param shapeType
	 *            - the shape of the object
	 * @param bodyType
	 *            - the body type: static, kinematic, dynamic
	 * @param x
	 *            - the x coordinate position
	 * @param y
	 *            - the y coordinate position
	 * @param param1
	 *            - the width or radius of the body
	 * @param param2
	 *            - the height of the body (if necessary)
	 * @param angle
	 *            - the angle in radians of the body
	 * @param userData
	 *            - a string to identify the object
	 * @param density
	 *            - the density of the object, usually in kg/m^2
	 * @param friction
	 *            - the friction coefficient, usually in the range [0, 1]
	 * @param restitution
	 *            - the restitution (elasticity), usually in the range [0, 1]
	 */
	public void createObject(ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density, float friction, float restitution) {

		bodies.add(new MyBody(world, shapeType, bodyType, x, y, param1, param2, angle, userData, density, friction,
				restitution));
	}
}
