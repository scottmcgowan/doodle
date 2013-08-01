package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JPanel;

import model.CurvedLine;


@SuppressWarnings("serial")
public class MainPanel extends JPanel {
	
	CurvedLine curve;
	Point old;

	public MainPanel() {
		super();
		this.setPreferredSize(new Dimension(500, 500));
		this.setBackground(Color.WHITE);
		this.addMouseListener(new MListener());
		this.addMouseMotionListener(new MListener());
		curve = new CurvedLine();
		old = null;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		List<Line2D.Double> lines = curve.getLine();
		for (Line2D.Double l: lines) {
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2d.draw(l);
		}
	}
	
	public class MListener extends MouseAdapter {
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (old == null) {
				old = e.getPoint();
			}
			curve.addLine(old, e.getPoint());
			old = e.getPoint();
			repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			old = null;
		}
	}
}
