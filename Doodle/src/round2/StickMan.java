package round2;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import utility.ImagingTools;

/**
 * A class for the player object
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class StickMan {

	private static final String SPRITESHEET_IMAGE_LOCATION = "res/manSprite.png";
	private static final String SPRITESHEET_XML_LOCATION = "res/manSprite.xml";
	private static final Map<String, Sprite> spriteMap = new HashMap<String, Sprite>();
	private static int spritesheet;
	private Sprite currentSprite;
	private int spriteCounter;
	private World world;
	private Body body;
	private float hx, hy;

	/**
	 * Sets up and stores the basic height and image info for the stick man.
	 * Does not create the appropriate JBox2D objects.
	 * 
	 * @param world
	 *            - the JBox2D world in which the character will exist
	 * @param hx
	 *            - the width of the stick man from the center to an edge
	 * @param hy
	 *            - the height of the stick man from the center to an edge
	 */
	public StickMan(World world, float hx, float hy) {
		this.world = world;
		this.hx = hx;
		this.hy = hy;

		setUpSpriteSheets();

		currentSprite = spriteMap.get("stand");
		spriteCounter = 0;

		body = null;
	}

	/**
	 * Creates the JBox2D body, shape, and fixtures.
	 * 
	 * @param x
	 *            - the x coordinate of the stick man
	 * @param y
	 *            - the y coordinate of the stick man
	 */
	public void makeMan(float x, float y) {
		// Body def
		BodyDef manDef = new BodyDef();
		manDef.type = BodyType.DYNAMIC;
		manDef.fixedRotation = true;
		manDef.linearDamping = 1.0f;

		// Shape def
		PolygonShape manShape = new PolygonShape();
		manShape.setAsBox(hx/4, hy/2);
		
		float radius = hx * 3/4;
		CircleShape headShape= new CircleShape();
		headShape.setRadius(radius);
		headShape.m_p.set(new Vec2(0, hy - radius));
		CircleShape footShape= new CircleShape();
		footShape.setRadius(radius);
		footShape.m_p.set(new Vec2(0, radius - hy));

		// Fixture def
		FixtureDef manFixtureDef = new FixtureDef();
		manFixtureDef.density = 0.3f;
		manFixtureDef.shape = manShape;
		manFixtureDef.friction = 0.001f;
		FixtureDef headFixtureDef = new FixtureDef();
		headFixtureDef.density = 0.1f;
		headFixtureDef.shape = headShape;
		headFixtureDef.friction = 0.001f;
		FixtureDef footFixtureDef = new FixtureDef();
		footFixtureDef.density = 0.1f;
		footFixtureDef.shape = footShape;
		footFixtureDef.friction = 0.001f;

		// Create body
		manDef.position.set(x, y);
		body = world.createBody(manDef);

		// Add main fixtures
		Fixture manFixture = body.createFixture(manFixtureDef);
		Fixture headFixture = body.createFixture(headFixtureDef);
		Fixture footFixture = body.createFixture(footFixtureDef);
		manFixture.setUserData("man");
		headFixture.setUserData("man");
		footFixture.setUserData("man");

		// Add foot sensor fixture to determine if the player is on something.
		footShape.setRadius(radius - 0.01f);
		footShape.m_p.set(new Vec2(0f, radius - hy - 0.02f));
		footFixtureDef.isSensor = true;
		Fixture footSensorFixture = body.createFixture(footFixtureDef);
		footSensorFixture.setUserData("foot");
	}

	/*
	 * Deletes the current body in preparation to load a new one.
	 */
	public void destroyBody() {
		if (body != null)
			world.destroyBody(body);
	}

	/**
	 * Loads a sprite sheet and initializes a list of Sprite objects. Each
	 * Sprite object contains the location and size of an individual image.
	 */
	private static void setUpSpriteSheets() {
		spritesheet = ImagingTools.glLoadTextureLinear(SPRITESHEET_IMAGE_LOCATION);
		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = builder.build(new File(SPRITESHEET_XML_LOCATION));
			Element root = document.getRootElement();
			for (Element spriteElement : root.getChildren()) {
				String name = spriteElement.getAttributeValue("n");
				int x = spriteElement.getAttribute("x").getIntValue();
				int y = spriteElement.getAttribute("y").getIntValue();
				int w = spriteElement.getAttribute("w").getIntValue();
				int h = spriteElement.getAttribute("h").getIntValue();
				Sprite sprite = new Sprite(name, x, y, w, h);
				spriteMap.put(sprite.getName(), sprite);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Renders the stick man in the game window
	 */
	public void draw() {

		// Save the current matrix data before translating and/or rotating it.
		glPushMatrix();

		// Get the stick man's center position and move the rendering matrix.
		// Note: rotation not needed since stick man has a fixed rotation.
		Vec2 bodyPosition = body.getPosition().mul(Doodle.METER_SCALE);
		glTranslatef(bodyPosition.x, bodyPosition.y, 0);

		// Convert the Box2D center location to openGL edge coordinates.
		float x = -hx * Doodle.METER_SCALE;
		float y = -hy * Doodle.METER_SCALE;
		float x2 = hx * Doodle.METER_SCALE;
		float y2 = hy * Doodle.METER_SCALE;

		// Set a white background and select the texture image.
		glColor3f(1, 1, 1);
		glBindTexture(GL_TEXTURE_RECTANGLE_ARB, spritesheet);

		// Get the appropriate coordinates for the selected sprite image.
		int imgX = currentSprite.getX();
		int imgY = currentSprite.getY();
		int imgX2 = imgX + currentSprite.getWidth();
		int imgY2 = imgY + currentSprite.getHeight();

		// Draws the stick man rectangle from the given vertices
		// (counter-clockwise) and maps the image coordinates to each
		// corresponding vertex.
		glBegin(GL_QUADS);
		glTexCoord2f(imgX, imgY2); // bottom left
		glVertex2f(x, y);
		glTexCoord2f(imgX2, imgY2); // bottom right
		glVertex2f(x2, y);
		glTexCoord2f(imgX2, imgY); // top right
		glVertex2f(x2, y2);
		glTexCoord2f(imgX, imgY); // top left
		glVertex2f(x, y2);
		glEnd();

		// Restore the texture and matrix data.
		glBindTexture(GL_TEXTURE_RECTANGLE_ARB, 0);
		glPopMatrix();

	}

	/**
	 * Moves the player in the desired direction and allows the character to
	 * jump if is standing on at least one object.
	 * 
	 * @param direction
	 *            - string representing the direction of movement
	 */
	public void move(String direction) {
		switch (direction) {
		case "left":
			if (body.getLinearVelocity().x > -1.5f)
				body.applyLinearImpulse(new Vec2(-0.1f, 0), body.getPosition());
			else
				body.setLinearVelocity(new Vec2(-1.4f, body.getLinearVelocity().y));

			// Check if man is airborne then set appropriate sprite image.
			if (Doodle.numFootContacts <= 0) {
				currentSprite = spriteMap.get("jumpL");
			} else {
				currentSprite = spriteMap.get("left" + (spriteCounter / 10));
			}
			break;
		case "stop":
			body.setLinearVelocity(new Vec2(0, body.getLinearVelocity().y));

			// Check if man is airborne then set appropriate sprite image.
			if (Doodle.numFootContacts <= 0) {
				if (body.getLinearVelocity().x >= 0) {
					currentSprite = spriteMap.get("jumpR");
				} else {
					currentSprite = spriteMap.get("jumpL");
				}
			} else {
				currentSprite = spriteMap.get("stand");
			}
			break;
		case "right":
			if (body.getLinearVelocity().x < 1.5f)
				body.applyLinearImpulse(new Vec2(0.1f, 0), body.getPosition());
			else
				body.setLinearVelocity(new Vec2(1.4f, body.getLinearVelocity().y));

			// Check if man is airborne then set appropriate sprite image.
			if (Doodle.numFootContacts <= 0) {
				currentSprite = spriteMap.get("jumpR");
			} else {
				currentSprite = spriteMap.get("right" + (spriteCounter / 10));
			}
			break;
		case "jump":

			// Only jump if stick man's "feet" are on an object. Otherwise
			// select the appropriate sprite image.
			if (Doodle.numFootContacts > 0)
				body.applyLinearImpulse(new Vec2(0, 0.27f), body.getPosition());
			else {
				if (body.getLinearVelocity().x >= 0) {
					currentSprite = spriteMap.get("jumpR");
				} else {
					currentSprite = spriteMap.get("jumpL");
				}
			}
			break;
		}
		spriteCounter = (spriteCounter + 1) % 30;
	}

	/**
	 * Returns the position of the stick man.
	 * 
	 * @return a Vec2 of the stick man's center position
	 */
	public Vec2 getPosition() {
		return body.getPosition();
	}
}
