
import java.util.ArrayList;
import java.util.Collections;

public class Greedy {
	protected Location[] locationArray;
    protected float totalDistance = 0;
    MergeInfo mergeInfo = new MergeInfo();
    Greedy(Location[] locationArray){
        this.locationArray = locationArray;
        greedyAlgorithm();
    }
    public void greedyAlgorithm() {
    	ArrayList<Segment> segmentList = new ArrayList<Segment>();
    	for(Location location: locationArray) {
    		Segment segment = new Segment();
    		segment.leftEndpoint = location;
    		segment.rightEndpoint = location;
    		segment.locationList.add(location);
    		segmentList.add(segment);
    	}
    	float minDistance;
    	while(true) {
    		minDistance = Float.MAX_VALUE;
    		for(int i = 0; i < segmentList.size(); i++) {
        		for(int j = i + 1; j < segmentList.size(); j++) {
        			if(segmentList.get(i).locationList.size() == 1) {
        				if(segmentList.get(j).locationList.size() == 1) {
        					float distance = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).leftEndpoint);
        					if(distance < minDistance) {
        						minDistance = distance;
        						setMergeInfo(i, j, "right", "left");
        					}
        				}
        				else {
        					float distance1 = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).leftEndpoint);
        					float distance2 = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).rightEndpoint);
        					if(distance1 < distance2) {
        						if(distance1 < minDistance) {
        							minDistance = distance1;
        							setMergeInfo(i, j, "right", "left");
        						}
        					}
        					else {
        						if(distance2 < minDistance) {
        							minDistance = distance2;
        							setMergeInfo(i, j, "right", "right");
        						}
        					}
        				}
        			}
        			else {
        				if(segmentList.get(j).locationList.size() == 1) {
        					float distance1 = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).leftEndpoint);
        					float distance2 = calculateDistBetweenLocs(segmentList.get(i).leftEndpoint, segmentList.get(j).leftEndpoint);
        					if(distance1 < distance2) {
        						if(distance1 < minDistance) {
        							minDistance = distance1;
        							setMergeInfo(i, j, "right", "left");
        						}
        					}
        					else {
        						if(distance2 < minDistance) {
        							minDistance = distance2;
        							setMergeInfo(i, j, "left", "left");
        						}
        					}
        				}
        				else {
        					float distance1 = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).leftEndpoint);
        					float distance2 = calculateDistBetweenLocs(segmentList.get(i).leftEndpoint, segmentList.get(j).leftEndpoint);
        					float distance3 = calculateDistBetweenLocs(segmentList.get(i).rightEndpoint, segmentList.get(j).rightEndpoint);
        					float distance4 = calculateDistBetweenLocs(segmentList.get(i).leftEndpoint, segmentList.get(j).rightEndpoint);
        					if(distance1 < distance2) {
        						if(distance3 < distance4) {
        							if(distance1 < distance3) {
        								if(distance1 < minDistance) {
        									minDistance = distance1;
        									setMergeInfo(i, j, "right", "left");
        								}
        							}
        							else {
        								if(distance3 < minDistance) {
        									minDistance = distance3;
        									setMergeInfo(i, j, "right", "right");
        								}
        							}
        						}
        						else {
        							if(distance1 < distance4) {
        								if(distance1 < minDistance) {
        									minDistance = distance1;
        									setMergeInfo(i, j, "right", "left");
        								}
        							}
        							else {
        								if(distance4 < minDistance) {
        									minDistance = distance4;
        									setMergeInfo(i, j, "left", "right");
        								}
        							}
        						}
        					}
        					else {
        						if(distance3 < distance4) {
        							if(distance2 < distance3) {
        								if(distance2 < minDistance) {
        									minDistance = distance2;
        									setMergeInfo(i, j, "left", "left");
        								}
        							}
        							else {
        								if(distance3 < minDistance) {
        									minDistance = distance3;
        									setMergeInfo(i, j, "right", "right");
        								}
        							}
        						}
        						else {
        							if(distance2 < distance4) {
        								if(distance2 < minDistance) {
        									minDistance = distance2;
        									setMergeInfo(i, j, "left", "left");
        								}
        							}
        							else {
        								if(distance4 < minDistance) {
        									minDistance = distance4;
        									setMergeInfo(i, j, "left", "right");
        								}
        							}
        						}
        					}
        				}
        			}
        		}
        	}
        	if(mergeInfo.firstSegmentEndpoint.equals("left") && mergeInfo.secondSegmentEndpoint.equals("left")) {
        		Collections.reverse(segmentList.get(mergeInfo.firstSegmentIndex).locationList);
        		segmentList.get(mergeInfo.firstSegmentIndex).locationList.addAll(segmentList.get(mergeInfo.secondSegmentIndex).locationList);
        		segmentList.get(mergeInfo.firstSegmentIndex).leftEndpoint = segmentList.get(mergeInfo.firstSegmentIndex).rightEndpoint;
        		segmentList.get(mergeInfo.firstSegmentIndex).rightEndpoint = segmentList.get(mergeInfo.secondSegmentIndex).rightEndpoint;
        		segmentList.remove(mergeInfo.secondSegmentIndex);
        	}
        	else if(mergeInfo.firstSegmentEndpoint.equals("right") && mergeInfo.secondSegmentEndpoint.equals("left")) {
        		segmentList.get(mergeInfo.firstSegmentIndex).locationList.addAll(segmentList.get(mergeInfo.secondSegmentIndex).locationList);
        		segmentList.get(mergeInfo.firstSegmentIndex).rightEndpoint = segmentList.get(mergeInfo.secondSegmentIndex).rightEndpoint;
        		segmentList.remove(mergeInfo.secondSegmentIndex);
        	}
        	else if(mergeInfo.firstSegmentEndpoint.equals("right") && mergeInfo.secondSegmentEndpoint.equals("right")) {
        		Collections.reverse(segmentList.get(mergeInfo.secondSegmentIndex).locationList);
        		segmentList.get(mergeInfo.firstSegmentIndex).locationList.addAll(segmentList.get(mergeInfo.secondSegmentIndex).locationList);
        		segmentList.get(mergeInfo.firstSegmentIndex).rightEndpoint = segmentList.get(mergeInfo.secondSegmentIndex).leftEndpoint;
        		segmentList.remove(mergeInfo.secondSegmentIndex);
        	}
        	else {
        		segmentList.get(mergeInfo.secondSegmentIndex).locationList.addAll(segmentList.get(mergeInfo.firstSegmentIndex).locationList);
        		segmentList.get(mergeInfo.secondSegmentIndex).rightEndpoint = segmentList.get(mergeInfo.firstSegmentIndex).rightEndpoint;
        		segmentList.remove(mergeInfo.firstSegmentIndex);
        	}
        	if(segmentList.size() == 1) {
        		totalDistance = calculatePathLength(segmentList.get(0).locationList) +
        				        calculateDistBetweenLocs(segmentList.get(0).leftEndpoint, segmentList.get(0).rightEndpoint);
        		break;
        	}
    	}
    	for(int i = 0; i < locationArray.length; i++) {
    		locationArray[i] = segmentList.get(0).locationList.get(i);
    	}
    }
    public void setMergeInfo(int firstSegmentIndex, int secondSegmentIndex, String firstSegmentEndpoint, String secondSegmentEndpoint) {
    	mergeInfo.firstSegmentIndex = firstSegmentIndex;
    	mergeInfo.secondSegmentIndex = secondSegmentIndex;
    	mergeInfo.firstSegmentEndpoint = firstSegmentEndpoint;
    	mergeInfo.secondSegmentEndpoint = secondSegmentEndpoint;
    }
    public float calculateDistBetweenLocs(Location firstHalfLocation, Location secondHalfLocation) {
    	float xAxisDistance = firstHalfLocation.xCoordinate - secondHalfLocation.xCoordinate;
    	float yAxisDistance = firstHalfLocation.yCoordinate - secondHalfLocation.yCoordinate;
    	return (float)(Math.sqrt(Math.pow(xAxisDistance, 2) + Math.pow(yAxisDistance, 2)));
    }
    public float calculatePathLength(ArrayList<Location> locationList) {
    	float totalLength = 0;
    	for(int i = 0; i < locationList.size() - 1; i++) {
    		totalLength += calculateDistBetweenLocs(locationList.get(i), locationList.get(i + 1));
    	}
    	return totalLength;
    }
    class Segment{
    	protected Location leftEndpoint;
    	protected Location rightEndpoint;
    	protected ArrayList<Location> locationList = new ArrayList<Location>();
    }
    class MergeInfo{
    	protected int firstSegmentIndex;
    	protected String firstSegmentEndpoint;
    	protected int secondSegmentIndex;
    	protected String secondSegmentEndpoint;
    }
}
