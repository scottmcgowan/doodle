package model;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

@SuppressWarnings("serial")
public class StickMan extends Rectangle2D.Double {

	private Point location;
	
	public StickMan(int x, int y) {
		super(x, y, 30, 70);
		location = new Point(x, y);
	}
	
	public StickMan(Point p) {
		super(p.x, p.y, 30, 70);
		location = p;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setLocation(int x, int y) {
		location.x = x;
		location.y = y;
	}
}
