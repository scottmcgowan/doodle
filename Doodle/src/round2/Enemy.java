package round2;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import round2.Enemies.EnemyType;

public class Enemy {

	private World world;
	private Enemies enemies;
	private EnemyType type;
	private Body body;
	private float x, y;
	private int timer;

	public Enemy(World world, Enemies enemies, EnemyType type, float x, float y) {
		this.world = world;
		this.enemies = enemies;
		this.type = type;
		this.x = x;
		this.y = y;
		timer = 0;

		BodyDef enemyDef = new BodyDef();
		enemyDef.position.set(x, y);
		if (type == EnemyType.TURRET) {
			enemyDef.type = BodyType.KINEMATIC;
			body = world.createBody(enemyDef);

			PolygonShape bodyShape = new PolygonShape();
			bodyShape.setAsBox(0.35f, 0.25f, new Vec2(0, 0), 0);

			CircleShape turretShape = new CircleShape();
			turretShape.setRadius(0.25f);

			// PolygonShape barrelShape = new PolygonShape();
			// barrelShape.setAsBox(0.25f, 0.05f, new Vec2(0.25f, 0), 0);

			// FixtureDef bodyFixtureDef = new FixtureDef();
			FixtureDef turretFixtureDef = new FixtureDef();
			turretFixtureDef.shape = turretShape;
			turretFixtureDef.filter.groupIndex = -1;
			// FixtureDef barrelFixtureDef = new FixtureDef();

			// Fixture bodyFixture = body.createFixture(bodyFixtureDef);
			Fixture turretFixture = body.createFixture(turretFixtureDef);
			// Fixture barrelFixture = body.createFixture(barrelFixtureDef);
			turretFixture.setUserData("turret");
			// barrelFixture.setUserData("turret");
			timer = 500;
		}

		if (type == EnemyType.BOMB) {
			enemyDef.type = BodyType.DYNAMIC;
			body = world.createBody(enemyDef);

			CircleShape bombShape = new CircleShape();
			bombShape.setRadius(0.15f);

			FixtureDef bombFixtureDef = new FixtureDef();
			bombFixtureDef.shape = bombShape;
			bombFixtureDef.restitution = 0.3f;
			bombFixtureDef.filter.groupIndex = -1;

			Fixture bombFixture = body.createFixture(bombFixtureDef);
			bombFixture.setUserData("bomb");

			timer = 500;
		}
	}

	public Body getBody() {
		return body;
	}

	public EnemyType getEnemyType() {
		return type;
	}

	public void draw() {
		Vec2 bodyPos = body.getPosition().mul(Doodle.METER_SCALE);

		glPushMatrix();
		glTranslatef(bodyPos.x, bodyPos.y, 0);
		glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);
		float length = 0.45f * Doodle.METER_SCALE;
		float hy = 0.05f * Doodle.METER_SCALE;

		if (type == EnemyType.TURRET) {
			glColor3f(0.75f, 0.75f, 0.75f);
			glBegin(GL_QUADS);
			glVertex2f(-length, hy);
			glVertex2f(-length, -hy);
			glVertex2f(0, -hy);
			glVertex2f(0, hy);
			glEnd();

			glColor3f(0.5f, 0.5f, 0.5f);
		} else if (type == EnemyType.BOMB) {
			glColor3f(1, 0, 1);
		}
		glEnable(GL_POINT_SMOOTH);
		glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
		glPointSize(4 * body.getFixtureList().getShape().getRadius() * Doodle.METER_SCALE);
		glBegin(GL_POINTS);
		glVertex2f(0, 0);
		glEnd();

		glPopMatrix();
	}

	public void fire(Vec2 manPos) {
		Vec2 direction = manPos.sub(body.getPosition());
		// float angle = (float) Math.atan(direction.y / direction.x);
		float angle = (float) Math.atan2(-direction.x, direction.y);
		body.setTransform(body.getPosition(), angle - (float) Math.PI / 2);

		if (timer == 0) {
			BodyDef bulletDef = new BodyDef();;
			bulletDef.type = BodyType.DYNAMIC;
			bulletDef.fixedRotation = true; // rotation not necessary
			bulletDef.bullet = true; // prevent tunneling at high speed
			bulletDef.linearDamping = 0; // drag due to moving through air
			bulletDef.gravityScale = 0; // ignore gravity
			bulletDef.position = body.getPosition(); // start at blast center
			float hypotenuse = (float) Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.y, 2));
			bulletDef.linearVelocity = new Vec2(direction.x, direction.y).mul(5 / hypotenuse);
			Body bullet = world.createBody(bulletDef);

			CircleShape bulletShape = new CircleShape();
			bulletShape.setRadius(0.05f);

			FixtureDef bulletFixtureDef = new FixtureDef();
			bulletFixtureDef.shape = bulletShape;
			bulletFixtureDef.density = 10.0f; // very high - shared across all
												// particles
			bulletFixtureDef.friction = 0; // friction not necessary
			bulletFixtureDef.restitution = 0.99f; // high restitution to reflect
													// off obstacles
			bulletFixtureDef.filter.groupIndex = -1; // particles should not
														// collide with each
			// other
			Fixture bulletFixture = bullet.createFixture(bulletFixtureDef);
			bulletFixture.setUserData("bullet");

			enemies.addBullet(bullet);

			timer = (timer + 1) % 50;
		} else
			timer = (timer + 1) % 50;
	}

	public int checkTimer() {
		return timer--;
	}

	public List<Body> blowUp() {
		int numRays = 60;
		float blastPower = 50;
		List<Body> p = new ArrayList<Body>();

		for (int i = 0; i < numRays; i++) {
			float angle = (float) Math.toRadians((i / (float)numRays) * 360);
			Vec2 rayDir = new Vec2((float) Math.sin(angle), (float) Math.cos(angle));

			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.fixedRotation = true; // rotation not necessary
			bd.bullet = true; // prevent tunneling at high speed
			bd.linearDamping = 5; // drag due to moving through air
			bd.gravityScale = 0; // ignore gravity
			bd.position = body.getPosition(); // start at blast center
			bd.linearVelocity = rayDir.mul(blastPower);
			Body pBody = world.createBody(bd);

			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(0.05f); // very small

			FixtureDef fd = new FixtureDef();
			fd.shape = circleShape;
			fd.density = 1.0f; // very high - shared across all
												// particles
			fd.friction = 0; // friction not necessary
			fd.restitution = 0.99f; // high restitution to reflect off obstacles
			fd.filter.groupIndex = -1; // particles should not collide with each
										// other
			Fixture f = pBody.createFixture(fd);
			f.setUserData("projectile");

			p.add(pBody);
		}
		world.destroyBody(body);
		return p;
	}

}
