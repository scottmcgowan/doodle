package round2;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class CurvedLine {

	private Vec2 prev;
	private World world;
	private List<Body> bodies;
	private List<Fixture> toRemove;
	public Body eraser;

	/**
	 * Initializes an eraser object and a list to which curved lines can be
	 * added.
	 * 
	 * @param world
	 *            - the game world
	 */
	public CurvedLine(World world) {
		this.world = world;
		prev = null;
		bodies = new ArrayList<Body>();
		toRemove = new ArrayList<Fixture>();
		createEraser();
	}

	/**
	 * Creates the eraser body.
	 */
	private void createEraser() {
		// Eraser setup
		BodyDef eraserDef = new BodyDef();
		eraserDef.type = BodyType.DYNAMIC;
		eraserDef.position.set(5, 4);
		eraser = world.createBody(eraserDef);

		// Eraser shape
		CircleShape eraserShape = new CircleShape();
		eraserShape.setRadius(0.3f);

		// Fixture
		FixtureDef eraserFixtureDef = new FixtureDef();
		eraserFixtureDef.shape = eraserShape;
		eraserFixtureDef.density = 1f;
		eraserFixtureDef.isSensor = true;
		Fixture eraserFixture = eraser.createFixture(eraserFixtureDef);
		eraserFixture.setUserData("eraser");
	}

	/**
	 * Create a new body to which line segments can be added.
	 * 
	 * @param x
	 *            - the x position of the body
	 * @param y
	 *            - the y position of the body
	 * @return the newly created body
	 */
	public Body createBody(float x, float y) {
		// Body def
		BodyDef lineDef = new BodyDef();
		lineDef.type = BodyType.STATIC;

		// create body
		lineDef.position.set(x, y);
		Body body = world.createBody(lineDef);
		bodies.add(body);
		return body;
	}

	/**
	 * Create a new line segment on an existing body.
	 * 
	 * @param body
	 *            - the body to which the line segment will be added.
	 * @param v1
	 *            - the first vertex of the line.
	 * @param v2
	 *            - the second vertex of the line.
	 */
	public void createEdgeFixture(Body body, Vec2 v1, Vec2 v2) {
		// Shape def
		EdgeShape lineShape = new EdgeShape();
		lineShape.set(v1, v2);

		// Fixture def
		FixtureDef manFixtureDef = new FixtureDef();
		manFixtureDef.density = 1.0f;
		manFixtureDef.shape = lineShape;
		manFixtureDef.friction = 1.0f;

		// add main fixture
		Fixture manFixture = body.createFixture(manFixtureDef);
		manFixture.setUserData("line");
	}

	/**
	 * Destroy all the lines in the world in preparation to load new ones.
	 */
	public void destroyBodies() {
		for (Body b : bodies) {
			world.destroyBody(b);
		}
	}

	/**
	 * Adds the next vertex to the current curved line segment. If no such line
	 * segment exists a new segment is initialized at the given vertex.
	 * 
	 * @param vec
	 *            - a vertex to be added to the curved line segment.
	 */
	public void addVertex(Vec2 vec) {

		// Create the body to hold the lines and set the beginning vertex.
		if (prev == null) {
			createBody(vec.x, vec.y);
			prev = new Vec2(0.0f, 0.0f);
		}
		// If the line has already been started, add the next vertex.
		else {
			Body body = bodies.get(bodies.size() - 1);
			vec = vec.sub(body.getPosition());
			createEdgeFixture(body, prev, vec);
			prev = vec;
		}
	}

	public List<Body> getLines() {
		return bodies;
	}

	/**
	 * @param bodies
	 *            - the list of lines segments to be used in the game
	 */
	public void setLines(List<Body> bodies) {
		this.bodies = bodies;
	}

	/**
	 * Ends the current curved line segment.
	 */
	public void stop() {
		prev = null;
	}

	/**
	 * @param fixture
	 *            - a line segment to be removed in the next time step
	 */
	public void fixtureToRemove(Fixture fixture) {
		toRemove.add(fixture);
	}

	/**
	 * Removes lines that were selected to be erased.
	 */
	public void removeFixtures() {

		// Iterate through the removal list removing the lines from the bodies.
		for (int i = 0; i < toRemove.size(); i++) {
			Fixture f = toRemove.remove(i);
			Body body = f.getBody();
			if (body != null) { // TEMPORARY FIX, FIGURE OUT BUG!!!
				body.destroyFixture(f);

				// If a body has no more line fixtures, remove it from the
				// game world.
				if (body.m_fixtureCount <= 0) {
					bodies.remove(body);
					world.destroyBody(body);
				}
			}
		}
	}

	/**
	 * Renders the curved lines in the game window.
	 */
	public void draw() {
		// Set the color and width.
		glColor3f(0, 1, 0);
		glLineWidth(Doodle.METER_SCALE / 8);

		// Iterate through all the line bodies.
		for (Body body : bodies) {
			Fixture f = body.getFixtureList();

			glPushMatrix();
			Vec2 bodyPosition = body.getPosition().mul(Doodle.METER_SCALE);
			glTranslatef(bodyPosition.x - Doodle.TRANSLATE.x, bodyPosition.y - Doodle.TRANSLATE.y, 0);

			// Iterate through all the lines on each body.
			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();

				// Get the ending vertices for each line segment.
				Vec2 v1 = edge.m_vertex1.mul(Doodle.METER_SCALE);
				Vec2 v2 = edge.m_vertex2.mul(Doodle.METER_SCALE);

				// Draw the line from one vertex to the other.
				glBegin(GL_LINES);
				glVertex2f(v1.x, v1.y);
				glVertex2f(v2.x, v2.y);
				glEnd();

				// Get the next line segment.
				f = f.getNext();
			}
			glPopMatrix();
		}
	}

	/**
	 * Renders the eraser object when necessary.
	 */
	public void drawEraser() {

		glPushMatrix();
		// Gets the eraser's center position and radius.
		Vec2 pos = eraser.getPosition().mul(Doodle.METER_SCALE);
		glTranslatef(pos.x - Doodle.TRANSLATE.x, pos.y - Doodle.TRANSLATE.y, 0);

		glLineWidth(Doodle.METER_SCALE / 16);

		float cx = 0;
		float cy = 0;
		float r = eraser.getFixtureList().getShape().getRadius() * Doodle.METER_SCALE;
		int segments = (int) r * 4; // Sides of the polygon representing the
									// circle.

		// Calculate trig constants.
		float theta = (float) (2.0 * Math.PI / segments);
		float c = (float) Math.cos(theta);
		float s = (float) Math.sin(theta);
		float t;

		// Start at angle zero or coordinate (r, 0).
		float x = r;
		float y = 0;

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
		glPopMatrix();
	}
}
