package round2;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import round2.GameObjects.ShapeType;

public class MyBody {

	private Body body;
	private ShapeType type;
	private float param1, param2;
	private float density, friction, restitution;
	private String userData;

	public MyBody(World world, ShapeType shapeType, BodyType bodyType, float x, float y, float param1, float param2,
			float angle, String userData, float density, float friction, float restitution) {
		super();
		this.type = shapeType;
		this.param1 = param1;
		this.param2 = param2;
		this.density = density;
		this.friction = friction;
		this.restitution = restitution;
		this.setUserData(userData);

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.angle = angle;
		bodyDef.type = bodyType;
		body = world.createBody(bodyDef);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = restitution;

		switch (type) {
		case BOX:
			PolygonShape boxShape = new PolygonShape();
			boxShape.setAsBox(param1, param2);
			fixtureDef.shape = boxShape;
			break;
		case CIRCLE:
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(param1);
			fixtureDef.shape = circleShape;
			break;
		case LINE:
			EdgeShape edgeShape = new EdgeShape();
			edgeShape.set(new Vec2(x, y), new Vec2(param1, param2));
			fixtureDef.shape = edgeShape;
			break;
		case TRIANGLE:
			break;
		}
		Fixture boxFixture = body.createFixture(fixtureDef);
		boxFixture.setUserData(userData);
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public ShapeType getType() {
		return type;
	}

	public void setType(ShapeType type) {
		this.type = type;
	}

	public float getParam1() {
		return param1;
	}

	public void setParam1(float param1) {
		this.param1 = param1;
	}

	public float getParam2() {
		return param2;
	}

	public void setParam2(float param2) {
		this.param2 = param2;
	}

	public float getDensity() {
		return density;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public float getFriction() {
		return friction;
	}

	public void setFriction(float friction) {
		this.friction = friction;
	}

	public float getRestitution() {
		return restitution;
	}

	public void setRestitution(float restitution) {
		this.restitution = restitution;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
}
