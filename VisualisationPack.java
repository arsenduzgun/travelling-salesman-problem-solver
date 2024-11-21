
public class VisualisationPack{
	protected final int gapBetweenFrameAndPanel = 60;
	protected final int gapBetweenPanelAndVisualisation = 20;
	protected final float longAxisLength = 600f;
	protected float xCoordinateMinValue;
	protected float yCoordinateMinValue;
	protected float xCoordMaxDistBetweenLocations;
	protected float yCoordMaxDistBetweenLocations;
	protected float xAxisPanelVisualisationLength;
	protected float yAxisPanelVisualisationLength;
	protected int xAxisPanelLength;
	protected int yAxisPanelLength;
	protected int xAxisFrameLength;
	protected int yAxisFrameLength;
	protected float resizingValue;
	protected String title;
	protected Location[] locationArray;
	VisualisationPack(float xCoordinateMinValue, float yCoordinateMinValue,
	float xCoordMaxDistBetweenLocations, float yCoordMaxDistBetweenLocations,
	String title, Location[] locationArray){
		this.xCoordinateMinValue = xCoordinateMinValue;
		this.yCoordinateMinValue = yCoordinateMinValue;
		this.xCoordMaxDistBetweenLocations = xCoordMaxDistBetweenLocations;
		this.yCoordMaxDistBetweenLocations = yCoordMaxDistBetweenLocations;
		this.title = title;
		this.locationArray = locationArray;
		visualisationSizeCalculator();
		calculateResizingValue();
	}
	public void visualisationSizeCalculator() {
    	if(xCoordMaxDistBetweenLocations > yCoordMaxDistBetweenLocations) {
    		xAxisPanelVisualisationLength = longAxisLength;
    		yAxisPanelVisualisationLength = (xAxisPanelVisualisationLength * (yCoordMaxDistBetweenLocations / xCoordMaxDistBetweenLocations));
    	}
    	else {
    		yAxisPanelVisualisationLength = longAxisLength;
    		xAxisPanelVisualisationLength = (yAxisPanelVisualisationLength * (xCoordMaxDistBetweenLocations / yCoordMaxDistBetweenLocations));
    	}
    	xAxisPanelLength = Math.round(xAxisPanelVisualisationLength) + (gapBetweenPanelAndVisualisation * 2);
		yAxisPanelLength = Math.round(yAxisPanelVisualisationLength) + (gapBetweenPanelAndVisualisation * 2);
		xAxisFrameLength = xAxisPanelLength + (gapBetweenFrameAndPanel * 2);
		yAxisFrameLength = yAxisPanelLength + (gapBetweenFrameAndPanel * 2);
    }
	public void calculateResizingValue() {
    	resizingValue = xCoordMaxDistBetweenLocations / xAxisPanelVisualisationLength;
    }
}