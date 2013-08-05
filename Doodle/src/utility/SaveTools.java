package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import round2.CurvedLine;
import round2.GameObjects;
import round2.StickMan;

/**
 * Saves/loads the doodle sketch to and from an xml file.
 * 
 */
public class SaveTools {

	private String fileName;

	/**
	 * 
	 * @param filename
	 */
	public SaveTools(String fileName) {
		this.fileName = fileName;
	}

	public void load(CurvedLine lines, GameObjects objects, StickMan man) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(fileName);
		Element root = document.getRootElement();
		List<Element> children = root.getChildren();
		List<Body> temp = new ArrayList<Body>();
		lines.load();
		man.load();

		for (Element b : children) {
			switch (b.getName()) {
			
			case "body":
				float x = Float.parseFloat(b.getAttributeValue("x"));
				float y = Float.parseFloat(b.getAttributeValue("y"));
				Body body = lines.createBody(x, y);
				List<Element> bChildren = b.getChildren();

				for (Element e : bChildren) {
					float x1 = Float.parseFloat(e.getAttributeValue("x1"));
					float y1 = Float.parseFloat(e.getAttributeValue("y1"));
					float x2 = Float.parseFloat(e.getAttributeValue("x2"));
					float y2 = Float.parseFloat(e.getAttributeValue("y2"));
					lines.createEdgeFixture(body, new Vec2(x1, y1), new Vec2(x2, y2));
				}
				temp.add(body);
				break;
				
			case "object":
				break;
				
			case "man":
				x = Float.parseFloat(b.getAttributeValue("x"));
				y = Float.parseFloat(b.getAttributeValue("y"));
				man.makeMan(x, y);
				break;
			}
		}
		lines.setLines(temp);
	}

	public void save(CurvedLine curve, GameObjects objects, StickMan man) throws FileNotFoundException, IOException {
		Document document = new Document();
		Element root = new Element("doodles");
		document.setRootElement(root);
		
		// Lines
		List<Body> bodies = curve.getLines();

		for (Body body : bodies) {
			Element bodyElement = new Element("body");
			bodyElement.setAttribute("x", String.valueOf(body.getPosition().x));
			bodyElement.setAttribute("y", String.valueOf(body.getPosition().y));
			Fixture f = body.getFixtureList();

			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();

				Element edgeElement = new Element("edge");
				edgeElement.setAttribute("x1", String.valueOf(edge.m_vertex1.x));
				edgeElement.setAttribute("y1", String.valueOf(edge.m_vertex1.y));
				edgeElement.setAttribute("x2", String.valueOf(edge.m_vertex2.x));
				edgeElement.setAttribute("y2", String.valueOf(edge.m_vertex2.y));
				bodyElement.addContent(edgeElement);
				f = f.getNext();
			}
			root.addContent(bodyElement);
		}
		
		// Objects
		
		
		// Stick man
		Element manElement = new Element("man");
		Vec2 pos = man.getPosition();
		manElement.setAttribute("x", String.valueOf(pos.x));
		manElement.setAttribute("y", String.valueOf(pos.y));
		root.addContent(manElement);
		
		XMLOutputter output = new XMLOutputter();
		output.output(document, new FileOutputStream(fileName));
	}
	
	public void reset(CurvedLine lines, GameObjects objects, StickMan man) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build("res/reset.xml");
		Element root = document.getRootElement();
		List<Element> children = root.getChildren();
		List<Body> temp = new ArrayList<Body>();
		lines.load();
		man.load();

		for (Element b : children) {
			switch (b.getName()) {
			
			case "body":
				float x = Float.parseFloat(b.getAttributeValue("x"));
				float y = Float.parseFloat(b.getAttributeValue("y"));
				Body body = lines.createBody(x, y);
				List<Element> bChildren = b.getChildren();

				for (Element e : bChildren) {
					float x1 = Float.parseFloat(e.getAttributeValue("x1"));
					float y1 = Float.parseFloat(e.getAttributeValue("y1"));
					float x2 = Float.parseFloat(e.getAttributeValue("x2"));
					float y2 = Float.parseFloat(e.getAttributeValue("y2"));
					lines.createEdgeFixture(body, new Vec2(x1, y1), new Vec2(x2, y2));
				}
				temp.add(body);
				break;
				
			case "object":
				break;
				
			case "man":
				x = Float.parseFloat(b.getAttributeValue("x"));
				y = Float.parseFloat(b.getAttributeValue("y"));
				man.makeMan(x, y);
				break;
			}
		}
		lines.setLines(temp);
	}
}
