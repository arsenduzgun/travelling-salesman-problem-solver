import java.util.Random;

public class EnhancedNearestNeighbour {
	protected Location[] locationArray;
	protected float totalDistance = 0;
	private int numberOfLocations;
	private Random random;

	EnhancedNearestNeighbour(Location[] locationArray) {
		this.locationArray = locationArray;
		this.numberOfLocations = locationArray.length;
		this.random = new Random();
		nearestNeighbourAlgorithmWithMultipleStarts();
	}

	public float calculateDistBetweenLocs(Location location1, Location location2) {
		float xAxisDistance = location1.xCoordinate - location2.xCoordinate;
		float yAxisDistance = location1.yCoordinate - location2.yCoordinate;
		return (float) Math.sqrt(Math.pow(xAxisDistance, 2) + Math.pow(yAxisDistance, 2));
	}

	public void swap(int firstElementIndex, int secondElementIndex) {
		if (firstElementIndex >= 0 && firstElementIndex < numberOfLocations &&
				secondElementIndex >= 0 && secondElementIndex < numberOfLocations) {
			Location elementHolder = locationArray[firstElementIndex];
			locationArray[firstElementIndex] = locationArray[secondElementIndex];
			locationArray[secondElementIndex] = elementHolder;
		}
	}

	public void nearestNeighbourAlgorithmWithMultipleStarts() {
		float bestDistance = Float.MAX_VALUE;
		Location[] bestPath = null;

		for (int start = 0; start < numberOfLocations; start++) {
			float tempTotalDistance = 0;
			Location[] tempArray = locationArray.clone();
			tempTotalDistance = nearestNeighbourAlgorithm(tempArray, start);

			if (tempTotalDistance < bestDistance) {
				bestDistance = tempTotalDistance;
				bestPath = tempArray.clone();
			}
		}

		locationArray = bestPath;
		totalDistance = bestDistance;

		twoOptOptimization();

		totalDistance = calculateTotalDistance();
	}

	public float nearestNeighbourAlgorithm(Location[] locations, int startIndex) {
		boolean[] visited = new boolean[numberOfLocations];
		visited[startIndex] = true;
		float pathDistance = 0;

		int currentLocationIndex = startIndex;
		for (int i = 1; i < numberOfLocations; i++) {
			int nearestLocationIndex = -1;
			float shortestDistance = Float.MAX_VALUE;

			for (int j = 0; j < numberOfLocations; j++) {
				if (!visited[j]) {
					float distance = calculateDistBetweenLocs(locations[currentLocationIndex], locations[j]);
					if (distance < shortestDistance) {
						shortestDistance = distance;
						nearestLocationIndex = j;
					}
				}
			}

			if (nearestLocationIndex != -1) {
				visited[nearestLocationIndex] = true;
				pathDistance += shortestDistance;
				swap(currentLocationIndex + 1, nearestLocationIndex);
				currentLocationIndex = nearestLocationIndex;
			}
		}

		pathDistance += calculateDistBetweenLocs(locations[currentLocationIndex], locations[startIndex]);
		return pathDistance;
	}

	public void twoOptOptimization() {
		boolean improvement = true;

		while (improvement) {
			improvement = false;

			for (int i = 1; i < numberOfLocations - 1; i++) {
				for (int j = i + 1; j < numberOfLocations; j++) {
					float currentDist = calculateDistBetweenLocs(locationArray[i - 1], locationArray[i])
							+ calculateDistBetweenLocs(locationArray[j], locationArray[(j + 1) % numberOfLocations]);
					float newDist = calculateDistBetweenLocs(locationArray[i - 1], locationArray[j])
							+ calculateDistBetweenLocs(locationArray[i], locationArray[(j + 1) % numberOfLocations]);

					if (newDist < currentDist) {
						reverseSegment(i, j);
						improvement = true;
					}
				}
			}
		}
	}

	public void reverseSegment(int i, int j) {
		while (i < j) {
			swap(i, j);
			i++;
			j--;
		}
	}

	// Calculate total distance based on current location array
	public float calculateTotalDistance() {
		float distance = 0;
		for (int i = 0; i < numberOfLocations - 1; i++) {
			distance += calculateDistBetweenLocs(locationArray[i], locationArray[i + 1]);
		}
		distance += calculateDistBetweenLocs(locationArray[numberOfLocations - 1], locationArray[0]);
		return distance;
	}
}
