

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VisualisationFrame extends JFrame {
	protected int gapBetweenFrameAndPanel;
	protected int gapBetweenPanelAndVisualisation;
	protected int xAxisPanelLength;
	protected int yAxisPanelLength;
	protected int xAxisFrameLength;
	protected int yAxisFrameLength;
	VisualisationFrame(VisualisationPack visualisationPack){
        super(visualisationPack.visualisationType);
        this.gapBetweenFrameAndPanel = visualisationPack.gapBetweenFrameAndPanel;
        this.gapBetweenPanelAndVisualisation = visualisationPack.gapBetweenPanelAndVisualisation;
        this.xAxisPanelLength = visualisationPack.xAxisPanelLength;
        this.yAxisPanelLength = visualisationPack.yAxisPanelLength;
        this.xAxisFrameLength = visualisationPack.xAxisFrameLength;
        this.yAxisFrameLength = visualisationPack.yAxisFrameLength;
        setFrame();
        setPanel(visualisationPack);
        setVisible(true);
    }
    public void setFrame() {
    	setSize(xAxisFrameLength, yAxisFrameLength);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(Color.white);
    }
    public void setPanel(VisualisationPack visualisationPack) {
    	JPanel locationsVisualisationPanel = new VisualisationPanel(visualisationPack);
        locationsVisualisationPanel.setBounds(gapBetweenFrameAndPanel, gapBetweenFrameAndPanel, xAxisPanelLength, yAxisPanelLength);
        add(locationsVisualisationPanel);
        repaint();
    }
}
