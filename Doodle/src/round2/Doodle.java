package round2;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jdom2.JDOMException;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import round2.GameObjects.ShapeType;
import utility.SaveTools;

/**
 * 
 * @author Logan
 * 
 */
public class Doodle {

	private static final String WINDOW_TITLE = "JBox Demo!";
	private static final int[] WINDOW_DIMENSIONS = { 960, 720 };

	public static final float METER_SCALE = 32.0f; // 64 pixels = 1 meter?

	public static SaveTools saveAndLoad;

	private static World world = new World(new Vec2(0.0f, -9.8f));
	// private static Set<Body> bodies = new HashSet<Body>();
	private static GameObjects objects;
	private static StickMan man;
	private static CurvedLine lines;
	private static Boolean moveEraser;
	private static Boolean erase;
	private static int numFootContacts;
	static int jumpWait;

	private static void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		objects.draw();
		lines.draw();
		if (erase)
			lines.drawEraser();
		man.draw();
	}

	private static void logic() {
		world.step(1 / 60f, 8, 3);
		if (jumpWait > 0)
			jumpWait--;

		// Erase Fixtures
		lines.removeFixtures();

		// Keep eraser behind mouse
		if (!erase)
			moveEraser = true;
		if (moveEraser) {
			Vec2 pos = lines.eraser.getPosition();
			Vec2 mouse = new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / METER_SCALE).mul(1 / 3f);
			Vec2 velocity = mouse.sub(pos).mul(60);
			lines.eraser.setLinearVelocity(velocity);
		} else {
			lines.eraser.setLinearVelocity(new Vec2(0, 0));
		}
		moveEraser = !moveEraser;
	}

	private static void input() {
		// Traverse
		if (Keyboard.isKeyDown(Keyboard.KEY_A) && !Keyboard.isKeyDown(Keyboard.KEY_D)) {
			man.move("left", numFootContacts);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D) && !Keyboard.isKeyDown(Keyboard.KEY_A)) {
			man.move("right", numFootContacts);
		} else
			man.move("stop", numFootContacts);

		// Jump
		if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (numFootContacts >= 1 && jumpWait == 0) {
				jumpWait = 2;
				man.move("jump", numFootContacts);
			}
		}

		// Draw lines
		if (Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
			lines.addVertex(new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / METER_SCALE).mul(1 / 3f));
		} else if (Mouse.isButtonDown(1) && !Mouse.isButtonDown(0)) {
			erase = true;
		} else {
			lines.stop();
			erase = false;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			try {
				saveAndLoad.reset(lines, objects, man);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)
				&& (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			try {
				saveAndLoad.save(lines, objects, man);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)
				&& (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			try {
				saveAndLoad.load(lines, objects, man);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
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

	private static void setUpStates() {
		glEnable(GL_TEXTURE_RECTANGLE_ARB);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}

	private static void setUpObjects() {
		saveAndLoad = new SaveTools("res/DoodleSave.xml");
		erase = false;
		moveEraser = true;
		numFootContacts = 0;
		jumpWait = 0;

		man = new StickMan(world, 0.25f, 0.5f);
		objects = new GameObjects(world);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, 0, 0, 1000, 0, "ground", 1.0f);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 320 / METER_SCALE / 2 + 1.5f, 240 / METER_SCALE / 2 + 1,
				0.75f, 0.75f, "box", 1.0f, 6.0f);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 320 / METER_SCALE / 2 - 1.5f, 240 / METER_SCALE / 2 + 1,
				0.25f, 0.25f, "box", 1.0f, 6.0f, 0.3f);
		lines = new CurvedLine(world);

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
			Display.setVSyncEnabled(true);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			cleanUp(true);
		}
	}

	public static class MyContactListener implements ContactListener {

		@Override
		public void beginContact(Contact contact) {
			Object dataA = contact.getFixtureA().getUserData();
			Object dataB = contact.getFixtureB().getUserData();
			if (dataA.equals("eraser") || dataB.equals("eraser")) {
				if (dataA.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());
				if (dataB.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());

			} else if (dataA.equals("foot") || dataB.equals("foot")) {
				numFootContacts++;
			}
		}

		@Override
		public void endContact(Contact contact) {
			Object dataA = contact.getFixtureA().getUserData();
			Object dataB = contact.getFixtureB().getUserData();
			if (dataA.equals("foot") && !dataB.equals("eraser") || dataB.equals("foot") && !dataA.equals("eraser")) {
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
		setUpStates();
		setUpMatrices();
		enterGameLoop();
		cleanUp(false);
	}

}
