package round2;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class CurvedLine {

	// private List<ChainShape> chains;
	private List<EdgeShape> lines;
	private Vec2 prev;
	private World world;
	private List<Body> bodies;

	public CurvedLine(World world) {
		lines = new ArrayList<EdgeShape>();
		prev = null;
		this.world = world;
		bodies = new ArrayList<Body>();
		// addVertex(new Vec2(4.0f, 1.0f));
		// addVertex(new Vec2(6.0f, 1.0f));
		// stop();

		// Body def
		BodyDef lineDef = new BodyDef();
		lineDef.type = BodyType.STATIC;

		// create body
		lineDef.position.set(5.0f, 0.5f);
		bodies.add(world.createBody(lineDef));

		Body body = bodies.get(bodies.size() - 1);

		// Shape def
		EdgeShape lineShape = new EdgeShape();
		lineShape.set(new Vec2(0.0f, 0.0f), new Vec2(1.0f, 0.0f));

		// Fixture def
		FixtureDef manFixtureDef = new FixtureDef();
		manFixtureDef.density = 1.0f;
		manFixtureDef.shape = lineShape;
		manFixtureDef.friction = 0;

		// add main fixture
		Fixture manFixture = body.createFixture(manFixtureDef);
		manFixture.setUserData("line");
		
		
		body = bodies.get(bodies.size() - 1);
		
		// Shape def
		lineShape = new EdgeShape();
		lineShape.set(new Vec2(1.0f, 0.0f), new Vec2(1.0f, 1.0f));
		
		// Fixture def
		manFixtureDef = new FixtureDef();
		manFixtureDef.density = 1.0f;
		manFixtureDef.shape = lineShape;
		manFixtureDef.friction = 0;
		
		// add main fixture
		manFixture = body.createFixture(manFixtureDef);
		manFixture.setUserData("line");
	}

	public void addVertex(Vec2 vec) {
		if (prev == null) {
			// Body def
			BodyDef lineDef = new BodyDef();
			lineDef.type = BodyType.STATIC;

			// create body
			lineDef.position.set(vec.x, vec.y);
			bodies.add(world.createBody(lineDef));
			prev = new Vec2(0.0f, 0.0f);
		} else {
			Body body = bodies.get(bodies.size() - 1);

			// Shape def
			EdgeShape lineShape = new EdgeShape();
			vec = vec.sub(body.getPosition());
			System.out.println(vec);
			lineShape.set(prev, vec);

			// Fixture def
			FixtureDef manFixtureDef = new FixtureDef();
			manFixtureDef.density = 1.0f;
			manFixtureDef.shape = lineShape;
			manFixtureDef.friction = 0;

			// add main fixture
			Fixture manFixture = body.createFixture(manFixtureDef);
			manFixture.setUserData("line");
			prev = vec;
		}

		// if (prev == null) {
		// prev = vec;
		// } else {
		// // System.out.println("Add: " +prev+", " + vec);
		// EdgeShape line = new EdgeShape();
		// line.set(prev, vec);
		// lines.add(line);
		// prev = vec;
		// }
	}

	public void stop() {
		prev = null;
	}

	public void draw() {
		for (Body body : bodies) {
			// System.out.println("BODY: " + body.getPosition());
			Fixture f = body.getFixtureList();
			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();
				// }
				//
				// int i = 0;
				// for (EdgeShape edge : lines) {

				Vec2 v1 = edge.m_vertex1.add(body.getPosition()).mul(GameDemo.METER_SCALE);
				Vec2 v2 = edge.m_vertex2.add(body.getPosition()).mul(GameDemo.METER_SCALE);

				// System.out.print("Edge " + i);
				// System.out.print(", " + v1);
				// System.out.println(", " + v2);
				// i++;

				glBegin(GL_LINES);
				glVertex2f(v1.x, v1.y);
				glVertex2f(v2.x, v2.y);
				glEnd();

				f = f.getNext();
			}
		}
	}
}
