

public class Location {
	protected String locationName;
    protected float xCoordinate;
    protected float yCoordinate;
    protected int id;
    Location(String locationName, float xCoordinate, float yCoordinate){
    	this.locationName = locationName;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }
}
