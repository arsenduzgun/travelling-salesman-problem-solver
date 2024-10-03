

import java.awt.Graphics;
import javax.swing.JPanel;

public class VisualisationPanel extends JPanel {
	protected Location[] locationArray;
	protected float xCoordinateMinValue;
	protected float yCoordinateMinValue;
	protected int gapBetweenPanelAndVisualisation;
	protected float resizingValue;
	protected String visualisationType;
	VisualisationPanel(VisualisationPack visualisationPack){
		this.locationArray = visualisationPack.locationArray;
		this.xCoordinateMinValue = visualisationPack.xCoordinateMinValue;
		this.yCoordinateMinValue = visualisationPack.yCoordinateMinValue;
		this.gapBetweenPanelAndVisualisation = visualisationPack.gapBetweenPanelAndVisualisation;
		this.visualisationType = visualisationPack.visualisationType;
		this.resizingValue = visualisationPack.resizingValue;
	}
    
    @Override
    public void paint(Graphics g) {
    	if(visualisationType.equals("Locations(" + AppInterface.fileName + ")")) {
    		int gapForOval = gapBetweenPanelAndVisualisation - 1;
    		int xCoordinate;
    		int yCoordinate;
    		for(Location location: locationArray) {
        		xCoordinate = Math.round((location.xCoordinate - xCoordinateMinValue) / resizingValue);
        		yCoordinate = Math.round((location.yCoordinate - yCoordinateMinValue) / resizingValue);
        		g.fillOval(xCoordinate + gapForOval, yCoordinate + gapForOval, 2, 2);
        	}
    	}
    	else if(visualisationType.equals("Path(" + AppInterface.fileName + ")(" + AppInterface.selectedMethod + ")")) {
    		int xCoordOfFirstLocation;
    		int yCoordOfFirstLocation;
    		int xCoordOfSecondLocation;
    		int yCoordOfSecondLocation;
    		for(int i = 0; i < locationArray.length - 1; i++) {
    			xCoordOfFirstLocation = Math.round((locationArray[i].xCoordinate - xCoordinateMinValue) / resizingValue);
    			yCoordOfFirstLocation = Math.round((locationArray[i].yCoordinate - yCoordinateMinValue) / resizingValue);
    			xCoordOfSecondLocation = Math.round((locationArray[i + 1].xCoordinate - xCoordinateMinValue) / resizingValue);
    			yCoordOfSecondLocation = Math.round((locationArray[i + 1].yCoordinate - yCoordinateMinValue) / resizingValue);
        		g.drawLine(
        		xCoordOfFirstLocation + gapBetweenPanelAndVisualisation,
        		yCoordOfFirstLocation + gapBetweenPanelAndVisualisation,
        		xCoordOfSecondLocation + gapBetweenPanelAndVisualisation,
        		yCoordOfSecondLocation + gapBetweenPanelAndVisualisation
        		);
        	}
    		xCoordOfFirstLocation = Math.round((locationArray[locationArray.length - 1].xCoordinate - xCoordinateMinValue) / resizingValue);
			yCoordOfFirstLocation = Math.round((locationArray[locationArray.length - 1].yCoordinate - yCoordinateMinValue) / resizingValue);
			xCoordOfSecondLocation = Math.round((locationArray[0].xCoordinate - xCoordinateMinValue) / resizingValue);
			yCoordOfSecondLocation = Math.round((locationArray[0].yCoordinate - yCoordinateMinValue) / resizingValue);
			g.drawLine(
	        xCoordOfFirstLocation + gapBetweenPanelAndVisualisation,
	        yCoordOfFirstLocation + gapBetweenPanelAndVisualisation,
	        xCoordOfSecondLocation + gapBetweenPanelAndVisualisation,
	        yCoordOfSecondLocation + gapBetweenPanelAndVisualisation
	        );
    	}
    }
}
