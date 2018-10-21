package savestateTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Reader{

    private File file;

    //TODO
    public Tree read() throws IOException {
        return null;
    }

    public Reader(String filepath) throws FileNotFoundException {
        this.file = new File(filepath);
    }

}
