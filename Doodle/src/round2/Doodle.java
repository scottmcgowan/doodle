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
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jdom2.JDOMException;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import round2.Enemies.EnemyType;
import round2.GameObjects.ShapeType;

import utility.SaveTools;

/**
 * Main class for Doodle game.
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class Doodle {

	private static final String WINDOW_TITLE = "JBox Demo!";
	private static final Vec2 WINDOW_DIMENSIONS = new Vec2(960, 720);
	private static final float SCALE_DIFF = 2f;
	private static final String SAVE_FILE = "res/DoodleSave.xml";
	private static final String RESET_FILE = "res/reset.xml";

	public static float METER_SCALE = 32.0f; // 64 pixels = 1 meter?

	public static Vec2 TRANSLATE = new Vec2(0, 0);
	public static float SCROLL_VALUE = 1.5f;
	public static boolean SCROLL = false;

	private static World world = new World(new Vec2(0.0f, -9.8f));
	private static GameObjects objects;
	private static Enemies enemies;
	private static StickMan man;
	private static CurvedLine lines;
	private static Boolean moveEraser;
	private static Boolean erase;
	public static int numFootContacts;
	static int jumpWait;

	/**
	 * Renders the game objects and characters for each time step.
	 */
	private static void render() {
		glClear(GL_COLOR_BUFFER_BIT);

		glPushMatrix();
		glTranslatef(-TRANSLATE.x, -TRANSLATE.y, 0);

		objects.draw();
		lines.draw();
		if (erase)
			lines.drawEraser();
		enemies.draw(man.getPosition());
		man.draw();

		glPopMatrix();
	}

	/**
	 * Handles the game logic for each time step.
	 */
	private static void logic() {
		world.step(1 / 60f, 8, 3);

		// Decrease the jump timer if necessary.
		if (jumpWait > 0)
			jumpWait--;

		// Erase Fixtures
		lines.removeFixtures();
		enemies.remove();

		// Keep eraser behind mouse
		if (!erase)
			moveEraser = true;

		// Sets the velocity of the eraser so that it reaches the position of
		// the mouse in one time step (there are no position setting functions).
		if (moveEraser) {
			Vec2 pos = lines.eraser.getPosition();
			Vec2 mouse = new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / SCALE_DIFF).add(TRANSLATE)
					.mul(1.0f / METER_SCALE);
			Vec2 velocity = mouse.sub(pos).mul(60);
			lines.eraser.setLinearVelocity(velocity);
		} else {
			lines.eraser.setLinearVelocity(new Vec2(0, 0));
		}
		// Only moves eraser every other time step to prevent wobbling.
		moveEraser = !moveEraser;

		// Allows the screen to scroll when based on character position.
		if (SCROLL) {
			Vec2 manPos = man.getPosition().mul(METER_SCALE).mul(SCALE_DIFF);
			float maxXDiff = manPos.x - WINDOW_DIMENSIONS.x * 5 / 8;
			float minXDiff = manPos.x - WINDOW_DIMENSIONS.x * 1 / 8;
			float maxYDiff = manPos.y - WINDOW_DIMENSIONS.y * 3 / 4;
			float minYDiff = manPos.y - WINDOW_DIMENSIONS.y * 1 / 4;

			if (maxXDiff > TRANSLATE.x * SCALE_DIFF) {
				TRANSLATE.x = maxXDiff / SCALE_DIFF;
			} else if (minXDiff < TRANSLATE.x * SCALE_DIFF) {
				TRANSLATE.x = minXDiff / SCALE_DIFF;
			}
			if (minYDiff < TRANSLATE.y * SCALE_DIFF) {
				TRANSLATE.y = minYDiff / SCALE_DIFF;
			} else if (maxYDiff > TRANSLATE.y * SCALE_DIFF) {
				TRANSLATE.y = maxYDiff / SCALE_DIFF;
			}
		}
	}

	/**
	 * Handles the keyboard and mouse input from each time step;
	 */
	private static void input() {

		// Character control. Typical WASD movement.
		if (Keyboard.isKeyDown(Keyboard.KEY_A) && !Keyboard.isKeyDown(Keyboard.KEY_D)) {
			man.move("left");
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D) && !Keyboard.isKeyDown(Keyboard.KEY_A)) {
			man.move("right");
		} else
			man.move("stop");

		// Jump (space bar can be used to jump as well).
		if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			// Creates a small time gap before allowing the jump method to be
			// called again. This prevents boosted jumping when the character is
			// still near an object but not on it.
			if (jumpWait == 0) {
				jumpWait = 5;
				man.move("jump");
			}
		}

		// Draw and erase lines. Left click to draw, right click to erase.
		if (Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
			Vec2 mousePos = new Vec2(Mouse.getX(), Mouse.getY()).mul(1.0f / SCALE_DIFF).add(TRANSLATE)
					.mul(1.0f / METER_SCALE);
			lines.addVertex(mousePos);
		} else if (Mouse.isButtonDown(1) && !Mouse.isButtonDown(0)) {
			erase = true;
		} else {
			lines.stop();
			erase = false;
		}

		// Load, save, and reset. Ctrl + L to load; Ctrl + S to save; R to reset
		// back to level start.
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				try {
					SaveTools.save(RESET_FILE, lines, objects, man);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					cleanUp(true);
				} catch (IOException e) {
					e.printStackTrace();
					cleanUp(true);
				}
			} else {
				try {
					SaveTools.load(RESET_FILE, lines, objects, man);
				} catch (JDOMException e) {
					e.printStackTrace();
					cleanUp(true);
				} catch (IOException e) {
					e.printStackTrace();
					cleanUp(true);
				}
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)
				&& (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			try {
				SaveTools.save(SAVE_FILE, lines, objects, man);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				cleanUp(true);
			} catch (IOException e) {
				e.printStackTrace();
				cleanUp(true);
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)
				&& (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			try {
				SaveTools.load(SAVE_FILE, lines, objects, man);
			} catch (JDOMException e) {
				e.printStackTrace();
				cleanUp(true);
			} catch (IOException e) {
				e.printStackTrace();
				cleanUp(true);
			}
		}

		// Mouse scrolling when SHIFT is keyed.
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			SCROLL = false;
			float mouseX = Mouse.getX();
			float mouseY = Mouse.getY();
			if (mouseX > 0 && mouseX < WINDOW_DIMENSIONS.x && mouseY > 0 && mouseY < WINDOW_DIMENSIONS.y - 1) {
				if (mouseX > WINDOW_DIMENSIONS.x * 6.5 / 8) {
					TRANSLATE.x += SCROLL_VALUE;
					if (mouseX > WINDOW_DIMENSIONS.x * 15 / 16)
						TRANSLATE.x += SCROLL_VALUE * 2;
				} else if (mouseX < WINDOW_DIMENSIONS.x * 1.5 / 8) {
					TRANSLATE.x -= SCROLL_VALUE;
					if (mouseX < WINDOW_DIMENSIONS.x * 1 / 16)
						TRANSLATE.x -= SCROLL_VALUE * 2;
				}
				if (mouseY > WINDOW_DIMENSIONS.y * 6.5 / 8) {
					TRANSLATE.y += SCROLL_VALUE;
					if (mouseY > WINDOW_DIMENSIONS.y * 15 / 16)
						TRANSLATE.y += SCROLL_VALUE * 2;
				} else if (mouseY < WINDOW_DIMENSIONS.y * 1.5 / 8) {
					TRANSLATE.y -= SCROLL_VALUE;
					if (mouseY < WINDOW_DIMENSIONS.y * 1 / 16)
						TRANSLATE.y -= SCROLL_VALUE * 2;
				}
			}
		} else {
			SCROLL = true;
		}

		// Zoom function
		int mouseWheel = Mouse.getDWheel();
		if (mouseWheel > 0 && METER_SCALE > 6) {
			Vec2 oldPos = man.getPosition();
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				oldPos = new Vec2(Mouse.getX(), Mouse.getY()).mul(1 / SCALE_DIFF);
				oldPos = oldPos.add(TRANSLATE).mul(1 / METER_SCALE);
			}
			zoom(oldPos, METER_SCALE - 1);
		} else if (mouseWheel < 0 && METER_SCALE < 64) {
			Vec2 oldPos = man.getPosition();
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				oldPos = new Vec2(Mouse.getX(), Mouse.getY()).mul(1 / SCALE_DIFF);
				oldPos = oldPos.add(TRANSLATE).mul(1 / METER_SCALE);
			}
			zoom(oldPos, METER_SCALE + 1);
		}

		// Return zoom to original setting
		if (Keyboard.isKeyDown(Keyboard.KEY_0)
				&& (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			zoom(man.getPosition(), 32);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
				enemies.setFire(false);
			else
				enemies.setFire(true);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
			Vec2 mousePos = new Vec2(Mouse.getX(), Mouse.getY()).mul(1/SCALE_DIFF).add(TRANSLATE).mul(1/METER_SCALE);
			enemies.createEnemy(EnemyType.BOMB, mousePos.x, mousePos.y);
		}
	}

	/**
	 * Zooms in or out from the position given. Zoom factor depends on the new
	 * meter scale given.
	 * 
	 * @param oldPos
	 *            - the location where the zooming will be centered
	 * @param newMeterScale
	 *            - the new METER_SCALE number (will change METER_SCALE)
	 */
	private static void zoom(Vec2 oldPos, float newMeterScale) {
		Vec2 oldDim = WINDOW_DIMENSIONS.mul(1 / SCALE_DIFF).mul(1 / METER_SCALE);
		METER_SCALE = newMeterScale;
		Vec2 newDim = WINDOW_DIMENSIONS.mul(1 / SCALE_DIFF).mul(1 / METER_SCALE);
		Vec2 newPos = new Vec2(oldPos.x / oldDim.x * newDim.x, oldPos.y / oldDim.y * newDim.y);
		TRANSLATE = TRANSLATE.add((oldPos.sub(newPos)).mul(METER_SCALE));
	}

	/**
	 * Exits the program cleanly.
	 * 
	 * @param asCrash
	 *            - true if the game exits because of a crash
	 */
	private static void cleanUp(boolean asCrash) {
		Display.destroy();
		System.exit(asCrash ? 1 : 0);
	}

	/**
	 * Sets up openGL matrices for rendering objects. Bottom left: (0, 0) Top
	 * right: (320, 240)
	 */
	private static void setUpMatrices() {
		glMatrixMode(GL_PROJECTION);
		glOrtho(0, WINDOW_DIMENSIONS.x / SCALE_DIFF, 0, WINDOW_DIMENSIONS.y / SCALE_DIFF, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	/**
	 * Sets up the openGL settings
	 */
	private static void setUpStates() {
		// Allows for textures(images) to be drawn on shape surfaces.
		glEnable(GL_TEXTURE_RECTANGLE_ARB);

		// Only shows the image on the front of the surface.
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}

	/**
	 * Initializes variables and game objects.
	 */
	private static void setUpObjects() {
		
		// Variables
		erase = false;
		moveEraser = true;
		numFootContacts = 0;
		jumpWait = 0;

		// Game objects
		man = new StickMan(world, 0.25f, 0.5f);
		man.makeMan(2, 3);
		objects = new GameObjects(world);
		objects.createObject(ShapeType.CIRCLE, BodyType.DYNAMIC, 6.0f, 3.5f, 0.3f, 0, 0, "circle", 0.1f, 0.1f, 0.1f);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, 0, -10, 1000, 10, 0, "ground", 1, 0.01f, 0);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, -30, 20, 2, 40, 0, "wall", 1, 0.01f, 0);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, 0, 30, 100, 2, 0, "wall", 1, 0.01f, 0);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, 70, 20, 2, 40, 0, "wall", 1, 0.01f, 0);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 6.9F, 2.0f, 0.75f, 0.75f, 0, "box", 3, 0.5f, 0);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 4, 3, 0.25f, 0.25f, 0, "box", 1, 2, 0.1f);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 3, 3, 0.15f, 0.25f, 0, "box", 2, 2, 0.1f);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 3, 4, 0.15f, 0.25f, 0, "box", 2, 2, 0.1f);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 3, 5, 0.15f, 0.25f, 0, "box", 2, 2, 0.1f);
		Body bodyA = objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 7, 6, 0.15f, 2.5f, 0, "hangWall", 2, 2, 0.1f);
		Body bodyB = objects.createObject(ShapeType.LINE, BodyType.STATIC, 7, 8.3f, 7, 8.8f, 0, "joint", 2, 2, 0.1f);
		RevoluteJointDef rjd = new RevoluteJointDef();
		rjd.bodyA = bodyA;
		rjd.bodyB = bodyB;
		rjd.localAnchorA.set(0, 2.3f);
		rjd.localAnchorB.set(0, 0);
		world.createJoint(rjd);

		bodyA = objects.createObject(ShapeType.BOX, BodyType.STATIC, 1, 1, 0.05f, 2, 0, "wall", 1, 0.01f, 0, -2);
		bodyB = objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 1, 2, 0.05f, 0.1f, 0, "box", 2, 2, 0.1f, -2);
		rjd.bodyA = bodyA;
		rjd.bodyB = bodyB;
		rjd.localAnchorA.set(0, 1.9f);
		rjd.localAnchorB.set(0, 0.09f);
		world.createJoint(rjd);
		
		for (int i = 0; i < 10; i++) {
			bodyA = objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 1, 2, 0.05f, 0.1f, 0, "box", 2, 2, 0.1f, -2);
			
			rjd.bodyA = bodyA;
			rjd.bodyB = bodyB;
			rjd.localAnchorA.set(0, 0.09f);
			rjd.localAnchorB.set(0, -0.09f);
			world.createJoint(rjd);
			
			bodyB = bodyA;
		}
		bodyA = objects.createObject(ShapeType.BOX, BodyType.STATIC, -1, 1, 0.05f, 2, 0, "wall", 1, 0.01f, 0, -2);
		rjd.bodyA = bodyA;
		rjd.bodyB = bodyB;
		rjd.localAnchorA.set(0, 1.9f);
		rjd.localAnchorB.set(0, -0.09f);
		world.createJoint(rjd);
		
		objects.createObject(ShapeType.CIRCLE, BodyType.DYNAMIC, 1.0f, 3.0f, 0.7f, 0, 0, "circle", 1.0f, 0.1f, 0.1f);
		
		enemies = new Enemies(world);
		enemies.createEnemy(EnemyType.TURRET, 10, 0.25f);
		enemies.createEnemy(EnemyType.TURRET, 10, 7);
		enemies.createEnemy(EnemyType.BOMB, 4, 10);
		lines = new CurvedLine(world);

		// Game world
		world.setContactListener(new MyContactListener());

		// Load starting game settings from xml file
		// try {
		// SaveTools.load(RESET_FILE, lines, objects, man);
		// } catch (JDOMException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * Updates the display at 60 frames per second.
	 */
	private static void update() {
		Display.update();
		Display.sync(60);
	}

	/**
	 * Starts the game loop and continues until the window is closed.
	 */
	private static void enterGameLoop() {
		while (!Display.isCloseRequested()) {
			render();
			logic();
			input();
			update();
		}
	}

	/**
	 * Initialize a new display with size determined by the variable
	 * WINDOW_DIMENSIONS.
	 */
	private static void setUpDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode((int) WINDOW_DIMENSIONS.x, (int) WINDOW_DIMENSIONS.y));
			Display.setTitle(WINDOW_TITLE);
			Display.setVSyncEnabled(true); // Prevents flickering frames.
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			cleanUp(true);
		}
	}

	/**
	 * The methods of this class are called whenever a collision is detected
	 * within the JBox2D world.
	 * 
	 */
	public static class MyContactListener implements ContactListener {

		@Override
		public void beginContact(Contact contact) {
			// Get the userData from each intersecting game object
			String dataA = (String) contact.getFixtureA().getUserData();
			String dataB = (String) contact.getFixtureB().getUserData();
			// System.out.println(dataA + ", " + dataB);

			// If the eraser intersects with a line and 'erase' == true, erase
			// the line.
			if (dataA.equals("eraser") || dataB.equals("eraser")) {
				if (dataA.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());
				if (dataB.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());

				// If the foot sensor on the bottom of the main character is
				// triggered, increase the number of foot contacts to determine
				// whether the player can jump or not.
			} else if (dataA.equals("foot") || dataB.equals("foot")) {
				numFootContacts++;
			} else if (dataA.equals("bullet")) {
				enemies.toRemove(contact.getFixtureA().getBody());
				if (dataB.equals("line")) {
					lines.fixtureToRemove(contact.getFixtureB());
				}
			} else if (dataB.equals("bullet")) {
				enemies.toRemove(contact.getFixtureB().getBody());
				if (dataA.equals("line")) {
					lines.fixtureToRemove(contact.getFixtureA());
				}
				// } else if (dataA.equals("projectile") &&
				// dataB.equals("line")) {
				// lines.fixtureToRemove(contact.getFixtureB());
				// } else if (dataB.equals("projectile") &&
				// dataA.equals("line")) {
				// lines.fixtureToRemove(contact.getFixtureA());
			}
		}

		@Override
		public void endContact(Contact contact) {
			// Get the userData from each intersecting game object
			String dataA = (String) contact.getFixtureA().getUserData();
			String dataB = (String) contact.getFixtureB().getUserData();

			// If the eraser intersects with a line and 'erase' == true, erase
			// the line.
			if (dataA.equals("eraser") || dataB.equals("eraser")) {
				if (dataA.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());
				if (dataB.equals("line") && erase)
					lines.fixtureToRemove(contact.getFixtureA());

				// If the character's 'feet' leave an object, decrease the
				// number of foot contacts to determine whether the player can
				// jump or not.
			} else if (dataA.equals("foot") || dataB.equals("foot")) {
				numFootContacts--;
			}
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}

	}

	/**
	 * Sets up the openGL display and settings, initializes the game objects,
	 * and begins the game loop (in that order).
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		setUpDisplay();
		setUpStates();
		setUpMatrices();
		setUpObjects();
		enterGameLoop();
		cleanUp(false);
	}

}
