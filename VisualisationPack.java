public class VisualisationPack {
	protected final int gapBetweenFrameAndPanel = 60;
	protected final int gapBetweenPanelAndVisualisation = 20;
	protected final float longAxisLength = 600f;

	protected float xCoordinateMinValue, yCoordinateMinValue;
	protected float xCoordMaxDistBetweenLocations, yCoordMaxDistBetweenLocations;
	protected float xAxisPanelVisualisationLength, yAxisPanelVisualisationLength;
	protected int xAxisPanelLength, yAxisPanelLength;
	protected int xAxisFrameLength, yAxisFrameLength;
	protected float resizingValue;
	protected String title;
	protected Location[] locationArray;

	public VisualisationPack(float xCoordinateMinValue, float yCoordinateMinValue,
							 float xCoordMaxDistBetweenLocations, float yCoordMaxDistBetweenLocations,
							 String title, Location[] locationArray) {
		this.xCoordinateMinValue = xCoordinateMinValue;
		this.yCoordinateMinValue = yCoordinateMinValue;
		this.xCoordMaxDistBetweenLocations = xCoordMaxDistBetweenLocations;
		this.yCoordMaxDistBetweenLocations = yCoordMaxDistBetweenLocations;
		this.title = title;
		this.locationArray = locationArray;

		calculateVisualisationSize();
		calculateResizingValue();
	}

	private void calculateVisualisationSize() {
		if (xCoordMaxDistBetweenLocations > yCoordMaxDistBetweenLocations) {
			xAxisPanelVisualisationLength = longAxisLength;
			yAxisPanelVisualisationLength = (xAxisPanelVisualisationLength * yCoordMaxDistBetweenLocations / xCoordMaxDistBetweenLocations);
		} else {
			yAxisPanelVisualisationLength = longAxisLength;
			xAxisPanelVisualisationLength = (yAxisPanelVisualisationLength * xCoordMaxDistBetweenLocations / yCoordMaxDistBetweenLocations);
		}

		xAxisPanelLength = Math.round(xAxisPanelVisualisationLength) + (gapBetweenPanelAndVisualisation * 2);
		yAxisPanelLength = Math.round(yAxisPanelVisualisationLength) + (gapBetweenPanelAndVisualisation * 2);
		xAxisFrameLength = xAxisPanelLength + (gapBetweenFrameAndPanel * 2);
		yAxisFrameLength = yAxisPanelLength + (gapBetweenFrameAndPanel * 2);
	}

	private void calculateResizingValue() {
		resizingValue = xCoordMaxDistBetweenLocations / xAxisPanelVisualisationLength;
	}
}
