package round2;

import org.jbox2d.dynamics.Body;

import round2.GameObjects.ShapeType;

public class MyBody  {

	private Body body;
	private ShapeType type;
	private float param1;
	private float param2;
	
	public MyBody(Body body, ShapeType type, float param1, float param2) {
		super();
		this.body = body;
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
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
}
