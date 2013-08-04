package round2;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

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

	public CurvedLine(World world) {
		prev = null;
		this.world = world;
		bodies = new ArrayList<Body>();
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
	}

	public void stop() {
		prev = null;
	}

	public void draw() {
		for (Body body : bodies) {
			Fixture f = body.getFixtureList();
			glColor3f(1, 1, 1);
			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();

				Vec2 v1 = edge.m_vertex1.add(body.getPosition()).mul(GameDemo.METER_SCALE);
				Vec2 v2 = edge.m_vertex2.add(body.getPosition()).mul(GameDemo.METER_SCALE);

				glBegin(GL_LINES);
				glVertex2f(v1.x, v1.y);
				glVertex2f(v2.x, v2.y);
				glEnd();

				f = f.getNext();
			}
		}
	}
}
