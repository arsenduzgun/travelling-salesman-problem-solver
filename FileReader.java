import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileReader {
    protected Location[] locationArray;
    protected float xCoordinateMinValue = Float.MAX_VALUE;
    protected float yCoordinateMinValue = Float.MAX_VALUE;
    protected float xCoordinateMaxValue = Float.MIN_VALUE;
    protected float yCoordinateMaxValue = Float.MIN_VALUE;
    protected float xCoordMaxDistBetweenLocations;
    protected float yCoordMaxDistBetweenLocations;

    public FileReader(String filePath) {
        readFile(filePath);
    }

    private void readFile(String filePath) {
        List<Location> locations = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue; // Skip empty lines

                String[] lineParsed = line.split("\\s+"); // Handle multiple spaces
                if (lineParsed.length < 3) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    String locationName = lineParsed[0];
                    float xCoordinate = Float.parseFloat(lineParsed[1]);
                    float yCoordinate = Float.parseFloat(lineParsed[2]);

                    // Update min/max values
                    xCoordinateMinValue = Math.min(xCoordinateMinValue, xCoordinate);
                    xCoordinateMaxValue = Math.max(xCoordinateMaxValue, xCoordinate);
                    yCoordinateMinValue = Math.min(yCoordinateMinValue, yCoordinate);
                    yCoordinateMaxValue = Math.max(yCoordinateMaxValue, yCoordinate);

                    locations.add(new Location(locationName, xCoordinate, yCoordinate));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid numeric values in line: " + line);
                }
            }

            // Convert List to Array
            locationArray = locations.toArray(new Location[0]);

            // Compute max distances
            xCoordMaxDistBetweenLocations = xCoordinateMaxValue - xCoordinateMinValue;
            yCoordMaxDistBetweenLocations = yCoordinateMaxValue - yCoordinateMinValue;

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
        }
    }
}
