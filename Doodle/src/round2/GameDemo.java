package round2;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashSet;
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

/**
 * 
 * @author Logan
 * 
 */
public class GameDemo {

	private static final String WINDOW_TITLE = "JBox Demo!";
	private static final int[] WINDOW_DIMENSIONS = { 960, 720 };

	public static final float METER_SCALE = 32.0f; // 64 pixels = 1 meter?

	private static World world = new World(new Vec2(0.0f, -9.8f));
	private static Set<Body> bodies = new HashSet<Body>();
	private static Body ground;
	private static StickMan man;
	private static CurvedLine lines;
	private static int numFootContacts;
	static int jumpWait;

	private static void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		for (Body body : bodies) {
			if (body.getType() == BodyType.DYNAMIC) {
				glPushMatrix();
				
				Vec2 bodyPosition = body.getPosition().mul(METER_SCALE);
				glTranslatef(bodyPosition.x, bodyPosition.y, 0);
				glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);

				float x = -0.25f * METER_SCALE;
				float y = -0.5f * METER_SCALE;
				float x2 = 0.25f * METER_SCALE;
				float y2 = 0.5f * METER_SCALE;

				glColor3f(0.25f, 0.25f, 0.25f);
				glBegin(GL_QUADS);
				glVertex2f(x, y);
				glVertex2f(x2, y);
				glVertex2f(x2, y2);
				glVertex2f(x, y2);
				glEnd();
				
				glPopMatrix();
			}
		}
		lines.draw();
		man.draw();
	}

	private static void logic() {
		world.step(1 / 60f, 8, 3);
		if (jumpWait > 0)
			jumpWait--;
	}

	private static void input() {
		// Traverse
		if (Keyboard.isKeyDown(Keyboard.KEY_A) && !Keyboard.isKeyDown(Keyboard.KEY_D)) {
			man.move("left");
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D) && !Keyboard.isKeyDown(Keyboard.KEY_A)) {
			man.move("right");
		} else
			man.move("stop");

		// Jump
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			if (numFootContacts >= 1 && jumpWait == 0) {
				jumpWait = 2;
				man.move("jump");
			}
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			System.out.println(man.getPosition());
		}

		// Draw lines
		if (Mouse.isButtonDown(0)) {
			System.out.println(new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / METER_SCALE).mul(1 / 3f));
			lines.addVertex(new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / METER_SCALE).mul(1 / 3f));
		} else
			lines.stop();
	}

	private static void cleanUp(boolean asCrash) {
		Display.destroy();
		System.exit(asCrash ? 1 : 0);
	}

	private static void setUpMatrices() {
		glMatrixMode(GL_PROJECTION);
		glOrtho(0, 320, 0, 240, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	private static void setUpObjects() {
		numFootContacts = 0;
		jumpWait = 0;

		man = new StickMan(world, 0.25f, 0.5f);
		lines = new CurvedLine(world);

		BodyDef groundDef = new BodyDef();
		groundDef.position.set(0, 0);
		groundDef.type = BodyType.STATIC;
		PolygonShape groundShape = new PolygonShape();
		groundShape.setAsBox(1000, 0);
		ground = world.createBody(groundDef);
		FixtureDef groundFixtureDef = new FixtureDef();
		groundFixtureDef.density = 1;
		groundFixtureDef.shape = groundShape;
		groundFixtureDef.friction = 0.01f;
		Fixture groundFixture = ground.createFixture(groundFixtureDef);
		groundFixture.setUserData("ground");
		bodies.add(ground);

		BodyDef boxDef = new BodyDef();
		boxDef.position.set(320 / METER_SCALE / 2 + 1, 240 / METER_SCALE / 2 + 1);
		boxDef.type = BodyType.DYNAMIC;
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(0.25f, 0.5f);
		Body box = world.createBody(boxDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.density = 0.6f;
		boxFixtureDef.shape = boxShape;
		boxFixtureDef.friction = 6f;
		Fixture boxFixture = box.createFixture(boxFixtureDef);
		boxFixture.setUserData("box");
		bodies.add(box);

		// World
		world.setContactListener(new MyContactListener());
	}

	private static void update() {
		Display.update();
		Display.sync(60);
	}

	private static void enterGameLoop() {
		while (!Display.isCloseRequested()) {
			render();
			logic();
			input();
			update();
		}
	}

	private static void setUpDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(WINDOW_DIMENSIONS[0], WINDOW_DIMENSIONS[1]));
			Display.setTitle(WINDOW_TITLE);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			cleanUp(true);
		}
	}

	public static class MyContactListener implements ContactListener {

		@Override
		public void beginContact(Contact contact) {
			Object fixtureUserData = contact.getFixtureA().getUserData();
			if (fixtureUserData.equals("foot")) {
				numFootContacts++;
			}
			fixtureUserData = contact.getFixtureB().getUserData();
			if (fixtureUserData.equals("foot")) {
				numFootContacts++;
			}
		}

		@Override
		public void endContact(Contact contact) {
			Object fixtureUserData = contact.getFixtureA().getUserData();
			if (fixtureUserData.equals("foot")) {
				numFootContacts--;
			}
			fixtureUserData = contact.getFixtureB().getUserData();
			if (fixtureUserData.equals("foot")) {
				numFootContacts--;
			}
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}

	}

	public static void main(String[] args) {
		setUpDisplay();
		setUpObjects();
		setUpMatrices();
		enterGameLoop();
		cleanUp(false);
	}
}
