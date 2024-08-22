package edo;

import java.io.FileWriter;
import java.io.IOException;

public class Writer {
    public static void main(String fileName, String phrase) {
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(phrase);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
