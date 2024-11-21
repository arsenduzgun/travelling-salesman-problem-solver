
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;


public class AppInterface extends JFrame implements Runnable, ActionListener {
    protected JButton searchFileButton;
    protected JTextArea selectedFileArea;
    protected JButton findPathButton;
    protected JTextArea output;
    protected String filePath;
    protected static String fileName;
    protected Thread thread;
    protected Timer timer;
    protected float totalDistance;
    protected long start;
    protected long elapsedTime;
    protected String processing;
    protected Location[] locationArray;
    protected FileReader fileReader;
    AppInterface(){
        super("TSP Solver");
        try {
            // Set the Look and Feel to the system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setSize(550, 500);
        setDefaultCloseOperation(AppInterface.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setup();
        setTimer();
        setVisible(true);
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
                return null; // Prevents focus traversal
            }

            @Override
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                return null; // Prevents focus traversal
            }

            @Override
            public Component getFirstComponent(Container focusCycleRoot) {
                return null; // No initial focus
            }

            @Override
            public Component getLastComponent(Container focusCycleRoot) {
                return null; // No initial focus
            }

            @Override
            public Component getDefaultComponent(Container focusCycleRoot) {
                return null; // No default component
            }
        });

    }

    public void setup(){
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
        outputScrollPane.setBounds(0, 300, 537, 164);
        add(outputScrollPane);
        getRootPane().requestFocusInWindow();

    }

    public boolean doesFilePathExist(){
        return (filePath != null);
    }

    public boolean isExtensionAcceptable(String filePath){
        String extension = filePath.substring(filePath.lastIndexOf("."));
        if(extension.equals(".txt") || extension.equals(".tsp")){
            return true;
        }
        return false;
    }

    public void analysisStarted() {
    	searchFileButton.setEnabled(false);
    	findPathButton.setText("Stop Processing");
    	processing = "Processing";
        timer.start();
    }

    public void analysisFinished() {
    	timer.stop();
    	output.setText("");
    	printOutput();
    	displayVisualisation();
    	searchFileButton.setEnabled(true);
    	findPathButton.setText("Find Path");
    }

    public void printOutput() {
    	output.append("File name: " + fileName +
 		       		  "\n\n" + "Number of locations: " + locationArray.length +
 		       		  "\n\n" + "Total length of the path: " + totalDistance +
 		       		  "\n\n" + "Elapsed time: " + elapsedTime + " milliseconds" +
 		       		  "\n\n" + "Location order of the shortest path found: ");
    	for(int i = 0; i < locationArray.length; i++) {
        	output.append("\n\n" + (i + 1) + "-) Location name: " + locationArray[i].locationName +
        			      " - " + "x Coordinate: " + locationArray[i].xCoordinate +
        			      " - " + "y Coordinate: " + locationArray[i].yCoordinate);
        }
    	output.append("\n\n" + (locationArray.length + 1) + "-) Location name: " + locationArray[0].locationName +
  		      		  " - " + "x Coordinate: " + locationArray[0].xCoordinate +
  		      		  " - " + "y Coordinate: " + locationArray[0].yCoordinate);
    }

    public void displayVisualisation() {
    	VisualisationPack visualisationPack = new VisualisationPack(fileReader.xCoordinateMinValue,
			    							  fileReader.yCoordinateMinValue,
			    							  fileReader.xCoordMaxDistBetweenLocations,
			    							  fileReader.yCoordMaxDistBetweenLocations,
			    							  "Path - " + fileName,
			    							  locationArray);
    	new VisualisationFrame(visualisationPack);
    }

    public void setTimer(){
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(processing.length() == 14){
                    processing = processing.substring(0, 10);
                    output.setText(processing);
                }
                else{
                	output.setText(processing);
                    processing += ".";
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchFileButton) {
            JFileChooser fileChooser = new JFileChooser();
            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
                String filePathHolder = fileChooser.getSelectedFile().getPath();
                if (isExtensionAcceptable(filePathHolder)) {
                    filePath = fileChooser.getSelectedFile().getPath();
                    fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

                    if (fileName.length() > 20) {
                        String truncatedFileName = fileName.substring(0, 8) + "..." + fileName.substring(fileName.length() - 8);
                        selectedFileArea.setText(truncatedFileName);
                        selectedFileArea.setToolTipText(fileName);
                    } else {
                        selectedFileArea.setText(fileName);
                    }

                } else {
                    JOptionPane.showMessageDialog(this, "Selected file must be in .txt or .tsp format.");
                }
            }
        } else if (e.getSource() == findPathButton) {
            if (doesFilePathExist()) {
                boolean appropriateContent = true;
                Scanner scanner;
                try{
                    scanner = new Scanner(new File(filePath));
                    int size = 0;
                    while(scanner.hasNextLine()){
                        String line = scanner.nextLine();
                        String lineParsed[] = line.split(" ");
                        if (lineParsed.length != 3) {
                            appropriateContent = false;
                            break;
                        }
                        try{
                            float xCoordinate = Float.parseFloat(lineParsed[1]);
                            float yCoordinate = Float.parseFloat(lineParsed[2]);
                        } catch (NumberFormatException ex) {
                            appropriateContent = false;
                            break;
                        }

                        size++;
                    }
                    if (size < 2){
                        appropriateContent = false;
                    }
                } catch (FileNotFoundException ex) {

                }



                if (appropriateContent) {
                    if (!(thread == null) && thread.isAlive()) {
                        timer.stop();
                        thread.stop();
                        findPathButton.setText("Find Path");
                        searchFileButton.setEnabled(true);
                        output.setText("");
                    } else {
                        thread = new Thread(this);
                        thread.start();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "File content is not appropriate.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a file.");
            }
        }
    }


    @Override
    public void run() {
    	analysisStarted();
        fileReader = new FileReader(filePath);
        locationArray = fileReader.locationArray;
        start = System.currentTimeMillis();
        EnhancedNearestNeighbour enhancedNearestNeighbour = new EnhancedNearestNeighbour(locationArray);
        locationArray = enhancedNearestNeighbour.locationArray;
        totalDistance = enhancedNearestNeighbour.totalDistance;
        elapsedTime = System.currentTimeMillis() - start;
        analysisFinished();
    }
}
