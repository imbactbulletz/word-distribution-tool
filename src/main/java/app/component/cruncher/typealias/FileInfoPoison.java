package app.component.cruncher.typealias;

import app.component.input.FileInfo;

public class FileInfoPoison extends FileInfo {
    public FileInfoPoison(String fileName, String absolutePath, String content) {
        super(fileName, absolutePath, content);
    }
}
