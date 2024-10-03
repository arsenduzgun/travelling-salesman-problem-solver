


public class NearestNeighbour {
    protected Location[] locationArray;
    protected float totalDistance = 0;
    NearestNeighbour(Location[] locationArray){
        this.locationArray = locationArray;
        nearestNeighbourAlgorithm();
    }
    public float calculateDistBetweenLocs(Location location1, Location location2) {
    	float xAxisDistance = location1.xCoordinate - location2.xCoordinate;
    	float yAxisDistance = location1.yCoordinate - location2.yCoordinate;
    	return (float)(Math.sqrt(Math.pow(xAxisDistance, 2) + Math.pow(yAxisDistance, 2)));
    }
    public void swap(int firstElementIndex, int secondElementIndex) {
    	Location elementHolder = locationArray[firstElementIndex];
    	locationArray[firstElementIndex] = locationArray[secondElementIndex];
    	locationArray[secondElementIndex] = elementHolder;
    }
    public void nearestNeighbourAlgorithm() {
    	Location sourceLocation;
    	Location targetLocation;
    	int NearestLocationIndex = -1;
    	float distance;
    	float distanceHolder;
    	for(int i = 0; i < locationArray.length - 1; i++) {
    		sourceLocation = locationArray[i];
    		distance = Float.MAX_VALUE;
    		for(int j = i + 1; j < locationArray.length; j++) {
    			targetLocation = locationArray[j];
    			distanceHolder = calculateDistBetweenLocs(sourceLocation, targetLocation);
    			if(distanceHolder < distance) {
    				distance = distanceHolder;
    				NearestLocationIndex = j;
    			}
    		}
    		totalDistance += distance;
    	    swap(i + 1, NearestLocationIndex);
    	}
    	totalDistance += calculateDistBetweenLocs(locationArray[locationArray.length - 1], locationArray[0]);
    }
}
