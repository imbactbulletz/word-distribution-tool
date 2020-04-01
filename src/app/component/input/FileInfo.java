package app.component.input;

public class FileInfo {

    String fileName;

    String absolutePath;

    String content;

    public FileInfo(String fileName, String absolutePath, String content) {
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.content = content;
    }
}
