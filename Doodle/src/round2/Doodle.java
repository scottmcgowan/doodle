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
 * Main class for Doodle game.
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class Doodle {

	private static final String WINDOW_TITLE = "JBox Demo!";
	private static final int[] WINDOW_DIMENSIONS = { 960, 720 };
	private static final int SCALE_DIFF = 2;
	private static final String SAVE_FILE = "res/DoodleSave.xml";
	private static final String RESET_FILE = "res/reset.xml";

	public static final float METER_SCALE = 32.0f; // 64 pixels = 1 meter?

	public static Vec2 TRANSLATE = new Vec2(0, 0);
	public static float SCROLL_VALUE = 1.5f;
	public static boolean SCROLL = false;

	private static World world = new World(new Vec2(0.0f, -9.8f));
	private static GameObjects objects;
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
		objects.draw();
		lines.draw();
		if (erase)
			lines.drawEraser();
		man.draw();
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
			float maxXDiff = manPos.x - WINDOW_DIMENSIONS[0] * 5 / 8;
			float minXDiff = manPos.x - WINDOW_DIMENSIONS[0] * 1 / 8;
			float maxYDiff = manPos.y - WINDOW_DIMENSIONS[1] * 3 / 4;
			float minYDiff = manPos.y - WINDOW_DIMENSIONS[1] * 1 / 4;
			
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
		// System.out.println(Doodle.numFootContacts);

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
				jumpWait = 2;
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
			System.out.println(mouseY);
			if (mouseX > 0 && mouseX < WINDOW_DIMENSIONS[0] && mouseY > 0 && mouseY < WINDOW_DIMENSIONS[1] - 1) {
				if (mouseX > WINDOW_DIMENSIONS[0] * 7 / 8) {
					TRANSLATE.x += SCROLL_VALUE;
					if (mouseX > WINDOW_DIMENSIONS[0] * 15 / 16)
						TRANSLATE.x += SCROLL_VALUE;
				} else if (mouseX < WINDOW_DIMENSIONS[0] * 1 / 8) {
					TRANSLATE.x -= SCROLL_VALUE;
					if (mouseX < WINDOW_DIMENSIONS[0] * 1 / 16)
						TRANSLATE.x -= SCROLL_VALUE;
				}
				if (mouseY > WINDOW_DIMENSIONS[1] * 7 / 8) {
					TRANSLATE.y += SCROLL_VALUE;
					if (mouseY > WINDOW_DIMENSIONS[1] * 15 / 16)
						TRANSLATE.y += SCROLL_VALUE;
				} else if (mouseY < WINDOW_DIMENSIONS[1] * 1 / 8) {
					TRANSLATE.y -= SCROLL_VALUE;
					if (mouseY < WINDOW_DIMENSIONS[1] * 1 / 16)
						TRANSLATE.y -= SCROLL_VALUE;
				}
			}
		} else {
			SCROLL = true;
		}
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
		glOrtho(0, WINDOW_DIMENSIONS[0] / SCALE_DIFF, 0, WINDOW_DIMENSIONS[1] / SCALE_DIFF, 1, -1);
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
		objects.createObject(ShapeType.CIRCLE, BodyType.DYNAMIC, 5, 3, 0.3f, 0, 0, "circle", 0.01f, 0.1f, 0.1f);
		objects.createObject(ShapeType.BOX, BodyType.STATIC, 0, -5, 1000, 5, 0, "ground", 1, 0.01f, 0);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 6.5F, 3, 0.75f, 0.75f, 0, "box", 3, 0.5f, 0);
		objects.createObject(ShapeType.BOX, BodyType.DYNAMIC, 4, 3, 0.25f, 0.25f, 0, "ground", 1, 2, 0.1f);
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
			Display.setDisplayMode(new DisplayMode(WINDOW_DIMENSIONS[0], WINDOW_DIMENSIONS[1]));
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
			}
		}

		@Override
		public void endContact(Contact contact) {
			// Get the userData from each intersecting game object
			String dataA = (String) contact.getFixtureA().getUserData();
			String dataB = (String) contact.getFixtureB().getUserData();

			// If the character's 'feet' leave an object, decrease the number of
			// foot contacts to determine whether the player can jump or not.
			if (dataA.equals("foot") && !dataB.equals("eraser") || dataB.equals("foot") && !dataA.equals("eraser")) {
				if (numFootContacts > 0)
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
