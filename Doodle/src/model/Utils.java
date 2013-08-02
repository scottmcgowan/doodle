package model;

public class Utils {
	
	// TODO: Screen width and height
	
	
	// Scales using 50px == 1 meter?
	
	//Convert a JBox2D x coordinate to a pixel x coordinate
    public static float toPixelPosX(float xMeters) {
        float xPixels = 50.0f * xMeters;
        return xPixels;
    }
    
    //Convert a JBox2D y coordinate to a pixel y coordinate
    public static float toPixelPosY(float yMeters) {
        float yPixels = 50.0f * yMeters;
        return yPixels;
    }

    //Convert a pixel x coordinate to a JBox2D x coordinate
    public static float toPosX(float xPixles) {
        float xMeters = 0.02f * xPixles;
        return xMeters;
    }
    
    //Convert a pixel y coordinate to a JBox2D y coordinate
    public static float toPosY(float yPixles) {
        float yMeters = 0.02f * yPixles;
        return yMeters;
    }
	
}
