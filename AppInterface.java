import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AppInterface extends JFrame implements Runnable, ActionListener {
    private static final int FRAME_WIDTH = 550;
    private static final int FRAME_HEIGHT = 500;

    private JButton searchFileButton;
    private JTextArea selectedFileArea;
    private JButton findPathButton;
    private JTextArea output;
    private String filePath;
    private static String fileName;
    private Thread thread;
    private Timer timer;
    private float totalDistance;
    private long start;
    private long elapsedTime;
    private String processing;
    private Location[] locationArray;
    private FileReader fileReader;

    public AppInterface() {
        super("TSP Solver");
        initializeLookAndFeel();
        setupFrame();
        setupComponents();
        setupTimer();
        setFocusTraversalPolicy(new NoFocusTraversalPolicy());
        setVisible(true);
    }

    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFrame() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
    }

    private void setupComponents() {
        searchFileButton = new JButton("Choose File");
        searchFileButton.setBounds(50, 50, 110, 24);
        searchFileButton.addActionListener(this);
        add(searchFileButton);

        JLabel selectedFileLabel = new JLabel("Selected File: ");
        selectedFileLabel.setBounds(220, 50, 130, 24);
        add(selectedFileLabel);

        selectedFileArea = new JTextArea();
        selectedFileArea.setEditable(false);
        selectedFileArea.setBounds(320, 53, 170, 19);
        add(selectedFileArea);

        findPathButton = new JButton("Find Path");
        findPathButton.setBounds(200, 200, 128, 24);
        findPathButton.addActionListener(this);
        add(findPathButton);

        output = new JTextArea();
        output.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(output);
        outputScrollPane.setBounds(0, 300, FRAME_WIDTH - 13, FRAME_HEIGHT - 336);
        add(outputScrollPane);
    }

    private void setupTimer() {
        timer = new Timer(500, e -> {
            if (processing.length() == 14) {
                processing = processing.substring(0, 10);
            }
            output.setText(processing);
            processing += ".";
        });
    }

    private boolean doesFilePathExist() {
        return filePath != null;
    }

    private boolean isExtensionAcceptable(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf("."));
        return extension.equals(".txt") || extension.equals(".tsp");
    }

    private void analysisStarted() {
        searchFileButton.setEnabled(false);
        findPathButton.setText("Stop Processing");
        processing = "Processing";
        timer.start();
    }

    private void analysisFinished() {
        timer.stop();
        output.setText("");
        printOutput();
        displayVisualization();
        searchFileButton.setEnabled(true);
        findPathButton.setText("Find Path");
    }

    private void printOutput() {
        output.append("File name: " + fileName +
                "\n\nNumber of locations: " + locationArray.length +
                "\n\nTotal length of the path: " + totalDistance +
                "\n\nElapsed time: " + elapsedTime + " milliseconds\n\n");

        for (int i = 0; i < locationArray.length; i++) {
            output.append((i + 1) + "-) Location name: " + locationArray[i].locationName +
                    " - x: " + locationArray[i].xCoordinate +
                    " - y: " + locationArray[i].yCoordinate + "\n");
        }
    }

    private void displayVisualization() {
        VisualisationPack visualisationPack = new VisualisationPack(
                fileReader.xCoordinateMinValue,
                fileReader.yCoordinateMinValue,
                fileReader.xCoordMaxDistBetweenLocations,
                fileReader.yCoordMaxDistBetweenLocations,
                "Path - " + fileName,
                locationArray);
        new VisualisationFrame(visualisationPack);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchFileButton) {
            handleFileSelection();
        } else if (e.getSource() == findPathButton) {
            handlePathFinding();
        }
    }

    private void handleFileSelection() {
        JFileChooser fileChooser = new JFileChooser();
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            String selectedPath = fileChooser.getSelectedFile().getPath();
            if (isExtensionAcceptable(selectedPath)) {
                filePath = selectedPath;
                fileName = new File(filePath).getName();
                selectedFileArea.setText(fileName);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid file type. Select a .txt or .tsp file.");
            }
        }
    }

    private void handlePathFinding() {
        if (!doesFilePathExist()) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }

        if (thread != null && thread.isAlive()) {
            stopThread();
        } else {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void stopThread() {
        timer.stop();
        thread.interrupt();
        findPathButton.setText("Find Path");
        searchFileButton.setEnabled(true);
        output.setText("");
    }

    @Override
    public void run() {
        try {
            analysisStarted();
            fileReader = new FileReader(filePath);
            locationArray = fileReader.locationArray;
            start = System.currentTimeMillis();

            EnhancedNearestNeighbour enhancedNearestNeighbour = new EnhancedNearestNeighbour(locationArray);
            locationArray = enhancedNearestNeighbour.locationArray;
            totalDistance = enhancedNearestNeighbour.totalDistance;

            elapsedTime = System.currentTimeMillis() - start;
            analysisFinished();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing file.");
        }
    }

    public static void main(String[] args) {
        new AppInterface();
    }

    private static class NoFocusTraversalPolicy extends FocusTraversalPolicy {
        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            return null;
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            return null;
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            return null;
        }

        @Override
        public Component getLastComponent(Container focusCycleRoot) {
            return null;
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            return null;
        }
    }
}
