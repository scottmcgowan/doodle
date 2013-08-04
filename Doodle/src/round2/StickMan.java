package round2;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class StickMan {

	private static final String SPRITESHEET_IMAGE_LOCATION = "res/manSprite.png";
	private static final String SPRITESHEET_XML_LOCATION = "res/manSprite.xml";
	private static final Map<String, Sprite> spriteMap = new HashMap<String, Sprite>();
	private static int spritesheet;
	private Sprite currentSprite;
	private int spriteCounter;
	private Body body;
	private float hx, hy;

	public StickMan(World world, float hx, float hy) {
		this.hx = hx;
		this.hy = hy;

		setUpSpriteSheets();

		currentSprite = spriteMap.get("stand");
		spriteCounter = 0;

		// Body def
		BodyDef manDef = new BodyDef();
		manDef.type = BodyType.DYNAMIC;
		manDef.fixedRotation = true;

		// Shape def
		PolygonShape manShape = new PolygonShape();
		manShape.setAsBox(hx, hy);

		// Fixture def
		FixtureDef manFixtureDef = new FixtureDef();
		manFixtureDef.density = 0.1f;
		manFixtureDef.shape = manShape;
		manFixtureDef.friction = 0;

		// create body
		manDef.position.set(320 / GameDemo.METER_SCALE / 2, 240 / GameDemo.METER_SCALE / 2);
		body = world.createBody(manDef);

		// add main fixture
		Fixture manFixture = body.createFixture(manFixtureDef);
		manFixture.setUserData("man");

		// add foot sensor fixture
		manShape.setAsBox(0.22f, 0.02f, new Vec2(0f, -0.5f), 0);
		manFixtureDef.isSensor = true;
		Fixture footSensorFixture = body.createFixture(manFixtureDef);
		footSensorFixture.setUserData("foot");
	}

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

	public void draw() {
		// System.out.println("Man: " + body.getPosition());
		// StickMan
		glPushMatrix();
		Vec2 bodyPosition = body.getPosition().mul(GameDemo.METER_SCALE);
		glTranslatef(bodyPosition.x, bodyPosition.y, 0);
		glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);

		float x = -hx * GameDemo.METER_SCALE;
		float y = -hy * GameDemo.METER_SCALE;
		float x2 = hx * GameDemo.METER_SCALE;
		float y2 = hy * GameDemo.METER_SCALE;

		glColor3f(1, 1, 1);
		glBindTexture(GL_TEXTURE_RECTANGLE_ARB, spritesheet);

		int imgX = currentSprite.getX();
		int imgY = currentSprite.getY();
		int imgX2 = imgX + currentSprite.getWidth();
		int imgY2 = imgY + currentSprite.getHeight();

		glBegin(GL_QUADS);
		glTexCoord2f(imgX, imgY2);
		glVertex2f(x, y);
		glTexCoord2f(imgX2, imgY2);
		glVertex2f(x2, y);
		glTexCoord2f(imgX2, imgY);
		glVertex2f(x2, y2);
		glTexCoord2f(imgX, imgY);
		glVertex2f(x, y2);
		glEnd();

		glBindTexture(GL_TEXTURE_RECTANGLE_ARB, 0);
		glPopMatrix();

	}

	public void move(String direction, int footContacts) {
		switch (direction) {
		case "left":
			if (body.getLinearVelocity().x > -1.5)
				body.applyLinearImpulse(new Vec2(-0.1f, 0), body.getPosition());
			if (footContacts <= 0) {
				currentSprite = spriteMap.get("jumpL");
			} else {
				currentSprite = spriteMap.get("left" + (spriteCounter / 10));
			}
			break;
		case "stop":
			body.setLinearVelocity(new Vec2(0, body.getLinearVelocity().y));
			if (footContacts <= 0) {
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
			if (body.getLinearVelocity().x < 1.5)
				body.applyLinearImpulse(new Vec2(0.1f, 0), body.getPosition());
			if (footContacts <= 0) {
				currentSprite = spriteMap.get("jumpR");
			} else {
				currentSprite = spriteMap.get("right" + (spriteCounter / 10));
			}
			break;
		case "jump":
			body.applyLinearImpulse(new Vec2(0, 0.27f), body.getPosition());
			if (footContacts <= 0) {
				if (body.getLinearVelocity().x >= 0) {
					currentSprite = spriteMap.get("jumpR");
				} else {
					currentSprite = spriteMap.get("jumpL");
				}
			} else {
				currentSprite = spriteMap.get("stand");
			}
			break;
		}
		spriteCounter = (spriteCounter + 1) % 30;
	}

	public Vec2 getPosition() {
		return body.getPosition();
	}
}
