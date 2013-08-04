package round2;

import static org.lwjgl.opengl.GL11.*;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class StickMan {

	private static Body body;
	private float hx, hy;

	public StickMan(World world, float hx, float hy) {
		this.hx = hx;
		this.hy = hy;

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
		manShape.setAsBox(0.2f, 0.03f, new Vec2(0f, -0.5f), 0);
		manFixtureDef.isSensor = true;
		Fixture footSensorFixture = body.createFixture(manFixtureDef);
		footSensorFixture.setUserData("foot");
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
		glBegin(GL_QUADS);
		// glTexCoord2f(x, y);
		glVertex2f(x, y);
		// glTexCoord2f(x, y2);
		glVertex2f(x2, y);
		// glTexCoord2f(x2, y2);
		glVertex2f(x2, y2);
		// glTexCoord2f(x2, y);
		glVertex2f(x, y2);
		glEnd();
		glPopMatrix();
	}

	public void move(String direction) {
		switch (direction) {
		case "left":
			if (body.getLinearVelocity().x > -1.5)
				body.applyLinearImpulse(new Vec2(-0.1f, 0), body.getPosition());
			break;
		case "stop":
			body.setLinearVelocity(new Vec2(0, body.getLinearVelocity().y));
			break;
		case "right":
			if (body.getLinearVelocity().x < 1.5)
				body.applyLinearImpulse(new Vec2(0.1f, 0), body.getPosition());
			break;
		case "jump":
			body.applyLinearImpulse(new Vec2(0, 0.27f), body.getPosition());
			break;
		}
	}

	public Vec2 getPosition() {
		return body.getPosition();
	}
}
