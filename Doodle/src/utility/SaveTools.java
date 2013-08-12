package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import round2.CurvedLine;
import round2.Doodle;
import round2.GameObjects;
import round2.GameObjects.ShapeType;
import round2.MyBody;
import round2.StickMan;

/**
 * Saves/loads the doodle sketch to and from an xml file.
 * 
 */
public class SaveTools {

	/**
	 * Loads the given game objects from the given xml file.
	 * 
	 * @param fileName
	 *            - the file name for the xml file.
	 * @param lines
	 *            - the curved lines from the game.
	 * @param objects
	 *            - the game objects from the game.
	 * @param man
	 *            - the stick man from the game.
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static void load(String fileName, CurvedLine lines, GameObjects objects, StickMan man) throws JDOMException,
			IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(fileName);
		Element root = document.getRootElement();
		List<Element> children = root.getChildren();
		List<Body> tempLines = new ArrayList<Body>();
		lines.destroyBodies();
		objects.destroyBodies();
		man.destroyBody();

		for (Element b : children) {
			switch (b.getName()) {

			// Lines
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
				tempLines.add(body);
				break;

			// Objects
			case "object":
//				if (!ShapeType.valueOf(b.getAttributeValue("shapeType")).equals(ShapeType.CIRCLE)) {
					ShapeType shapeType = ShapeType.valueOf(b.getAttributeValue("shapeType"));
					BodyType bodyType = BodyType.valueOf(b.getAttributeValue("bodyType"));
					x = Float.parseFloat(b.getAttributeValue("x"));
					y = Float.parseFloat(b.getAttributeValue("y"));
					float param1 = Float.parseFloat(b.getAttributeValue("param1"));
					float param2 = Float.parseFloat(b.getAttributeValue("param2"));
					float angle = Float.parseFloat(b.getAttributeValue("angle"));
					String userData = b.getAttributeValue("userData");
					float density = Float.parseFloat(b.getAttributeValue("density"));
					float friction = Float.parseFloat(b.getAttributeValue("friction"));
					float restitution = Float.parseFloat(b.getAttributeValue("restitution"));
					objects.createObject(shapeType, bodyType, x, y, param1, param2, angle, userData, density, friction,
							restitution);
//				}
				break;

			// Stick man
			case "man":
				x = Float.parseFloat(b.getAttributeValue("x"));
				y = Float.parseFloat(b.getAttributeValue("y"));
				man.makeMan(x, y + 0.25f);
				break;
			
			// Camera angle
			case "display":
				Doodle.TRANSLATE.x = Float.parseFloat(b.getAttributeValue("x"));
				Doodle.TRANSLATE.y = Float.parseFloat(b.getAttributeValue("y"));
				Doodle.METER_SCALE = Float.parseFloat(b.getAttributeValue("scale"));
			}
		}
		lines.setLines(tempLines);
		Doodle.numFootContacts = 0;
	}

	/**
	 * Saves the current game state to the given xml file.
	 * 
	 * @param fileName
	 *            - the file name for the xml file.
	 * @param lines
	 *            - the curved lines from the game.
	 * @param objects
	 *            - the game objects from the game.
	 * @param man
	 *            - the stick man from the game.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void save(String fileName, CurvedLine lines, GameObjects objects, StickMan man)
			throws FileNotFoundException, IOException {
		Document document = new Document();
		Element root = new Element("doodles");
		document.setRootElement(root);

		// Lines
		List<Body> bodies = lines.getLines();

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
		List<MyBody> objectList = objects.getList();

		for (MyBody myBody : objectList) {
//			if (myBody.getType() != ShapeType.CIRCLE) {
				Body body = myBody.getBody();
				Vec2 pos = body.getPosition();
				Element objectElement = new Element("object");
				objectElement.setAttribute("shapeType", String.valueOf(myBody.getType()));
				objectElement.setAttribute("bodyType", String.valueOf(body.getType()));
				objectElement.setAttribute("x", String.valueOf(pos.x));
				objectElement.setAttribute("y", String.valueOf(pos.y));
				objectElement.setAttribute("param1", String.valueOf(myBody.getParam1()));
				objectElement.setAttribute("param2", String.valueOf(myBody.getParam2()));
				objectElement.setAttribute("angle", String.valueOf(body.getAngle()));
				objectElement.setAttribute("userData", myBody.getUserData());
				objectElement.setAttribute("density", String.valueOf(myBody.getDensity()));
				objectElement.setAttribute("friction", String.valueOf(myBody.getFriction()));
				objectElement.setAttribute("restitution", String.valueOf(myBody.getRestitution()));
				root.addContent(objectElement);
//			}
		}

		// Stick man
		Element manElement = new Element("man");
		Vec2 pos = man.getPosition();
		manElement.setAttribute("x", String.valueOf(pos.x));
		manElement.setAttribute("y", String.valueOf(pos.y + 0.15f));
		root.addContent(manElement);
		
		// Camera
		Element display = new Element("display");
		display.setAttribute("x", String.valueOf(Doodle.TRANSLATE.x));
		display.setAttribute("y", String.valueOf(Doodle.TRANSLATE.y));
		display.setAttribute("scale", String.valueOf(Doodle.METER_SCALE));
		root.addContent(display);

		XMLOutputter output = new XMLOutputter();
		output.output(document, new FileOutputStream(fileName));
	}
}
