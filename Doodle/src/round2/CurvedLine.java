package round2;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glLineWidth;

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

public class CurvedLine {

	private Vec2 prev;
	private World world;
	private List<Body> bodies;
	private List<Fixture> toRemove;
	public Body eraser;

	public CurvedLine(World world) {
		this.world = world;
		prev = null;
		bodies = new ArrayList<Body>();
		toRemove = new ArrayList<Fixture>();
		createEraser();
	}

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
		if (prev == null) {
			createBody(vec.x, vec.y);
			prev = new Vec2(0.0f, 0.0f);
		} else {
			Body body = bodies.get(bodies.size() - 1);
			vec = vec.sub(body.getPosition());
			createEdgeFixture(body, prev, vec);
			prev = vec;
		}
	}

	public List<Body> getLines() {
		return bodies;
	}

	public void setLines(List<Body> bodies) {
		this.bodies = bodies;
	}

	/**
	 * Ends the current curved line segment.
	 */
	public void stop() {
		prev = null;
	}

	public void fixtureToRemove(Fixture fixture) {
		toRemove.add(fixture);
	}

	public void removeFixtures() {
		for (int i = 0; i < toRemove.size(); i++) {
			Fixture f = toRemove.remove(i);
			Body body = f.getBody();
			// System.out.println(body + ", " + f);
			if (body != null) { // TEMPORARY FIX, FIGURE OUT BUG!!!
				body.destroyFixture(f);
				if (body.m_fixtureCount <= 0) {
					bodies.remove(body);
					world.destroyBody(body);
				}
			}
		}
	}

	public void draw() {
		for (Body body : bodies) {
			Fixture f = body.getFixtureList();
			glColor3f(1, 1, 1);
			glLineWidth(2.0f);
			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();

				Vec2 v1 = edge.m_vertex1.add(body.getPosition()).mul(Doodle.METER_SCALE);
				Vec2 v2 = edge.m_vertex2.add(body.getPosition()).mul(Doodle.METER_SCALE);

				glBegin(GL_LINES);
				glVertex2f(v1.x, v1.y);
				glVertex2f(v2.x, v2.y);
				glEnd();

				f = f.getNext();
			}
		}
	}

	public void drawEraser() {
		Vec2 pos = eraser.getPosition().mul(Doodle.METER_SCALE);
		float cx = pos.x;
		float cy = pos.y;
		float r = eraser.getFixtureList().getShape().getRadius() * Doodle.METER_SCALE;
		int segments = 16;

		float theta = (float) (2.0 * Math.PI / segments);
		float c = (float) Math.cos(theta);
		float s = (float) Math.sin(theta);
		float t;

		float x = r;
		float y = 0;

		glColor3f(0.8f, 0.8f, 0.8f);
		glBegin(GL_LINE_LOOP);
		for (int i = 0; i < segments; i++) {
			glVertex2f(x + cx, y + cy);

			t = x;
			x = c * x - s * y;
			y = s * t + c * y;
		}
		glEnd();
	}
}
