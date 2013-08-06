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

/**
 * A class for game objects.
 * 
 * @author Scott McGowan and Logan Barnes
 * 
 */
public class MyBody {

	private Body body;
	private ShapeType type;
	private float param1, param2;
	private float density, friction, restitution;
	private String userData;

	/**
	 * Initializes a game object to interact with the JBox2d world
	 * 
	 * @param world
	 *            - the JBox2D world
	 * @param shapeType
	 *            - the shape of the object
	 * @param bodyType
	 *            - the body type: static, kinematic, dynamic
	 * @param x
	 *            - the x coordinate position
	 * @param y
	 *            - the y coordinate position
	 * @param param1
	 *            - the width or radius of the body
	 * @param param2
	 *            - the height of the body (if necessary)
	 * @param angle
	 *            - the angle in radians of the body
	 * @param userData
	 *            - a string to identify the object
	 * @param density
	 *            - the density of the object, usually in kg/m^2
	 * @param friction
	 *            - the friction coefficient, usually in the range [0, 1]
	 * @param restitution
	 *            - the restitution (elasticity), usually in the range [0, 1]
	 */
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

	/**
	 * @return the Body of this game object
	 */
	public Body getBody() {
		return body;
	}

	/**
	 * @param body
	 *            - the Body to be used for this game object
	 */
	public void setBody(Body body) {
		this.body = body;
	}

	/**
	 * @return the ShapeType of this game object
	 */
	public ShapeType getType() {
		return type;
	}

	/**
	 * @param type
	 *            - the ShapeType to be used for this game object
	 */
	public void setType(ShapeType type) {
		this.type = type;
	}

	/**
	 * @return the first parameter used with this game object
	 */
	public float getParam1() {
		return param1;
	}

	/**
	 * @param param1
	 *            - the first parameter to be used with this game object
	 */
	public void setParam1(float param1) {
		this.param1 = param1;
	}

	/**
	 * @return the second parameter used with this game object
	 */
	public float getParam2() {
		return param2;
	}

	/**
	 * @param param2
	 *            - the second parameter to be used with this game object
	 */
	public void setParam2(float param2) {
		this.param2 = param2;
	}

	/**
	 * @return the density of this game object
	 */
	public float getDensity() {
		return density;
	}

	/**
	 * @param density
	 *            - the density to be used for this game object
	 */
	public void setDensity(float density) {
		this.density = density;
	}

	/**
	 * @return the friction of this game object
	 */
	public float getFriction() {
		return friction;
	}

	/**
	 * @param friction
	 *            - the friction to be used for this game object
	 */
	public void setFriction(float friction) {
		this.friction = friction;
	}

	/**
	 * @return the restitution of this game object
	 */
	public float getRestitution() {
		return restitution;
	}

	/**
	 * @param restitution
	 *            - the restitution to be used for this game object
	 */
	public void setRestitution(float restitution) {
		this.restitution = restitution;
	}

	/**
	 * @return the user data of this game object
	 */
	public String getUserData() {
		return userData;
	}

	/**
	 * 
	 * @param userData
	 *            - the user data to be used for this game object
	 */
	public void setUserData(String userData) {
		this.userData = userData;
	}
}
