package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import round2.CurvedLine;
/**
 * Saves/loads the doodle sketch to and from an xml file.
 * 
 */
public class SaveTools {
	
	private String filename;
	
	/**
	 * 
	 * @param filename
	 */
	public SaveTools(String filename) {
		this.filename = filename;
	}
	
	public void load() throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(filename);
		Element root = document.getRootElement();
		List<Element> children = root.getChildren();
		for (Element e : children) {
			// TODO: figure out what goes here
		}
	}

	public void save(CurvedLine curve) throws FileNotFoundException, IOException {
		Document document = new Document();
		Element root = new Element("doodles");
		document.setRootElement(root);
		List<Body> bodies = curve.getLines();
		for (Body body : bodies) {
			Element bodyElement = new Element("body");
			Fixture f = body.getFixtureList();
			while (f != null) {
				EdgeShape edge = (EdgeShape) f.getShape();
				edge = (EdgeShape) f.getShape();
				f = f.getNext();
				// TODO: figure out what goes here too;
			}
		}
		XMLOutputter output = new XMLOutputter();
		output.output(document, new FileOutputStream(filename));
	}
}
