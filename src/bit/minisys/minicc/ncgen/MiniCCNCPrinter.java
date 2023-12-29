package bit.minisys.minicc.ncgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MiniCCNCPrinter {

    public MiniCCNCPrinter() {

    }

    public void print(String filename, String instruction) {

        // write
        try {
            FileWriter fileWriter = new FileWriter(new File(filename));
            fileWriter.write(instruction);
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
