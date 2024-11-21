

import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

public class VisualisationPanel extends JPanel {
	protected Location[] locationArray;
	protected float xCoordinateMinValue;
	protected float yCoordinateMinValue;
	protected int gapBetweenPanelAndVisualisation;
	protected float resizingValue;
	protected String title;
	VisualisationPanel(VisualisationPack visualisationPack){
		this.locationArray = visualisationPack.locationArray;
		this.xCoordinateMinValue = visualisationPack.xCoordinateMinValue;
		this.yCoordinateMinValue = visualisationPack.yCoordinateMinValue;
		this.gapBetweenPanelAndVisualisation = visualisationPack.gapBetweenPanelAndVisualisation;
		this.title = visualisationPack.title;
		this.resizingValue = visualisationPack.resizingValue;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g); // Ensure the background is cleared

		// Use Graphics2D for more control
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Set the background to white
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());

		// Draw lines in red
		g2.setColor(Color.RED);
		for (int i = 0; i < locationArray.length - 1; i++) {
			drawScaledLine(g2, locationArray[i], locationArray[i + 1]);
		}
		// Connect last point back to the first
		drawScaledLine(g2, locationArray[locationArray.length - 1], locationArray[0]);

		// Draw dots in black
		g2.setColor(Color.BLACK);
		for (Location location : locationArray) {
			drawScaledDot(g2, location);
		}
	}

	private void drawScaledLine(Graphics2D g2, Location start, Location end) {
		int x1 = Math.round((start.xCoordinate - xCoordinateMinValue) / resizingValue) + gapBetweenPanelAndVisualisation;
		int y1 = Math.round((start.yCoordinate - yCoordinateMinValue) / resizingValue) + gapBetweenPanelAndVisualisation;
		int x2 = Math.round((end.xCoordinate - xCoordinateMinValue) / resizingValue) + gapBetweenPanelAndVisualisation;
		int y2 = Math.round((end.yCoordinate - yCoordinateMinValue) / resizingValue) + gapBetweenPanelAndVisualisation;

		g2.drawLine(x1, y1, x2, y2);
	}

	private void drawScaledDot(Graphics2D g2, Location location) {
		float x = (location.xCoordinate - xCoordinateMinValue) / resizingValue + gapBetweenPanelAndVisualisation - 1.25f;
		float y = (location.yCoordinate - yCoordinateMinValue) / resizingValue + gapBetweenPanelAndVisualisation - 1.25f;

		g2.fill(new Ellipse2D.Float(x, y, 2f, 2f)); // Use 2.5x2.5 dots
	}



}
