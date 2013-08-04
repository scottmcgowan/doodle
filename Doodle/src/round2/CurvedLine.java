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
	private Body body;

	public CurvedLine(World world) {
		lines = new ArrayList<EdgeShape>();
		prev = null;
	}

	public void addVertex(Vec2 vec) {
		if (prev == null) {
			prev = vec;
		} else {
			// System.out.println("Add: " +prev+", " + vec);
			EdgeShape line = new EdgeShape();
			line.set(prev, vec);
			lines.add(line);
			prev = vec;
		}
	}

	public void stop() {
		prev = null;
	}

	public void draw() {

		int i = 0;
		for (EdgeShape edge : lines) {

			Vec2 v1 = edge.m_vertex1.mul(GameDemo.METER_SCALE);
			Vec2 v2 = edge.m_vertex2.mul(GameDemo.METER_SCALE);

			// System.out.print("Edge " + i);
			// System.out.print(", " + v1);
			// System.out.println(", " + v2);
			// i++;

			glBegin(GL_LINES);
			glVertex2f(v1.x, v1.y);
			glVertex2f(v2.x, v2.y);
			glEnd();

		}
	}
}
