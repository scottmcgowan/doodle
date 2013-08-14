package round2;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

public class Enemies {

	private World world;
	private Set<Enemy> enemies;
	private Set<Body> bullets;
	private List<Body> projectiles;
	private List<Body> toRemove;
	private List<Enemy> enemyToRemove;
	private int timer;
	private boolean fire;

	public enum EnemyType {
		TURRET, MOVING_TURRET, LINE_EATER, BOMB;
	}

	public Enemies(World world) {
		this.world = world;
		enemies = new HashSet<Enemy>();
		bullets = new HashSet<Body>();
		projectiles = new ArrayList<Body>();
		toRemove = new ArrayList<Body>();
		enemyToRemove = new ArrayList<Enemy>();
		timer = 0;
		fire = true;
	}

	public void createEnemy(EnemyType type, float x, float y) {
		Enemy enemy = new Enemy(world, this, type, x, y);
		enemies.add(enemy);
	}

	public void draw(Vec2 manPos) {
		for (Body bullet : bullets) {
			Vec2 bulletPos = bullet.getPosition().mul(Doodle.METER_SCALE);

			glPushMatrix();
			glTranslatef(bulletPos.x, bulletPos.y, 0);

			glColor3f(1.0f, 1.0f, 1.0f);
			glEnable(GL_POINT_SMOOTH);
			glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
			glPointSize(0.1f * Doodle.METER_SCALE);
			glBegin(GL_POINTS);
			glVertex2f(0, 0);
			glEnd();

			glPopMatrix();
		}

		for (Body pBody : projectiles) {
			Vec2 pPos = pBody.getPosition().mul(Doodle.METER_SCALE);

			glPushMatrix();
			glTranslatef(pPos.x, pPos.y, 0);

//			glColor3f(0.15f, 0.15f, 0.15f); // subtle
			glColor3f(1, 1, 0); // not subtle
			glEnable(GL_POINT_SMOOTH);
			glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
			glPointSize(0.1f * Doodle.METER_SCALE);
			glBegin(GL_POINTS);
			glVertex2f(0, 0);
			glEnd();

			glPopMatrix();
		}

		for (Enemy enemy : enemies) {
			enemy.draw();
			if (enemy.getEnemyType() == EnemyType.TURRET) {
				if (fire)
					enemy.fire(manPos);
			} else if (enemy.getEnemyType() == EnemyType.BOMB) {
				if (enemy.checkTimer() == 200) {
					projectiles.addAll(enemy.blowUp());
					enemyToRemove.add(enemy);
					timer = 50;
				}
			}
		}
		if (--timer == 0) {
			for (Body pBody : projectiles) {
				world.destroyBody(pBody);
			}
			projectiles.clear();
		}
	}

	public void setFire(boolean fire) {
		this.fire = fire;
	}

	public boolean getFire() {
		return fire;
	}

	public void addBullet(Body bullet) {
		bullets.add(bullet);
	}

	public void toRemove(Body bullet) {
		toRemove.add(bullet);
	}

	public void remove() {
		for (Body bullet : toRemove) {
			bullets.remove(bullet);
			world.destroyBody(bullet);
		}
		for (Enemy enemy : enemyToRemove) {
			enemies.remove(enemy);
		}
		toRemove.clear();
		enemyToRemove.clear();
	}
}
