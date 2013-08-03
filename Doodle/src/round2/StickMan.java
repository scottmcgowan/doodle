package round2;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import round2.GameDemo.MoveState;

public class StickMan {

	private static Body body;

	public StickMan(World world) {
		// Body def
		BodyDef manDef = new BodyDef();
		manDef.type = BodyType.DYNAMIC;
		manDef.fixedRotation = true;

		// Shape def
		PolygonShape manShape = new PolygonShape();
		manShape.setAsBox(0.25f, 0.5f);

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
		manShape.setAsBox(0.2f, 0.1f, new Vec2(0f, -0.5f), 0);
		manFixtureDef.isSensor = true;
		Fixture footSensorFixture = body.createFixture(manFixtureDef);
		footSensorFixture.setUserData("foot");
	}

	public void move(MoveState ms) {
		switch (ms) {
		case LEFT:
			body.getLinearVelocity().x = -5.0f;
			break;
		case STOP:
			body.getLinearVelocity().x = 0.0f;
			break;
		case RIGHT:
			body.getLinearVelocity().x = 5.0f;
			break;
		}
	}
	
	public Vec2 getPosition() {
		return body.getPosition();
	}
	
	public float getAngle() {
		return body.getAngle();
	}
	
	public void applyLinearImpulse(Vec2 impulse, Vec2 point) {
		body.applyLinearImpulse(impulse, point);
	}
}
