import java.io.File;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors

public class GenesWriter {
    public static void main(String[] args) {
        try {
            String[] s = new String[1];
            s[0] = "GeneStatus.txt";
            CreatingFiles.CreateFile.main(s);
            FileWriter myWriter = new FileWriter(s[0], true);
            myWriter.write(args[0]);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
