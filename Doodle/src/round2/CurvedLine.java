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
import org.jbox2d.common.Vec2;

public class CurvedLine {

	// private List<ChainShape> chains;
	private ChainShape chain;
	private Vec2 prev;
	private Vec2[] vecs = { new Vec2(0, 0), new Vec2(10, 10), new Vec2(100, 20), new Vec2(200, 400) };

	public CurvedLine() {
		// chains = new ArrayList<ChainShape>();
		chain = new ChainShape();
		chain.createChain(vecs, vecs.length);
		prev = vecs[2];
	}

	public void addVertex(Vec2 vec) {
		if (prev == null) {
			System.out.println("new chain");
			Vec2[] start = { vec };
			// ChainShape chain = new ChainShape();
			chain.createChain(start, 1);
			// chains.add(chain);
			prev = vec;
		} else {
			System.out.println("old chain");
			// ChainShape chain = chains.get(chains.size() - 1);
//			chain.setPrevVertex(prev);
			while(chain.m_hasNextVertex){
				
			}
			chain.setNextVertex(vec);
			prev = vec;
		}
	}

	public void stop() {
		prev = null;
	}

	public void draw() {

		int n = chain.getChildCount();
		if (n > 0) {
			// for (ChainShape chain : chains) {
			for (int i = 0; i < n; i++) {
				EdgeShape edge = new EdgeShape();
				 chain.getChildEdge(edge, i);
//				 System.out.print("Edge " + i);
//				 System.out.print(": " + edge.m_vertex0);
//				 System.out.print(", " + edge.m_vertex1);
//				 System.out.print(", " + edge.m_vertex2);
//				 System.out.println(edge.m_vertex3);

				glBegin(GL_LINES);
				glVertex2f(edge.m_vertex1.x, edge.m_vertex1.y);
				glVertex2f(edge.m_vertex2.x, edge.m_vertex2.y);
				glEnd();

			}
			// }
		}
	}
}
