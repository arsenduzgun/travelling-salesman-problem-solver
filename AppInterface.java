
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

public class AppInterface extends JFrame implements Runnable, ActionListener {
    protected JButton searchFileButton;
    protected JTextArea selectedFileArea;
    protected JButton findPathButton;
    protected JTextArea output;
    protected String filePath;
    protected static String fileName;
    protected static String selectedMethod;
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
        String NOT_SELECTABLE_OPTION = " - Select a method - ";
        String[] NORMAL_OPTION = {"Nearest Neighbour Algorithm", "Greedy Algorithm", "Divide and Conquer Algorithm"};
        JComboBox<String> methodList = new JComboBox<String>();

        methodList.setModel(new DefaultComboBoxModel<String>() {
            boolean selectionAllowed = true;
            @Override
            public void setSelectedItem(Object object) {
                if (!NOT_SELECTABLE_OPTION.equals(object)) {
                    super.setSelectedItem(object);
                }
                else if (selectionAllowed) {
                    selectionAllowed = false;
                    super.setSelectedItem(object);
                }
            }
        });

        methodList.addItem(NOT_SELECTABLE_OPTION);
        for(int i = 0; i < NORMAL_OPTION.length; i++){
            methodList.addItem(NORMAL_OPTION[i]);
        }

        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                selectedMethod = methodList.getSelectedItem().toString();
            }
        };

        methodList.addItemListener(itemListener);
        JPanel comboBoxPanel = new JPanel();
        comboBoxPanel.setBounds(165, 140, 200, 30);
        comboBoxPanel.add(methodList);
        add(comboBoxPanel);
        findPathButton = new JButton("Find Path");
        findPathButton.setBounds(200, 240, 128, 24);
        findPathButton.addActionListener(this);
        add(findPathButton);
        output = new JTextArea();
        output.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(output);
        outputScrollPane.setBounds(0, 300, 537, 164);
        add(outputScrollPane);
    }

    public boolean isFilePathExist(){
        return (filePath != null);
    }

    public boolean isExtensionAcceptable(String filePath){
        String extension = filePath.substring(filePath.lastIndexOf("."));
        if(extension.equals(".txt") || extension.equals(".tsp")){
            return true;
        }
        return false;
    }

    public boolean isMethodSelected(){
        return (selectedMethod != null);
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
 		       		  "\n\n" + "Algorithm used: " + selectedMethod +
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
			    							  "Locations(" + fileName + ")",
			    							  locationArray);
    	new VisualisationFrame(visualisationPack);
    	visualisationPack.visualisationType = "Path(" + fileName + ")(" + selectedMethod + ")";
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
            if (isFilePathExist()) {
                if (isMethodSelected()) {
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
                    JOptionPane.showMessageDialog(this, "Please select a method.");
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
        if(selectedMethod.equals("Nearest Neighbour Algorithm")) {
        	NearestNeighbour nearestNeighbour = new NearestNeighbour(locationArray);
        	locationArray = nearestNeighbour.locationArray;
        	totalDistance = nearestNeighbour.totalDistance;
        }
        else if(selectedMethod.equals("Greedy Algorithm")) {
        	Greedy greedy = new Greedy(locationArray);
        	locationArray = greedy.locationArray;
        	totalDistance = greedy.totalDistance;
        }
        else {
        	DivideAndConquer divideAndConquer = new DivideAndConquer(locationArray);
        	locationArray = divideAndConquer.locationArray;
        	totalDistance = divideAndConquer.totalDistance;
        }
        elapsedTime = System.currentTimeMillis() - start;
        analysisFinished();
    }
}
