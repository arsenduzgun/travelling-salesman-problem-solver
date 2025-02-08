import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VisualisationFrame extends JFrame {
    private final int gapBetweenFrameAndPanel;
    private final int xAxisPanelLength;
    private final int yAxisPanelLength;
    private final int xAxisFrameLength;
    private final int yAxisFrameLength;

    public VisualisationFrame(VisualisationPack visualisationPack) {
        super(visualisationPack.title);

        // Initialize fields
        this.gapBetweenFrameAndPanel = visualisationPack.gapBetweenFrameAndPanel;
        this.xAxisPanelLength = visualisationPack.xAxisPanelLength;
        this.yAxisPanelLength = visualisationPack.yAxisPanelLength;
        this.xAxisFrameLength = visualisationPack.xAxisFrameLength;
        this.yAxisFrameLength = visualisationPack.yAxisFrameLength;

        // Configure frame and panel
        initializeFrame();
        initializePanel(visualisationPack);

        // Make the frame visible
        setVisible(true);
    }

    private void initializeFrame() {
        setSize(xAxisFrameLength, yAxisFrameLength);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
    }

    private void initializePanel(VisualisationPack visualisationPack) {
        // Create and configure the visualization panel
        JPanel locationsVisualisationPanel = new VisualisationPanel(visualisationPack);
        locationsVisualisationPanel.setBounds(
                gapBetweenFrameAndPanel,
                gapBetweenFrameAndPanel,
                xAxisPanelLength,
                yAxisPanelLength
        );
        add(locationsVisualisationPanel);
    }
}
