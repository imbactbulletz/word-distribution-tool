package app.component.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public class FileInputReadWorker implements Callable<FileInfo> {

    File file;

    public FileInputReadWorker(File file) {
        this.file = file;
    }

    @Override
    public FileInfo call() throws Exception {
        String content = Files.readString(Paths.get(file.getPath()));
        return new FileInfo(file.getName(), file.getAbsolutePath(), content);
    }
}
