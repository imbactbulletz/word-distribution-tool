package app.component.input;

public class FileInfo {

    final String fileName;

    final String absolutePath;

    final String content;

    public FileInfo(String fileName, String absolutePath, String content) {
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getContent() {
        return content;
    }
}
