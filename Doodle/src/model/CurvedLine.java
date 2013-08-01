package model;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class CurvedLine {

	List<Line2D.Double> lines;
	
	public CurvedLine() {
		lines = new ArrayList<Line2D.Double>();
	}
	
	public void addLine(Point start, Point end) {
		Line2D.Double line = new Line2D.Double(start, end);
		lines.add(line);
	}
	
	public List<Line2D.Double> getLine() {
		return lines;
	}
}
