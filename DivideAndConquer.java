import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DivideAndConquer {
	protected Location[] locationArray;
	protected float totalDistance;
	protected ArrayList<Location> shortestPathExhaustive = new ArrayList<Location>();
	protected float shortestDistanceExhaustive;
	protected ArrayList<Location> shortestMergedPath = new ArrayList<Location>();
	protected float shortestMergedDistance;

	DivideAndConquer(Location[] locationArray) {
		this.locationArray = locationArray;
		divideAndConquerAlgorithm();
	}

	public float calculateDistBetweenLocs(Location firstHalfLocation, Location secondHalfLocation) {
		float xAxisDistance = firstHalfLocation.xCoordinate - secondHalfLocation.xCoordinate;
		float yAxisDistance = firstHalfLocation.yCoordinate - secondHalfLocation.yCoordinate;
		return (float) (Math.sqrt(Math.pow(xAxisDistance, 2) + Math.pow(yAxisDistance, 2)));
	}

	public void divideAndConquerAlgorithm() {
		int l = 0;
		int r = locationArray.length - 1;
		divideIntoSubproblems(l, r);
		totalDistance = calculatePathLength(locationArray)
				+ calculateDistBetweenLocs(locationArray[locationArray.length - 1], locationArray[0]);
	}

	public Location[] divideIntoSubproblems(int l, int r) {
		if (r - l + 1 <= 6) {
			return exhaustiveSolution(l, r);
		} else {
			sortLocations(l, r);
			int mid = l + (r - l) / 2;
			return mergePaths(divideIntoSubproblems(l, mid), divideIntoSubproblems(mid + 1, r), l, mid, r);
		}
	}

	public Location[] exhaustiveSolution(int l, int r) {
		ArrayList<Location> allPossiblePaths = new ArrayList<Location>();
		for (int i = l; i <= r; i++) {
			allPossiblePaths.add(locationArray[i]);
		}
		shortestDistanceExhaustive = Float.MAX_VALUE;
		findAllPossiblePaths(allPossiblePaths, 0);
		int counter = 0;
		for (int i = l; i <= r; i++) {
			locationArray[i] = shortestPathExhaustive.get(counter);
			counter++;
		}
		return locationArray;
	}

	public void findAllPossiblePaths(ArrayList<Location> locationList, int k) {
		for (int i = k; i < locationList.size(); i++) {
			Collections.swap(locationList, i, k);
			findAllPossiblePaths(locationList, k + 1);
			Collections.swap(locationList, k, i);
		}
		if (locationList.size() - 1 == k) {
			float length = calculatePathLength(locationList);
			if (length < shortestDistanceExhaustive) {
				shortestDistanceExhaustive = length;
				shortestPathExhaustive.clear();
				for (Location location : locationList) {
					shortestPathExhaustive.add(location);
				}
			}
		}
	}

	public float calculatePathLength(ArrayList<Location> locationList) {
		float totalLength = 0;
		for (int i = 0; i < locationList.size() - 1; i++) {
			totalLength += calculateDistBetweenLocs(locationList.get(i), locationList.get(i + 1));
		}
		return totalLength;
	}

	public float calculatePathLength(Location[] locationArray) {
		float totalLength = 0;
		for (int i = 0; i < locationArray.length - 1; i++) {
			totalLength += calculateDistBetweenLocs(locationArray[i], locationArray[i + 1]);
		}
		return totalLength;
	}

	public void sortLocations(int l, int r) {
		ArrayList<Location> locationList = new ArrayList<Location>();
		for (int i = l; i <= r; i++) {
			locationList.add(locationArray[i]);
		}

		float xCoordinateMinValue = Float.MAX_VALUE;
		float yCoordinateMinValue = Float.MAX_VALUE;
		float xCoordinateMaxValue = Float.MIN_VALUE;
		float yCoordinateMaxValue = Float.MIN_VALUE;

		for (Location location : locationList) {
			if (location.xCoordinate < xCoordinateMinValue) {
				xCoordinateMinValue = location.xCoordinate;
			}
			if (location.yCoordinate < yCoordinateMinValue) {
				yCoordinateMinValue = location.yCoordinate;
			}
			if (location.xCoordinate > xCoordinateMaxValue) {
				xCoordinateMaxValue = location.xCoordinate;
			}
			if (location.yCoordinate > yCoordinateMaxValue) {
				yCoordinateMaxValue = location.yCoordinate;
			}
		}

		float xCoordMaxDistBetweenLocations = xCoordinateMaxValue - xCoordinateMinValue;
		float yCoordMaxDistBetweenLocations = yCoordinateMaxValue - yCoordinateMinValue;

		if (xCoordMaxDistBetweenLocations > yCoordMaxDistBetweenLocations) {
			Collections.sort(locationList, new Comparator<Location>() {
				@Override
				public int compare(Location firstHalfLocation, Location secondHalfLocation) {
					return Float.compare(firstHalfLocation.xCoordinate, secondHalfLocation.xCoordinate);
				}
			});
		} else {
			Collections.sort(locationList, new Comparator<Location>() {
				@Override
				public int compare(Location firstHalfLocation, Location secondHalfLocation) {
					return Float.compare(firstHalfLocation.yCoordinate, secondHalfLocation.yCoordinate);
				}
			});
		}

		int counter = 0;
		for (int i = l; i <= r; i++) {
			locationArray[i] = locationList.get(counter);
			counter++;
		}
	}

	public Location[] mergePaths(Location[] firstPath, Location[] secondPath, int l, int mid, int r) {
		shortestMergedDistance = Float.MAX_VALUE;
		int firstPathArrayLength = mid - l + 1;
		int secondPathArrayLength = r - mid;

		ArrayList<Location> pathFirstHalf = new ArrayList<Location>();
		ArrayList<Location> pathSecondHalf = new ArrayList<Location>();

		for (int i = l; i <= mid; i++) {
			pathFirstHalf.add(locationArray[i]);
		}

		for (int i = mid + 1; i <= r; i++) {
			pathSecondHalf.add(locationArray[i]);
		}

		ArrayList<ArrayList<Location>> temp1 = new ArrayList<ArrayList<Location>>();
		ArrayList<ArrayList<Location>> temp2 = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> pathsMerged = new ArrayList<Location>();

		for (int i = 0; i < firstPathArrayLength; i++) {
			ArrayList<Location> firstHalfReversed = new ArrayList<Location>(pathFirstHalf.subList(0, pathFirstHalf.size()));
			Collections.reverse(firstHalfReversed);
			temp1.clear();
			temp1.add(pathFirstHalf);
			temp1.add(firstHalfReversed);
			for (int j = 0; j < secondPathArrayLength; j++) {
				ArrayList<Location> secondHalfReversed = new ArrayList<Location>(pathSecondHalf.subList(0, pathSecondHalf.size()));
				Collections.reverse(secondHalfReversed);
				temp2.clear();
				temp2.add(pathSecondHalf);
				temp2.add(secondHalfReversed);
				for (int m = 0; m < 2; m++) {
					for (int n = 0; n < 2; n++) {
						for (Location firstHalfLocation : temp1.get(m)) {
							pathsMerged.add(firstHalfLocation);
						}
						for (Location secondHalfLocation : temp2.get(n)) {
							pathsMerged.add(secondHalfLocation);
						}
						pathsMerged.add(temp1.get(m).get(0)); // Loop back to the start
						float distance = calculatePathLength(pathsMerged);
						if (distance < shortestMergedDistance) {
							shortestMergedDistance = distance;
							shortestMergedPath.clear();
							shortestMergedPath.addAll(pathsMerged);
						}
						pathsMerged.clear();
					}
				}
			}
			pathSecondHalf.add(pathSecondHalf.get(0));
			pathSecondHalf.remove(0);
		}
		int counter = 0;
		for (int i = l; i <= r; i++) {
			locationArray[i] = shortestMergedPath.get(counter);
			counter++;
		}
		return locationArray;
	}
}
